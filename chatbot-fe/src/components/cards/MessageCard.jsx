import HospitalMap from '../HospitalMap'

export default function MessageCard({ message }) {

    // Parse message content để kiểm tra có phải hospital map không
    let mapData = null;
    let displayContent = message.content;
    
    try {
        const parsed = JSON.parse(message.content);
        if (parsed && parsed.type === 'hospital_map' && Array.isArray(parsed.hospitals)) {
            mapData = parsed;
        }
    } catch (e) {
        // Không phải JSON, hiển thị text bình thường
    }

    return (
        <div
            key={message.id}
            className={`flex ${message.bot === false ? 'justify-end' : 'justify-start'}`}
        >
            <div
                className={`${mapData ? 'max-w-[90%]' : 'max-w-[70%]'} rounded-2xl px-4 py-3 ${
                    message.bot === false
                        ? 'bg-blue-500 text-white'
                        : 'message-bubble-bot'
                }`}
            >
                <div className='flex items-start gap-3'>
                    {mapData ? (
                        // Hiển thị bản đồ bệnh viện
                        <div className='flex-1'>
                            {/* Text mô tả (nếu có) */}
                            {mapData.text && (
                                <p className='whitespace-pre-wrap break-words mb-3'>
                                    {mapData.text}
                                </p>
                            )}
                            
                            {/* Component bản đồ */}
                            <HospitalMap hospitals={mapData.hospitals} />
                        </div>
                    ) : (
                        // Hiển thị text bình thường
                        <p className='whitespace-pre-wrap break-words flex-1'>
                            {displayContent}
                        </p>
                    )}
                </div>
            </div>
        </div>
    );
}