


export default function MessageCard({ message }) {
    return (
        <div
            key={message.id}
            className={`flex ${message.bot === false ? 'justify-end' : 'justify-start'}`}
        >
            <div
                className={`max-w-[70%] rounded-2xl px-4 py-3 ${message.bot === false
                    ? 'bg-blue-500 text-white'
                    : 'bg-[#262628] text-gray-800'
                    }`}
            >
                <p className='whitespace-pre-wrap break-words'>{message.content}</p>
                {/* <p className={`text-xs mt-1 ${message.bot === false ? 'text-blue-100' : 'text-gray-500'
                    }`}>
                    {
                        // Nếu message.createdAt là chuỗi, code tối ưu sẽ là:
                        new Date(message.createdAt).toLocaleTimeString('vi-VN', {
                            hour: '2-digit',
                            minute: '2-digit'
                        })
                    }
                </p> */}
            </div>
        </div>
    );
}