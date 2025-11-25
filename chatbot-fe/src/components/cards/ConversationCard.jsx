import { formatDate } from "../../utils/dateUtil";


export default function ConversationCard({ chat, currentChatId, handleSelectChat, handleDeleteChat, compact = false }) {
    console.log(
        "chat id:", chat.id, 
        "\nchat name:", chat.name,
        "\nchat createdAt:", chat.createdAt
    );
    
    if (compact) {
        return (
            <div onClick={() => handleSelectChat(chat.id)} title={chat.name} className={`group relative p-2 mb-2 rounded-lg cursor-pointer transition-colors flex items-center justify-center ${currentChatId === chat.id ? 'bg-[#3A3B3C]' : 'hover:bg-[#2E2F30]'}`}>
                <i className="fa-regular fa-message text-gray-300 text-lg"></i>
            </div>
        )
    }

    return (
        <div
            key={chat.id}
            onClick={() => handleSelectChat(chat.id)}
            className={`group relative bg-[#262628] p-3 mb-3 rounded-lg cursor-pointer transition-colors ${currentChatId === chat.id
                ? "bg-[#262628] border border-blue-300"
                : "hover:border-gray-600 border border-transparent"
                }`}
        >
            <div className="flex items-start justify-between gap-2">
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                        <i className="fa-regular fa-message text-gray-500 text-xs"></i>
                        <p className="text-sm font-medium text-[#C0C1C1] truncate">
                            {chat.name}
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
    );
}