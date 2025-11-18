import { 
    createChat,
    fetchChat, 
    sendMessageApi
} 
    from "../api/appApi";


export const useChat = () => {
    const fetchChatHistory = async (formData) => {
        try {
            return await fetchChat(formData);
        } catch (e) {
            console.log("Lỗi đăng ký:", e.message);
            return null;
        };
    }

    const createNewChat = async (token) => {
        try {
            return await createChat(token);
        } catch (e) {
            console.log("Lỗi tìm kiếm:", e.message);
            return null;
        }
    }

    const sendMessage = async (token, conversation_id, message) => {
        try {
            // Giả sử có hàm sendMessageToApi để gửi tin nhắn đến API
            return await sendMessageApi(token, conversation_id, message);
        } catch (e) {
            console.log("Lỗi gửi tin nhắn:", e.message);
            return null;
        }
    }



    return { fetchChatHistory, createNewChat, sendMessage };
}