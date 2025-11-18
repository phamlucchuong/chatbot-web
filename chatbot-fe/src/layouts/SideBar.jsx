import ConversationCard from "../components/cards/ConversationCard";


export default function SideBar({ handleNewChat, chatHistory, currentChatId, handleSelectChat, handleDeleteChat }) {
    return (
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
                <span className="block mb-3">Gần đây</span>
                {chatHistory.length === 0 ? (
                    <div className="p-4 text-center text-gray-500 text-sm">
                        <i className="fa-regular fa-comments text-2xl mb-2 block"></i>
                        <p>Chưa có lịch sử trò chuyện</p>
                    </div>
                ) : (
                    <div className="px-2">
                        {chatHistory.map((chat) => (
                            <ConversationCard
                                key={chat.id}
                                chat={chat}
                                currentChatId={currentChatId}
                                handleSelectChat={handleSelectChat}
                                handleDeleteChat={handleDeleteChat}
                            />
                        ))}
                    </div>
                )}
            </div>
        </aside>
    );

}