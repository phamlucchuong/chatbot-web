import React, { createContext, useContext, useMemo, useState } from 'react'
import useSpeech from '../hooks/useSpeech'

const SpeechContext = createContext(null)

export function SpeechProvider({ children }) {
  const speech = useSpeech({ lang: 'vi-VN' })

  const [ttsEnabled, setTtsEnabled] = useState(() => {
    try {
      const v = localStorage.getItem('ttsEnabled')
      return v === null ? true : v === 'true'
    } catch {
      return true
    }
  })

  const setTtsEnabledAndPersist = (v) => {
    setTtsEnabled(v)
    try { localStorage.setItem('ttsEnabled', v ? 'true' : 'false') } catch { /* ignore */ }
  }

  const value = useMemo(() => ({
    ...speech,
    ttsEnabled,
    setTtsEnabled: setTtsEnabledAndPersist,
  }), [speech, ttsEnabled])

  return <SpeechContext.Provider value={value}>{children}</SpeechContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useSpeechContext() {
  const ctx = useContext(SpeechContext)
  if (!ctx) throw new Error('useSpeechContext must be used within SpeechProvider')
  return ctx
}

export default SpeechContext
