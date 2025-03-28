import { useEffect, useRef, useState } from "react";
import {Message, NLURequest} from "../types/types";
import {FaPaperPlane} from "react-icons/fa";
import { FaDownload } from "react-icons/fa6";
import ChatMessage from "./ChatMessage.tsx";
import { v4 as randomUUID } from 'uuid';
import axios from 'axios';
import FivePhaseInfo from "./FivePhaseInfo.tsx";

export default function ChatWindow() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [savedMessages, setSavedMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState("");
  const [, setSocket] = useState<WebSocket | null>(null);
  const endOfChat = useRef<HTMLDivElement | null>(null);
  const totalTime = 60*15;
  const [timeRemaining, setTimeRemaining] = useState(totalTime);
  const [finishSession, setFinishSession] = useState(false);
  const [proceedButtonActive, setProceedButtonActive] = useState(false);  

  useEffect(() => {
      endOfChat.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);
  
  useEffect(() => {
    const timerInterval = setInterval(() => {
      setTimeRemaining((prevTime) => {
        if (prevTime === 0) {
          setFinishSession(true);
          clearInterval(timerInterval);
          console.log('Countdown complete!');
          return 0;
        } else {
          return prevTime - 1;
        }
      });
    }, 1000);

    return () => clearInterval(timerInterval);
  }, []);

  useEffect(() => {
    console.log("WEBSOCKET TIME.");
    const ws = new WebSocket("ws://lilobottest.ewi.tudelft.nl/nlg/ws");
    setSocket(ws);
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("Received message:", data.message);
      console.log("REM:", data.sessionid, "LOC:", localStorage.getItem('bdi_session_id'));
      if (data.sessionid === localStorage.getItem("bdi_session_id")) {
        const receivedMessage: Message = {
          id: randomUUID(),
          text: data.message,
          type: "Received",
        };
        setMessages((prevMessages) => [...prevMessages, receivedMessage]);
      }
    };
    ws.onerror = (error) => {
      console.error("WebSocket Error:", error);
    };
    ws.onclose = () => {
      console.log("WebSocket disconnected");
    };
    return () => {
      ws.close();
    };
  }, []);

  async function downloadMessages(){
    setProceedButtonActive(true);
    logChat();
    const json = JSON.stringify(savedMessages, null, 2);
    const blob = new Blob([json], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = localStorage.getItem('username') + "-l.json";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  async function handleMessageSend(message: string) {
    if (!message.trim() || !localStorage.getItem("bdi_session_id")) return;
    const newMessage: Message = {
      id: randomUUID(),
      text: message,
      type: "Sent",
    };
    setMessages((prev) => [...prev, newMessage]);
    try {
      await callNLUService(message);
    } catch (error) {
      console.error("Error sending message:", error);
    }
  }

  async function callNLUService(message: string) {
    const body: NLURequest = {
      utterance: message,
      sessionid: localStorage.getItem("bdi_session_id"),
    };

    try {
      const response = await fetch('http://lilobottest.ewi.tudelft.nl/nlu/intent_recognition', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Request failed with status:', response.status, response.statusText);
        console.error('Error response:', errorText);
      } else {
        const result = await response.json();
        console.log('Received result:', result);
      }
    } catch (error) {
      console.error('Error while calling NLU service:', error);
    }
  }

  async function logChat() {
    setSavedMessages((prevSavedMessages) => [...prevSavedMessages, ...messages]);
  }

  async function setUpNewSession() {
    try {
      logChat();
      setMessages([]);
      const newSessionId = randomUUID();
      console.log("SESSION ID:", newSessionId);
      localStorage.setItem('bdi_session_id', newSessionId);
      const username = localStorage.getItem('username');

      console.log("Making request to:", `http://lilobottest.ewi.tudelft.nl/bdi/create/${newSessionId}`);
      console.log("With username:", username);

      await axios.post(`http://lilobottest.ewi.tudelft.nl/bdi/create/${newSessionId}`, null, {
        params: {username: username}
      }).then(response => {
        console.log("Success response:", response.data);
      }).catch(error => {
        // More detailed error logging
        console.error('Detailed error:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
    });
    } catch (error) {
      console.error('Error setting up BDI session:', error);
      console.error('Failed to initialize chat session');
    }
  }

  async function handleRedirect(){

    const conditionOrder = localStorage.getItem("CONDITION_ORDER"); // Get CONDITION_ORDER from localStorage

    const baseurl = conditionOrder === "0" 
      ? "https://tudelft.fra1.qualtrics.com/jfe/form/SV_2bn62quuAYBcr6C"
      : "https://tudelft.fra1.qualtrics.com/jfe/form/SV_00djFCzGYOng4nA";  
 
    const params = `?PROLIFIC_PID=${localStorage.getItem("PROLIFIC_PID")}&CONDITION_ORDER=${localStorage.getItem("CONDITION_ORDER")}`;    
    const redirecturl = baseurl + params;
    window.location.href = redirecturl;
  }

  const minutes = Math.floor((timeRemaining % 3600) / 60);
  const seconds = timeRemaining % 60;

  return (
    <div className="w-screen h-screen flex flex-row justify-around items-center bg-purple-600">
      <FivePhaseInfo/>
      <div className="flex flex-col w-2/5 h-4/5 bg-slate-200 rounded-xl">
        {/* HEADER */}
        <div className="flex w-full h-16 flex-shrink-0 bg-slate-300 rounded-t-xl flex flex-row justify-between items-center p-5">
          <div className="text-2xl text-purple-900">{minutes}m {seconds}s</div>
        <div className="flex flex-row gap-4 items-center">
            <button
                className={`flex aspect-square h-10 items-center justify-center rounded-full ${
                    finishSession ? "bg-purple-500" : "bg-purple-200 cursor-not-allowed text-slate-500"
                }`}
                onClick={downloadMessages}
                disabled={!finishSession}
            >
                <FaDownload className={finishSession ? "" : "cursor-not-allowed"}/>
            </button>
            <button
                className="flex aspect-square h-10 items-center justify-center rounded-full px-2 text-white bg-purple-500"
                onClick={setUpNewSession}
            >
                Reset
            </button>
        </div>
        </div>
        {/* MESSAGES */}
        {finishSession ? 
 
          <div className="flex flex-col w-full flex-grow p-6 text-purple-900 gap-4 justify-center items-center">
             <p className="text-2xl">Thank you for completing the task!</p>
             <p className="font-bold text-xl">IMPORTANT: Please follow the following instructions:</p>
             <p>1. Use the download button above to save your chat data so you can upload it</p>
             <p>2. Click the button below to proceed to the next step in the experiment</p>
             <button className="bg-purple-500 text-white text-xl p-4 rounded-lg" onClick={handleRedirect} disabled={!proceedButtonActive}> Proceed </button>
          </div>
          :
          <div className="flex flex-col w-full flex-grow p-4 gap-1 overflow-y-scroll">
            {messages.map((msg) => (
              <ChatMessage key={msg.id} message={msg} />
            ))}
            <div ref={endOfChat} />
          </div>
        }
        {/* INPUT */}
        <div className="flex w-full h-16 flex-shrink-0 items-center px-5 gap-5 bg-slate-300 rounded-b-xl">
          <div className="flex grow h-2/3 px-6 rounded-full bg-white">
            <input
              className="flex grow focus:outline-none"
              disabled={finishSession}
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  handleMessageSend(inputText);
                  setInputText("");
                }
              }}
              placeholder="Type a message..."
            />
          </div>
          <button
            className="flex aspect-square h-10 items-center justify-center rounded-full bg-slate-500"
            onClick={() => {
              handleMessageSend(inputText);
              setInputText("");
            }}
          >
            <FaPaperPlane />
          </button>
        </div>
      </div>
    </div>
  );
}
