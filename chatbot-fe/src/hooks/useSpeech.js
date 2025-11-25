import { useEffect, useRef, useState } from 'react'

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

  // Text-to-speech (browser only)
  const synthRef = useRef(typeof window !== 'undefined' ? window.speechSynthesis : null)
  const utterRef = useRef(null)
  const [isSpeaking, setIsSpeaking] = useState(false)
  const [playingMessageId, setPlayingMessageId] = useState(null)
  const [voices, setVoices] = useState([])
  const [selectedVoice, setSelectedVoice] = useState(null)

  useEffect(() => {
    if (!synthRef.current) return

    const loadVoices = () => {
      const v = synthRef.current.getVoices() || []
      setVoices(v)
      const vi = v.find(vo => vo.lang && vo.lang.toLowerCase().startsWith('vi'))
        || v.find(vo => /viet|vietnam/i.test(vo.name))
        || v.find(vo => /vietnamese/i.test(vo.lang))
      if (vi) setSelectedVoice(vi)
    }

    loadVoices()
    const synth = synthRef.current
    if (synth) synth.onvoiceschanged = loadVoices
    return () => { if (synth) synth.onvoiceschanged = null }
  }, [])

  const reloadVoices = () => {
    if (!synthRef.current) return []
    const v = synthRef.current.getVoices() || []
    setVoices(v)
    return v
  }

  const ensureVoicesLoaded = (timeout = 1500) => {
    return new Promise((resolve) => {
      if (!synthRef.current) return resolve([])
      const v = synthRef.current.getVoices() || []
      if (v.length > 0) return resolve(v)
      let resolved = false
      const onVoices = () => {
        if (resolved) return
        const vv = synthRef.current.getVoices() || []
        if (vv.length > 0) {
          resolved = true
          synthRef.current.onvoiceschanged = null
          setVoices(vv)
          resolve(vv)
        }
      }
      synthRef.current.onvoiceschanged = onVoices
      setTimeout(() => {
        if (!resolved) {
          resolved = true
          if (synthRef.current) synthRef.current.onvoiceschanged = null
          const vv = synthRef.current.getVoices() || []
          setVoices(vv)
          resolve(vv)
        }
      }, timeout)
    })
  }

  const speak = async (text, { voice = null, rate = 1, pitch = 1, messageId = null } = {}) => {
    if (!('speechSynthesis' in window)) return
    // stop any current speech
    stopSpeaking()
    if ((!voices || voices.length === 0) && synthRef.current) await ensureVoicesLoaded(1500)

    let useVoice = voice || selectedVoice
    if (!useVoice && voices && voices.length > 0) {
      useVoice = voices.find(v => v.lang && v.lang.toLowerCase().startsWith('vi'))
        || voices.find(v => /viet|vietnam/i.test(v.name))
        || voices.find(v => /vietnamese/i.test(v.lang))
        || voices[0]
    }

    const u = new SpeechSynthesisUtterance(text)
    u.lang = lang || (useVoice && useVoice.lang) || 'vi-VN'
    u.rate = rate
    u.pitch = pitch
    if (useVoice) u.voice = useVoice
    u.onstart = () => {
      setIsSpeaking(true)
      if (messageId !== null && messageId !== undefined) setPlayingMessageId(messageId)
    }
    u.onend = () => {
      setIsSpeaking(false)
      setPlayingMessageId(null)
    }
    u.onerror = () => {
      setIsSpeaking(false)
      setPlayingMessageId(null)
    }
    utterRef.current = u
    try { synthRef.current.speak(u) } catch (e) { console.warn('speechSynthesis.speak failed', e) }
  }

  const stopSpeaking = () => {
    if (synthRef.current && typeof synthRef.current.cancel === 'function') synthRef.current.cancel()
    utterRef.current = null
    setIsSpeaking(false)
    setPlayingMessageId(null)
  }

  const setVoiceByName = (name) => {
    if (!voices || voices.length === 0) reloadVoices()
    const match = (voices || []).find(v => v.name === name || (v.name || '').toLowerCase().includes((name || '').toLowerCase()))
    if (match) setSelectedVoice(match)
    return match
  }

  return {
    // recognition
    isSupported,
    isRecording,
    interim,
    finalTranscript,
    startRecognition,
    stopRecognition,
    // tts
    speak,
    stopSpeaking,
    isSpeaking,
    playingMessageId,
    // voices
    voices,
    selectedVoice,
    setSelectedVoice,
    reloadVoices,
    ensureVoicesLoaded,
    setVoiceByName,
  }
}
