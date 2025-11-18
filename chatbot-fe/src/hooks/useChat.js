import { 
    createChat,
    fetchChat, 
    sendMessageApi,
    getMessagesApi,
    deleteConversationApi,
    updateConversationNameApi
} 
    from "../api/appApi";


export const useChat = () => {
    const fetchChatHistory = async (token) => {
        try {
            return await fetchChat(token);
        } catch (e) {
            console.log("Lỗi lấy lịch sử chat:", e.message);
            return null;
        };
    }

    const createNewChat = async (token, name) => {
        try {
            return await createChat(token, name);
        } catch (e) {
            console.log("Lỗi tạo chat mới:", e.message);
            return null;
        }
    }

    const sendMessage = async (token, conversation_id, message) => {
        try {
            return await sendMessageApi(token, conversation_id, message);
        } catch (e) {
            console.log("Lỗi gửi tin nhắn:", e.message);
            return null;
        }
    }

    const getMessages = async (token, conversation_id) => {
        try {
            return await getMessagesApi(token, conversation_id);
        } catch (e) {
            console.log("Lỗi lấy tin nhắn:", e.message);
            return null;
        }
    }

    const updateConversationName = async (token, conversation_id, name) => {
        try {
            return await updateConversationNameApi(token, conversation_id, name);
        } catch (e) {
            console.log("Lỗi xóa conversation:", e.message);
            return null;
        }
    }

    const deleteConversation = async (token, conversation_id) => {
        try {
            return await deleteConversationApi(token, conversation_id);
        } catch (e) {
            console.log("Lỗi xóa conversation:", e.message);
            return null;
        }
    }

    return { 
        fetchChatHistory, 
        createNewChat, 
        sendMessage, 
        getMessages,
        updateConversationName,
        deleteConversation 
    };
}