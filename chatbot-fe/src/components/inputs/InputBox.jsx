import React, { useEffect, useState } from 'react';
import LoginModal from '../modals/LoginModal';
import PricingModal from '../modals/PricingModal';
import useSpeech from '../../hooks/useSpeech'

export default function InputBox({ content, handleChange, handleSearch }) {

  const [isOpenMenu, setIsOpenMenu] = useState(false);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const { isSupported, isRecording, finalTranscript, startRecognition, stopRecognition, stopSpeaking } = useSpeech({ lang: 'vi-VN' })
  const lastAppendedRef = React.useRef({ text: null, at: 0 })

  useEffect(() => {
    if (!isRecording) return

    if (finalTranscript && finalTranscript.trim().length > 0) {
      const text = finalTranscript.trim()

      const now = Date.now()
      const last = lastAppendedRef.current
      if (last.text === text && (now - last.at) < 3000) {
        return
      }

      lastAppendedRef.current = { text, at: now }

      const newValue = content ? `${content} ${text}` : text
      handleChange({ target: { value: newValue } })
    }
  }, [finalTranscript, content, handleChange, handleSearch, isRecording])

  const handleClick = () => {
    setIsLoginModalOpen(true);
  }


    return (
    <div className='py-4 px-4'>
      <div className="w-full max-w-3xl mx-auto border border-theme rounded-2xl px-[20px] py-[15px] shadow bg-card">

        <textarea
          value={content}
          onChange={handleChange}
          id="input"
          rows="2"
          placeholder="Hỏi chatbot"
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault()
              if (typeof stopRecognition === 'function') stopRecognition()
              if (typeof stopSpeaking === 'function') stopSpeaking()
              handleSearch()
            }
          }}
        />

          <div className="textbox--bottom">
          <div className="textbox--bottom-left w-1/2 gap-2">

            <div onClick={() => setIsOpenMenu(!isOpenMenu)} id="plus-icon">
              <i className=" fa-solid fa-plus w-10"></i>

              {isOpenMenu &&
              <ul className='w-64 bg-card shadow-lg rounded-lg p-4 absolute bottom-16 left-4 z-10'>
                    <li onClick={handleClick} className='hover:bg-gray-800'>
                      <i className="fa-regular fa-image"></i>
                      Hình ảnh
                    </li>
                    <li onClick={handleClick}  className='hover:bg-gray-800'>
                      <i className="fa-solid fa-paperclip"></i>
                      Tệp
                    </li>
                </ul>}
            </div>

            <div onClick={handleClick} className="deepSearch">
              <i className="fa-solid fa-magnifying-glass mx-2"></i>
              <span>Deep search</span>
            </div>
          </div>

          <div className="textbox--bottom-right w-1/2 flex justify-end items-center gap-2">
              {isSupported && (
                    <>
                      <button
                        onClick={() => {
                          if (isRecording) stopRecognition()
                          else startRecognition()
                        }}
                        className={`w-10 rounded-full transition-colors ${isRecording ? 'bg-red-500 text-white' : 'bg-[var(--surface)] text-[var(--text)]'}`}
                        title='Chép chính tả'
                      >
                        <i className={`fa-solid fa-microphone ${isRecording ? 'animate-pulse' : ''}`}></i>
                      </button>
                    </>
                  )}
          </div>

        </div>
      </div>

      <PricingModal isOpen={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)} />
      <p className='text-center text-xs text-gray-500 m-4'>Thông tin chỉ mang tính chất tham khảo. Bạn nên đến cơ sở y tế để được thăm khám chính xác</p>
    </div>

    
  );

}