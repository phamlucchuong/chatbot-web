import React from 'react'
import useTheme from '../hooks/useTheme'

export default function Header() {
    const { theme, toggleTheme } = useTheme()

    return (
        <header className="z-100 px-6 md:px-20 py-3 border-b border-theme transition-colors">
          <div className="flex items-center justify-between">
            <div className="text-lg items-center flex gap-3 font-bold text-[var(--text)] cursor-pointer">
              <i className="fa-solid fa-robot"></i>
              <span>Chat skibidi</span>
            </div>

            <div className="flex items-center gap-3">
              <button onClick={toggleTheme} title="Chuyển giao diện" className="p-2 rounded hover:bg-gray-200 dark:hover:bg-gray-400">
                {theme === 'dark' ? <i className="fa-solid fa-sun"></i> : <i className="fa-solid fa-moon text-black"></i>}
              </button>
            </div>
          </div>
        </header>
    )
}