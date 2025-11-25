import ConversationCard from "../components/cards/ConversationCard";
import { useState } from "react";
import LoginModal from '../components/modals/LoginModal'
import RegisterModal from '../components/modals/RegisterModal'
import { useLogout } from '../hooks/useAuth'


export default function SideBar({ handleNewChat, chatHistory, currentChatId, handleSelectChat, handleDeleteChat, isLoggedIn: isLoggedInProp, handleLoginClick, handleLogout, handleSwitchToRegister }) {
    const [collapsed, setCollapsed] = useState(false)
    const [isLoginModalOpen, setIsLoginModalOpen] = useState(false)
    const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false)
    const { logoutUser } = useLogout()

    const loggedIn = typeof isLoggedInProp !== 'undefined' ? isLoggedInProp : (localStorage.getItem('auth') === 'true' && !!localStorage.getItem('token'))

    const openLogin = () => {
        if (typeof handleLoginClick === 'function') return handleLoginClick()
        setIsLoginModalOpen(true)
    }

    const openRegister = () => {
        if (typeof handleSwitchToRegister === 'function') return handleSwitchToRegister()
        setIsRegisterModalOpen(true)
    }

    const doLogout = async () => {
        if (typeof handleLogout === 'function') return handleLogout()
        try {
            await logoutUser(localStorage.getItem('token'))
        } catch {
            // ignore
        }
        localStorage.removeItem('token')
        localStorage.removeItem('auth')
        window.location.reload()
    }

    const userName = (() => {
        try {
            const u = localStorage.getItem('user')
            if (u) {
                const parsed = JSON.parse(u)
                return parsed?.name || parsed?.username || ''
            }
        } catch { /* ignore parse error */ }
        return localStorage.getItem('username') || localStorage.getItem('userName') || ''
    })()

    return (
        <aside className={`${collapsed ? 'w-20' : 'w-72'} bg-[#282A2C] border-r border-gray-600 flex flex-col transition-all`}> 
            {/* Header Sidebar */}
            <div className="p-2">
                {collapsed ? (
                    <div className="flex flex-col-reverse items-center gap-2">
                        <button
                            onClick={handleNewChat}
                            className="w-10 h-10 flex items-center justify-center rounded hover:bg-gray-700 text-white"
                            title="Tạo cuộc trò chuyện mới"
                            aria-label="Tạo cuộc trò chuyện mới"
                        >
                            <i className="fa-solid fa-plus"></i>
                        </button>

                        <button
                            onClick={() => setCollapsed(false)}
                            className="w-10 h-10 flex items-center justify-center rounded hover:bg-gray-700 text-gray-300"
                            title="Mở sidebar"
                            aria-label="Mở sidebar"
                        >
                            <i className={`fa-solid fa-chevron-right`}></i>
                        </button>
                    </div>
                ) : (
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <button
                                onClick={handleNewChat}
                                className={`flex items-center gap-2 px-8 py-3 bg-[#282A2C] text-white rounded-lg hover:bg-[#262628] transition-colors text-sm font-medium`}
                                title="Tạo cuộc trò chuyện mới"
                            >
                                <i className="fa-solid fa-plus"></i>
                                <span>Cuộc trò chuyện mới</span>
                            </button>
                        </div>

                        <div>
                            <button onClick={() => setCollapsed(!collapsed)} title={collapsed ? 'Mở sidebar' : 'Thu nhỏ sidebar'} className="text-gray-300 p-2 rounded hover:bg-gray-700">
                                <i className={`fa-solid ${collapsed ? 'fa-chevron-right' : 'fa-chevron-left'}`}></i>
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Danh sách lịch sử trò chuyện */}
            <div className={`flex-1 overflow-y-auto ${collapsed ? 'px-1' : 'm-4'}`}>
                {!collapsed && <span className="block mb-3">Gần đây</span>}
                {chatHistory.length === 0 ? (
                    <div className={`p-4 text-center text-gray-500 text-sm ${collapsed ? 'p-2' : ''}`}>
                        <i className="fa-regular fa-comments text-2xl mb-2 block"></i>
                        {!collapsed && <p>Chưa có lịch sử trò chuyện</p>}
                    </div>
                ) : (
                    <div className={`${collapsed ? 'px-1 space-y-1' : 'px-2'}`}>
                        {chatHistory.map((chat) => (
                            <ConversationCard
                                key={chat.id}
                                chat={chat}
                                currentChatId={currentChatId}
                                handleSelectChat={handleSelectChat}
                                handleDeleteChat={handleDeleteChat}
                                compact={collapsed}
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Footer area - login / account / logo */}
            {collapsed ? (
                <div className="p-3 border-t border-gray-700 flex items-center justify-center">
                    <div className="text-2xl text-white">
                        <i className="fa-solid fa-robot"></i>
                    </div>
                </div>
            ) : (
                <div className="p-4 border-t border-gray-700">
                    {!loggedIn ? (
                        <div className="flex flex-col gap-2">
                            <button onClick={openLogin} className="w-full px-4 py-2 rounded bg-[#282A2C] text-white text-sm">Đăng nhập</button>
                            <button onClick={openRegister} className="w-full px-4 py-2 rounded border border-gray-600 text-white text-sm">Đăng ký</button>
                        </div>
                    ) : (
                        <div className="flex flex-col gap-2">
                            <div className="text-sm text-gray-300">Xin chào, <span className="font-medium text-white">{userName || 'Bạn'}</span></div>
                            <button onClick={doLogout} className="w-full px-4 py-2 rounded bg-red-500 text-white text-sm">Đăng xuất</button>
                        </div>
                    )}
                </div>
            )}

            <LoginModal isOpen={isLoginModalOpen} onClose={() => { setIsLoginModalOpen(false); const auth = localStorage.getItem('auth'); if (auth === 'true') window.location.reload(); }} onSwitchToRegister={() => { setIsLoginModalOpen(false); setIsRegisterModalOpen(true); }} />
            <RegisterModal isOpen={isRegisterModalOpen} onClose={() => { setIsRegisterModalOpen(false); const auth = localStorage.getItem('auth'); if (auth === 'true') window.location.reload(); }} onSwitchToLogin={() => { setIsRegisterModalOpen(false); setIsLoginModalOpen(true); }} />
        </aside>
    );

}