import {Message} from "../types/types.ts";

export default function ChatMessage({ message }: { message: Message }) {
  return (
    <div
      className={`flex ${
        message.type === "Sent" ? "justify-end" : "justify-start"
      }`}
    >
      <div
        className={`inline-block p-3 rounded-t-2xl max-w-[50%] break-words ${
          message.type === "Sent" ? "bg-blue-500 rounded-bl-2xl" : "bg-gray-600 rounded-br-2xl"
        } text-white`}
      >
        {message.text}
      </div>
    </div>
  );
}
