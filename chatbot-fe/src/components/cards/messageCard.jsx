
import { useSpeechContext } from '../../contexts/SpeechContext'

export default function MessageCard({ message }) {
    const { speak, stopSpeaking, playingMessageId } = useSpeechContext()

    const isThisPlaying = playingMessageId !== null && playingMessageId === message.id

    const handlePlay = () => {
      if (isThisPlaying) stopSpeaking()
      else speak(message.content, { messageId: message.id })
    }

    return (
        <div
            key={message.id}
            className={`flex ${message.bot === false ? 'justify-end' : 'justify-start'}`}
        >
            <div
                className={`max-w-[70%] rounded-2xl px-4 py-3 ${message.bot === false
                    ? 'bg-blue-500 text-white'
                    : 'message-bubble-bot'
                    }`}
            >
                <div className='flex items-start gap-3'>
                  <p className='whitespace-pre-wrap break-words flex-1'>{message.content}</p>
                  {message.bot && (
                    <button onClick={handlePlay} className='ml-2 p-1 rounded-full message-play-btn' title={isThisPlaying ? 'Dừng nói' : 'Nghe'}>
                      <i className={`fa-solid ${isThisPlaying ? 'fa-stop' : 'fa-volume-high'}`}></i>
                    </button>
                  )}
                </div>

            </div>
        </div>
    );
}