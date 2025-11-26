import { useEffect, useRef, useState } from 'react'
import './App.css'
import MessageCard from './components/cards/MessageCard'
import InputBox from './components/inputs/InputBox'
import LoginModal from './components/modals/LoginModal'
import RegisterModal from './components/modals/RegisterModal'
import { useLogout } from './hooks/useAuth'
import { useChat } from './hooks/useChat'
import Header from './layouts/Header'
import { useSpeechContext } from './contexts/SpeechContext'
import { showToast } from './utils/notify'
import SideBar from './layouts/SideBar'

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

  const { createNewChat, sendMessage, fetchChatHistory, getMessages, deleteConversation } = useChat();

  // Kiểm tra trạng thái đăng nhập từ localStorage
  useEffect(() => {
    const auth = localStorage.getItem("auth");
    const token = localStorage.getItem("token");
    setIsLoggedIn(auth === "true" && !!token);
  }, []);

  // Load lịch sử chat từ backend khi user đăng nhập
  useEffect(() => {
    const loadChatHistory = async () => {
      const auth = localStorage.getItem("auth");
      const token = localStorage.getItem("token");

      if (auth === "true" && token) {
        try {
          const response = await fetchChatHistory(token);
          console.log("Chat history loaded:", response);

          if (response && response.results) {
            // Transform backend data to frontend format
            const transformedHistory = response.results
              .map((conv) => ({
                id: conv.id,
                name: conv.name || "Cuộc trò chuyện",
                createdAt: conv.createdAt,
              }))
              .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            setChatHistory(transformedHistory);
          }
        } catch (error) {
          console.error("Error loading chat history:", error);
        }
      }
    };

    loadChatHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn]);

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

  // Hàm để cuộn xuống tin nhắn cuối cùng
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };
  
  // Load tin nhắn của cuộc trò chuyện cuối cùng khi reload trang
  useEffect(() => {
    const lastChatId = localStorage.getItem("chatId");
    if (lastChatId && isLoggedIn) {
      console.log("Reloading last chat:", lastChatId);
      handleSelectChat(lastChatId);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn]); // Chỉ chạy khi trạng thái đăng nhập được xác định

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
    showToast('Đã đăng xuất thành công!', 'success');
    // give user a brief moment to see the toast before reloading
    setTimeout(() => window.location.reload(), 700);
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
    setIsNewChat(true);
    setCurrentChatId(null);
    setContent("");
    setMessages([]);
    localStorage.removeItem("chatId");
    console.log("Ready for new chat");
  }

  // Chọn cuộc trò chuyện và load messages từ backend
  const handleSelectChat = async (chatId) => {
    setCurrentChatId(chatId);
    setIsNewChat(false);
    localStorage.setItem("chatId", chatId);

    const currentChatFromHistory = chatHistory.find(chat => chat.id === chatId);

    // Nếu messages đã được load trước đó, dùng cache
    if (currentChatFromHistory && currentChatFromHistory.messages && currentChatFromHistory.messages.length > 0) {
      console.log("Loading messages from cache for chat ID:", chatId);
      const sortedMessages = [...currentChatFromHistory.messages].sort(
        (a, b) => {
          const dateA = new Date(a.timestamp);
          const dateB = new Date(b.timestamp);
          if (dateA.getTime() !== dateB.getTime()) {
            return dateA.getTime() - dateB.getTime();
          }
          return (a.bot ? 1 : 0) - (b.bot ? 1 : 0); // Ưu tiên tin nhắn người dùng (bot: false) trước bot (bot: true)
        }
      );

      setMessages(sortedMessages);
    } else {
      setMessages([]); // Xóa tin nhắn cũ để hiển thị trạng thái loading
      console.log("Fetching messages from backend for chatId:", chatId);
      // Load messages từ backend
      try {
        const response = await getMessages(localStorage.getItem("token"), chatId);
        console.log("Messages loaded:", response);

        if (response && response.results) {
          const transformedMessages = response.results
            .map((msg) => ({
              id: msg.id,
              bot: msg.bot,
              content: msg.content,
              timestamp: msg.createdAt,
            }));
          const sortedTransformedMessages = transformedMessages.sort((a, b) => {
            const dateA = new Date(a.timestamp);
            const dateB = new Date(b.timestamp);
            if (dateA.getTime() !== dateB.getTime()) {
              return dateA.getTime() - dateB.getTime();
            }
            return (a.bot ? 1 : 0) - (b.bot ? 1 : 0); // Ưu tiên tin nhắn người dùng (bot: false) trước bot (bot: true)
          });
          console.log("Messages from backend (sorted):", sortedTransformedMessages.map(m => ({ id: m.id, ts: m.timestamp, content: m.content.substring(0, 30) + "..." })));

          setMessages(sortedTransformedMessages);

          // Cập nhật cache trong chatHistory
          setChatHistory(prev => prev.map(chat =>
            chat.id === chatId
              ? { ...chat, messages: sortedTransformedMessages }
              : chat
          ));
        }
      } catch (error) {
        console.error("Error loading messages:", error);
        setMessages([]);
      }
    }
    // Cuộn xuống dưới cùng sau khi tin nhắn được tải
    // Sử dụng setTimeout để đảm bảo DOM đã được cập nhật
    setTimeout(() => {
      scrollToBottom();
    }, 0);
  }

  // Xóa cuộc trò chuyện
  const handleDeleteChat = async (e, chatId) => {
    e.stopPropagation(); // Ngăn chặn event bubble

    try {
      const response = await deleteConversation(localStorage.getItem("token"), chatId);
      console.log("Delete conversation result:", response);

      // Cập nhật UI
      const updatedHistory = chatHistory.filter(chat => chat.id !== chatId);
      setChatHistory(updatedHistory);

      if (currentChatId === chatId) {
        setCurrentChatId(null);
        setMessages([]);
        setContent("");
        setIsNewChat(true);
        localStorage.removeItem("chatId");
      }
    } catch (error) {
      console.error("Error deleting conversation:", error);
        showToast('Không thể xóa cuộc trò chuyện. Vui lòng thử lại!', 'error');
    }
  }

  const handleValidate = () => {
    if (!isLoggedIn) {
      showToast('Vui lòng đăng nhập để sử dụng chức năng này.', 'error');
      setIsLoginModalOpen(true);
      return false;
    }
    return true;
  }

  // eslint-disable-next-line no-unused-vars
  const { speak } = useSpeechContext()

const handleSearch = async (overrideMessage) => {
  if (!handleValidate()) {
    return;
  }

  const messageContent = overrideMessage ?? content;
  // Kiểm tra content không rỗng
  if (!messageContent || messageContent.trim() === "") {
    return;
  }

  let chatId = currentChatId || localStorage.getItem("chatId");
  console.log("isNewChat:", isNewChat, "chatId:", chatId);

  if (isNewChat || !chatId) {
    console.log("Creating new chat...");
    const response = await createNewChat(localStorage.getItem("token"), messageContent);
    
    if (response && response.results && response.results.id) {
      chatId = response.results.id;
      localStorage.setItem("chatId", chatId);
      setCurrentChatId(chatId);
      console.log("New chat created with ID:", chatId);
      setIsNewChat(false);

      // Thêm conversation mới vào chatHistory
      const newChat = {
        id: chatId,
        name: response.results.name || "Cuộc trò chuyện mới",
        createdAt: response.results.createdAt,
        messages: []
      };
      setChatHistory(prev => [newChat, ...prev].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
    } else {
      console.error("Failed to create new chat, response:", response);
      return;
    }
  }

  // Hiển thị tin nhắn của người dùng ngay lập tức
  const tempUserMessage = {
    id: `temp-${Date.now()}`, // ID tạm thời
    bot: false,
    content: messageContent,
    timestamp: new Date().toISOString(),
  };
  setMessages(prev => [...prev, tempUserMessage]); 
  setTimeout(() => {
    scrollToBottom();
  }, 0);

  // Xóa nội dung input và tắt trạng thái gõ phím
  setContent("");
  setIsTyping(false);

  console.log("Before sendMessage - chatId:", chatId, "messageContent:", messageContent);

  const response = await sendMessage(localStorage.getItem("token"), chatId, messageContent);
  console.log("sendMessage result:", response);

  // Thêm cả user message và bot response vào UI
  if (response && response.results) {
    const finalUserMessage = {
      id: response.results.userMessage.id,
      bot: false,
      content: response.results.userMessage.content,
      timestamp: response.results.userMessage.createdAt
    };

    const botResponseMessage = {
      id: response.results.botMessage.id,
      bot: true,
      content: response.results.botMessage.content || 'Xin lỗi, tôi không có câu trả lời.',
      timestamp: response.results.botMessage.createdAt
    };

    // Cập nhật tin nhắn: xóa tin nhắn tạm, thêm user message và bot message theo đúng thứ tự
    setMessages(prevMessages => {
      const messagesWithoutTemp = prevMessages.filter(msg => msg.id !== tempUserMessage.id);
      const combinedMessages = [...messagesWithoutTemp, finalUserMessage, botResponseMessage];
      return combinedMessages.sort((a, b) => {
        const dateA = new Date(a.timestamp);
        const dateB = new Date(b.timestamp);
        if (dateA.getTime() !== dateB.getTime()) {
          return dateA.getTime() - dateB.getTime();
        }
        return (a.bot ? 1 : 0) - (b.bot ? 1 : 0); // Ưu tiên tin nhắn người dùng (bot: false) trước bot (bot: true)
      });
    });
    
    setTimeout(() => {
      scrollToBottom();
    }, 0);

    // Cập nhật chat history với messages mới
    setChatHistory(prev => prev.map(chat => {
      if (chat.id === chatId) {
        const updatedMessages = [...(chat.messages || []), finalUserMessage, botResponseMessage].sort((a, b) => {
          const dateA = new Date(a.timestamp);
          const dateB = new Date(b.timestamp);
          if (dateA.getTime() !== dateB.getTime()) { return dateA.getTime() - dateB.getTime(); }
          return (a.bot ? 1 : 0) - (b.bot ? 1 : 0);
        });
        return { ...chat, messages: updatedMessages };
      }
      return chat;
    }));
    console.log("Updated chat history for chat ID:", chatId);
  } else {
    // Nếu có lỗi, xóa tin nhắn tạm của người dùng
    setMessages(prev => prev.filter(msg => msg.id !== tempUserMessage.id));
    console.error("Failed to send message, response:", response);
  }
}


  return (
    <div className="flex h-screen bg-[var(--bg)] text-[var(--text)]">
      <SideBar handleNewChat={handleNewChat} chatHistory={chatHistory} currentChatId={currentChatId} handleSelectChat={handleSelectChat} handleDeleteChat={handleDeleteChat}></SideBar>

      <div className="flex-1 flex flex-col">
        <Header dropdownRef={dropdownRef} isDropdownOpen={isDropdownOpen} handleUserIconClick={handleUserIconClick} isLoggedIn={isLoggedIn} handleLoginClick={handleLoginClick} handleLogout={handleLogout}></Header>

        <div className='flex-1 flex flex-col overflow-hidden'>
          <div className='flex-1 overflow-y-auto hide-scrollbar py-10 px-4'>
            {messages.length === 0 ? (
              <div className='flex justify-center items-center h-full'>
                <span className="ombre-color text-2xl">Can i help you, sir!</span>
              </div>
            ) : (

              <div className='max-w-3xl mx-auto space-y-4'>
                {messages.map((message) => (
                  <MessageCard key={message.id} message={message} />
                ))}
                <div ref={messagesEndRef} />
              </div>
            )}
          </div>

          <InputBox
            content={content}
            handleChange={handleChange}
            isTyping={isTyping}
            handleSearch={handleSearch}
          />
          
        </div>
      </div>

      <LoginModal isOpen={isLoginModalOpen} onClose={handleCloseLoginModal} onSwitchToRegister={handleSwitchToRegister} />
      <RegisterModal isOpen={isRegisterModalOpen} onClose={handleCloseRegisterModal} onSwitchToLogin={handleSwitchToLogin} />
    </div>
  )
}

export default App
