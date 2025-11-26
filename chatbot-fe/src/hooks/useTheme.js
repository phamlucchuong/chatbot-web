import { useTheme as useCtxTheme } from '../contexts/ThemeContext'

export default function useTheme() {
  const ctx = useCtxTheme()
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider')
  return ctx
}
