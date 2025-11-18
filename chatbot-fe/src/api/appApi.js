// app api

export async function createChat(token, name) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                },
                body: JSON.stringify( name )
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi gửi yêu cầu search:", error);
    }
}


export async function fetchChat(token) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/history`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                }
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi gửi yêu cầu fetch search:", error);
    }
}


export async function getMessagesApi(token, conversation_id) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/${conversation_id}/messages`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                }
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi lấy messages:", error);
    }
}


export async function sendMessageApi(token, conversation_id, content) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/${conversation_id}/chat`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ content })
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi gửi yêu cầu chat:", error);
    }
}


export async function updateConversationNameApi(token, conversation_id, name) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/${conversation_id}`,
            {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ name })
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi xóa conversation:", error);
    }
}

export async function deleteConversationApi(token, conversation_id) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/${conversation_id}`,
            {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                }
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi xóa conversation:", error);
    }
}