import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { SpeechProvider } from './contexts/SpeechContext'
import { ThemeProvider } from './contexts/ThemeContext'


createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider>
      <SpeechProvider>
        <App />
      </SpeechProvider>
    </ThemeProvider>
  </StrictMode>,
)
