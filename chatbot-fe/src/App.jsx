import { useState, useEffect, useRef } from 'react'
import './App.css'
import LoginModal from './components/modals/LoginModal'
import RegisterModal from './components/modals/RegisterModal'
import { useLogout } from './hooks/useAuth'
import { useChat } from './hooks/useChat'
import { formatDate } from './utils/dateUtil'

function App() {

  const [isTyping, setIsTyping] = useState(false);
  const [content, setContent] = useState("");
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentChatId, setCurrentChatId] = useState(null);
  const dropdownRef = useRef(null);
  const messagesEndRef = useRef(null);

  const [chatHistory, setChatHistory] = useState([]);
  const [messages, setMessages] = useState([]);
  // const [chatHistory, setChatHistory] = useChat();


  // Kiểm tra trạng thái đăng nhập từ localStorage
  useEffect(() => {
    const auth = localStorage.getItem("auth");
    const token = localStorage.getItem("token");
    setIsLoggedIn(auth === "true" && !!token);
  }, []);

  // Load lịch sử trò chuyện từ localStorage
  useEffect(() => {
    const savedHistory = localStorage.getItem("chatHistory");
    if (savedHistory) {
      try {
        const parsed = JSON.parse(savedHistory);
        setChatHistory(parsed);
      } catch (error) {
        console.error("Error loading chat history:", error);
      }
    }
  }, []);

  // Lưu lịch sử trò chuyện vào localStorage khi có thay đổi
  useEffect(() => {
    if (chatHistory.length > 0) {
      localStorage.setItem("chatHistory", JSON.stringify(chatHistory));
    }
  }, [chatHistory]);

  // Đóng dropdown khi click bên ngoài
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };

    if (isDropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isDropdownOpen]);

  // Auto scroll khi có tin nhắn mới
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleChange = (event) => {
    setContent(event.target.value); // Cập nhật state 'content' với giá trị mới từ textarea
    if (event.target.value.length > 0) { // Kiểm tra độ dài của giá trị mới
      setIsTyping(true);
    } else {
      setIsTyping(false);
    }
  }

  const handleUserIconClick = () => {
    setIsDropdownOpen(!isDropdownOpen);
  }

  const handleLoginClick = () => {
    setIsDropdownOpen(false);
    setIsLoginModalOpen(true);
  }

  const { logoutUser } = useLogout();

  const handleLogout = async () => {
    await logoutUser(localStorage.getItem("token"));
    localStorage.removeItem("token");
    localStorage.removeItem("auth");
    setIsLoggedIn(false);
    setIsDropdownOpen(false);
    alert("Đã đăng xuất thành công!");
    window.location.reload();
  }

  const handleCloseLoginModal = () => {
    setIsLoginModalOpen(false);
    // Cập nhật lại trạng thái đăng nhập sau khi đóng modal
    const auth = localStorage.getItem("auth");
    const token = localStorage.getItem("token");
    setIsLoggedIn(auth === "true" && !!token);
  }

  const handleCloseRegisterModal = () => {
    setIsRegisterModalOpen(false);
  }

  const handleSwitchToRegister = () => {
    setIsLoginModalOpen(false);
    setIsRegisterModalOpen(true);
  }

  const handleSwitchToLogin = () => {
    setIsRegisterModalOpen(false);
    setIsLoginModalOpen(true);
  }

  const [isNewChat, setIsNewChat] = useState(true);
  // Tạo cuộc trò chuyện mới
  const handleNewChat = () => {
    const newChat = {
      id: Date.now().toString(),
      title: "Cuộc trò chuyện mới",
      createdAt: new Date().toISOString(),
      messages: []
    };
    setIsNewChat(true);
    console.log("Creating new chat:", newChat);
    setChatHistory([newChat, ...chatHistory]);
    setCurrentChatId(newChat.id);
    setContent("");
    setMessages([]);
  }

  // Chọn cuộc trò chuyện
  const handleSelectChat = (chatId) => {
    setCurrentChatId(chatId);
    const selectedChat = chatHistory.find(chat => chat.id === chatId);
    if (selectedChat && selectedChat.messages) {
      setMessages(selectedChat.messages);
    } else {
      setMessages([]);
    }
  }

  // Xóa cuộc trò chuyện
  const handleDeleteChat = (e, chatId) => {
    e.stopPropagation(); // Ngăn chặn event bubble
    const updatedHistory = chatHistory.filter(chat => chat.id !== chatId);
    setChatHistory(updatedHistory);
    if (currentChatId === chatId) {
      setCurrentChatId(null);
      setContent("");
    }
    // Xóa khỏi localStorage
    if (updatedHistory.length === 0) {
      localStorage.removeItem("chatHistory");
    } else {
      localStorage.setItem("chatHistory", JSON.stringify(updatedHistory));
    }
  }

  // const [ chatId, setChatId ] = useState(null);
  const { createNewChat, sendMessage } = useChat();
  const handleSearch = async () => {
    // Lưu content vào biến local ngay lập tức để tránh bị mất do state update
    const messageContent = content;

    console.log("=== DEBUG handleSearch ===");
    console.log("State content:", content);
    console.log("Local messageContent:", messageContent);

    // Kiểm tra content không rỗng
    if (!messageContent || messageContent.trim() === "") {
      console.log("Content is empty");
      return;
    }

    let chatId = localStorage.getItem("chatId");
    console.log("Current chatId from localStorage:", chatId);
    console.log("isNewChat:", isNewChat);

    if (isNewChat) {
      console.log("Creating new chat...");
      const response = await createNewChat(localStorage.getItem("token"));
      console.log("createNewChat response:", response);

      if (response && response.results && response.results.id) {
        chatId = response.results.id;
        localStorage.setItem("chatId", chatId);
        console.log("New chat created with ID:", chatId);
        setIsNewChat(false);
      } else {
        console.error("Failed to create new chat, response:", response);
        return;
      }
    }

    // Thêm tin nhắn người dùng vào UI ngay lập tức
    const userMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: messageContent,
      timestamp: new Date().toISOString()
    };
    setMessages(prev => [...prev, userMessage]);

    // Đảm bảo chatId tồn tại và content không bị null
    console.log("Before sendMessage - chatId:", chatId, "messageContent:", messageContent);
    if (chatId && messageContent) {
      const result = await sendMessage(localStorage.getItem("token"), chatId, messageContent);
      console.log("sendMessage result:", result);

      // Thêm phản hồi từ bot vào UI
      if (result && result.results) {
        const botMessage = {
          id: (Date.now() + 1).toString(),
          role: 'assistant',
          content: result.results.response || result.results.message || 'Xin lỗi, tôi không có câu trả lời.',
          timestamp: new Date().toISOString()
        };
        setMessages(prev => [...prev, botMessage]);

        // Cập nhật chat history với messages mới
        setChatHistory(prev => prev.map(chat =>
          chat.id === currentChatId || localStorage.getItem("chatId") === chatId
            ? { ...chat, messages: [...(chat.messages || []), userMessage, botMessage] }
            : chat
        ));
      }
    } else {
      console.error("Missing chatId or content:", { chatId, messageContent });
    }

    // Sau khi gửi, xóa nội dung và đặt isTyping về false
    setContent("");
    setIsTyping(false);
  }


  return (
    <div className="flex h-screen">
      {/* Sidebar */}
      <aside className="w-80 bg-[#282A2C] border-r border-gray-600 flex flex-col">
        {/* Header Sidebar */}
        <div className="p-6">
          <button
            onClick={handleNewChat}
            className="w-full border-b border-gray-400 flex items-center gap-2 px-8 py-4 bg-[#282A2C] text-white rounded-lg hover:bg-[#262628] transition-colors text-sm font-medium"
          >
            <i className="fa-solid fa-plus"></i>
            Cuộc trò chuyện mới
          </button>
        </div>

        {/* Danh sách lịch sử trò chuyện */}
        <div className="flex-1 overflow-y-auto m-4">
          <span>Gần đây</span>
          {chatHistory.length === 0 ? (
            <div className="p-4 text-center text-gray-500 text-sm">
              <i className="fa-regular fa-comments text-2xl mb-2 block"></i>
              <p>Chưa có lịch sử trò chuyện</p>
            </div>
          ) : (
            <div className="p-2">
              {chatHistory.map((chat) => (
                <div
                  key={chat.id}
                  onClick={() => handleSelectChat(chat.id)}
                  className={`group relative bg-[#262628] p-3 mb-1 rounded-lg cursor-pointer transition-colors ${currentChatId === chat.id
                    ? "bg-blue-100 border border-blue-300"
                    : "hover:border-gray-600 border border-transparent"
                    }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <i className="fa-regular fa-message text-gray-500 text-xs"></i>
                        <p className="text-sm font-medium text-[#C0C1C1] truncate">
                          {chat.title}
                        </p>
                      </div>
                      <p className="text-xs text-gray-500">
                        {formatDate(chat.createdAt)}
                      </p>
                    </div>
                    <button
                      onClick={(e) => handleDeleteChat(e, chat.id)}
                      className="opacity-0 group-hover:opacity-100 p-1 text-gray-400 hover:text-red-500 transition-all"
                      title="Xóa cuộc trò chuyện"
                    >
                      <i className="fa-solid fa-trash text-xs"></i>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        <header className="flex justify-between items-center z-100 px-10 py-5 border-b border-gray-600">
          <div className="text-lg align-center">
            <p>Chatbot health care</p>
          </div>

          <div className="relative" ref={dropdownRef}>
            <div onClick={handleUserIconClick} className='text-4xl cursor-pointer hover:opacity-80 transition-opacity'>
              <i className="fa-regular fa-circle-user"></i>
            </div>

            {isDropdownOpen && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-50">
                {!isLoggedIn ? (
                  <button
                    onClick={handleLoginClick}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2"
                  >
                    <i className="fa-solid fa-right-to-bracket"></i>
                    Đăng nhập
                  </button>
                ) : (
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2"
                  >
                    <i className="fa-solid fa-right-from-bracket"></i>
                    Đăng xuất
                  </button>
                )}
              </div>
            )}
          </div>
        </header>

        <div className='flex-1 flex flex-col overflow-hidden'>
          {/* Messages container */}
          <div className='flex-1 overflow-y-auto py-10 px-4'>
            {messages.length === 0 ? (
              <div className='flex justify-center items-center h-full'>
                <span className="ombre-color text-2xl">Can i help you, sir!</span>
              </div>
            ) : (
              <div className='max-w-3xl mx-auto space-y-4'>
                {messages.map((message) => (
                  <div
                    key={message.id}
                    className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                  >
                    <div
                      className={`max-w-[70%] rounded-2xl px-4 py-3 ${message.role === 'user'
                          ? 'bg-blue-500 text-white'
                          : 'bg-gray-200 text-gray-800'
                        }`}
                    >
                      <p className='whitespace-pre-wrap break-words'>{message.content}</p>
                      <p className={`text-xs mt-1 ${message.role === 'user' ? 'text-blue-100' : 'text-gray-500'
                        }`}>
                        {new Date(message.timestamp).toLocaleTimeString('vi-VN', {
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </p>
                    </div>
                  </div>
                ))}
                <div ref={messagesEndRef} />
              </div>
            )}
          </div>

          {/* Input container - fixed at bottom */}
          <div className='py-4 px-4'>
            {/* <div className='border-t border-gray-600 py-4 px-4'> */}
            <div className="w-full max-w-3xl mx-auto border border-gray-600 rounded-2xl px-[20px] py-[15px] shadow">

              <textarea value={content} onChange={handleChange} id="input" rows="2" placeholder="Hỏi chatbot"></textarea>

              <div className="textbox--bottom">
                <div className="textbox--bottom-left">

                  <div id="plus-icon">
                    <i className=" fa-solid fa-plus"></i>

                    <div id="popup-menu">
                      <ul>
                        <li>
                          <i className="fa-regular fa-image"></i>
                          Hình ảnh
                        </li>
                        <li >
                          <i className="fa-solid fa-paperclip"></i>
                          Tệp
                        </li>
                      </ul>
                    </div>
                  </div>

                  <div className="deepSearch">
                    <i className="fa-solid fa-magnifying-glass mx-2"></i>
                    <span>Deep search</span>
                  </div>
                </div>

                <div className="textbox--bottom-right">
                  {
                    isTyping
                      ? <button onClick={handleSearch}>
                        <i className="fa-solid fa-paper-plane"></i>
                      </button>
                      : <button>
                        <i className="micro-icon icon fa-solid fa-microphone"></i>
                      </button>
                  }
                </div>

              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Login Modal */}
      <LoginModal
        isOpen={isLoginModalOpen}
        onClose={handleCloseLoginModal}
        onSwitchToRegister={handleSwitchToRegister}
      />

      {/* Register Modal */}
      <RegisterModal
        isOpen={isRegisterModalOpen}
        onClose={handleCloseRegisterModal}
        onSwitchToLogin={handleSwitchToLogin}
      />

    </div>
  )
}

export default App
