


export default function Header({dropdownRef, isDropdownOpen, handleUserIconClick, isLoggedIn, handleLoginClick, handleLogout}) {
    return (
        <header className="flex justify-between items-center z-100 px-20 py-5 border-b border-gray-600">
          <div className="text-lg items-center flex gap-3 font-bold text-white cursor-pointer">
            <i className="fa-solid fa-robot"></i>
            <span>Chat skibidi</span>
          </div>

          <div className="relative" ref={dropdownRef}>
            <div onClick={handleUserIconClick} className='text-4xl cursor-pointer px-3 border-b border-gray-500 hover:opacity-80 transition-opacity'>
              {
                isLoggedIn
                ? <i className="fa-brands fa-accessible-icon"></i>
                : <i className="fa-solid fa-wheelchair"></i>
              }
            </div>

            {isDropdownOpen && (
              <div className="absolute right-0 mt-2 w-48 bg-[#262628] rounded-lg shadow-lg border border-transparent transition-colors hover:border-gray-500 py-2 z-50">
                {!isLoggedIn ? (
                  <button
                    onClick={handleLoginClick}
                    className="w-full text-left px-4 py-2 text-sm text-white transition-colors flex items-center gap-2"
                  >
                    <i className="fa-solid fa-right-to-bracket"></i>
                    Đăng nhập
                  </button>
                ) : (
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-sm text-white transition-colors flex items-center gap-2"
                  >
                    <i className="fa-solid fa-right-from-bracket"></i>
                    Đăng xuất
                  </button>
                )}
              </div>
            )}
          </div>
        </header>
    );

}