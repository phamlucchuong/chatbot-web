// app api

export async function createChat(token) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${token}`,
                }
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi gửi yêu cầu search:", error);
    }

}


export async function fetchChat() {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/history`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    authorization: `Bearer ${localStorage.getItem("token")}`,
                }
            }
        );
        return response.json();
    } catch (error) {
        console.error("Lỗi khi gửi yêu cầu fetch search:", error);
    }

}



export async function sendMessageApi(token, conversation_id, content) {
    try {
        const response = await fetch(
            `http://localhost:8080/healthcare/api/search/${conversation_id}/message`,
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
        console.error("Lỗi khi gửi yêu cầu search:", error);
    }

}