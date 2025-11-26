import { useEffect, useRef, useState, useCallback } from 'react'

export default function useSpeech({ lang = 'vi-VN' } = {}) {
  const recognitionRef = useRef(null)
  const [isSupported, setIsSupported] = useState(false)
  const [isRecording, setIsRecording] = useState(false)
  const [interim, setInterim] = useState('')
  const [finalTranscript, setFinalTranscript] = useState('')

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    if (!SpeechRecognition) {
      setIsSupported(false)
      return
    }

    setIsSupported(true)
    const rec = new SpeechRecognition()
    rec.continuous = false
    rec.interimResults = true
    rec.lang = lang

    rec.onresult = (event) => {
      let interimTranscript = ''
      let final = ''
      for (let i = event.resultIndex; i < event.results.length; ++i) {
        const res = event.results[i]
        if (res.isFinal) final += res[0].transcript
        else interimTranscript += res[0].transcript
      }
      if (interimTranscript) setInterim(interimTranscript)
      if (final) {
        setFinalTranscript(final)
        setInterim('')
      }
    }

    rec.onerror = (e) => {
      console.error('Speech recognition error', e)
      setIsRecording(false)
    }

    rec.onend = () => {
      setIsRecording(false)
    }

    recognitionRef.current = rec

    return () => {
      const r = recognitionRef.current
      if (r && typeof r.stop === 'function') r.stop()
    }
  }, [lang])

  const startRecognition = () => {
    if (!recognitionRef.current || isRecording) return
    try {
      recognitionRef.current.start()
      setIsRecording(true)
      setInterim('')
      setFinalTranscript('')
    } catch (err) {
      console.warn('start recognition error', err)
    }
  }

  const stopRecognition = () => {
    const r = recognitionRef.current
    if (r && typeof r.stop === 'function') r.stop()
    setIsRecording(false)
  }

  const audioRef = useRef(null)
  const [isSpeaking, setIsSpeaking] = useState(false)
  const [playingMessageId, setPlayingMessageId] = useState(null)
  // Zalo TTS voices can be managed here if needed, for now, we use a default
  const [selectedVoice] = useState('banmai') // Default Zalo voice

  const zaloTtsApi = async (text, voice = 'banmai', speed = 1.0) => {
    // This should be an API call to your backend, which then calls Zalo AI
    // For demonstration, this is a placeholder.
    // In a real app, you'd have an API endpoint like '/api/tts/zalo'
    // that takes { text, voice, speed } and returns the audio URL.
    console.log(`Requesting TTS for: "${text}" with voice: ${voice}`)
    // Replace with your actual backend API endpoint
    // THAY THẾ "your_actual_zalo_api_key_here" BẰNG KHÓA API THỰC CỦA BẠN
    const ZALO_API_KEY = import.meta.env.VITE_ZALO_API_KEY
    const response = await fetch('https://api.zalo.ai/v1/tts/synthesize', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'apikey': ZALO_API_KEY,
        },
        body: new URLSearchParams({
            input: text,
            speaker_id: voice === 'banmai' ? 1 : 2, 
            speed: speed,
        })
    });
    const data = await response.json();
    if (data && data.data && data.data.url) {
        return data.data.url;
    }
    throw new Error(data.message || 'Zalo TTS API failed');
  }

  const speak = async (text, { voice = null, rate = 1, messageId = null, speaker = 'bot' } = {}) => {
    stopSpeaking() // Stop any currently playing audio
    try {
      const audioUrl = await zaloTtsApi(text, voice || selectedVoice, rate);
      const audio = new Audio(audioUrl);
      audioRef.current = audio;

      audio.onplay = () => { // Chỉ nên kích hoạt khi bot nói
        setIsSpeaking(true);
        if (messageId !== null) setPlayingMessageId(messageId);
      };
      audio.onended = () => {
        setIsSpeaking(false);
        setPlayingMessageId(null);
        audioRef.current = null;
      };
      audio.onerror = () => {
        console.error('Error playing audio');
        setIsSpeaking(false);
        setPlayingMessageId(null);
        audioRef.current = null;
      };
      audio.play();
    } catch (error) {
      console.error('Failed to get TTS audio from Zalo API:', error);
      setIsSpeaking(false);
      setPlayingMessageId(null);
    }
  }

  const stopSpeaking = () => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.src = ''; // Detach the source
      audioRef.current = null;
    }
    setIsSpeaking(false)
    setPlayingMessageId(null)
  }

  // Debounce function to prevent rapid API calls
  const debounce = (func, delay) => {
    let timeoutId;
    return (...args) => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        func.apply(this, args);
      }, delay);
    };
  };

  // Create a debounced version of the speak function.
  // This will prevent 429 "Too Many Requests" errors if the user clicks rapidly.
  // We use useCallback to ensure the debounced function is not recreated on every render.
  const debouncedSpeak = useCallback(debounce(speak, 500), [selectedVoice]); // 500ms delay

  return {
    // recognition
    isSupported,
    isRecording,
    interim,
    finalTranscript,
    startRecognition,
    stopRecognition,
    // tts
    speak, // The original function
    debouncedSpeak, // The new debounced function
    stopSpeaking,
    isSpeaking,
    playingMessageId,
    // voices
    selectedVoice,
  }
}
