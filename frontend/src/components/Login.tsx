import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {authService} from '../services/authService.ts';
import axios from "axios";
import { v4 as randomUUID } from 'uuid';

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [code, ] = useState("HyP$jdIHV$zK5#2X");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const location = useLocation(); // React Router's hook to access the current URL

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const prolificPID = params.get("PROLIFIC_PID");
    const conditionOrder = params.get("CONDITION_ORDER");

    if (prolificPID) {
      localStorage.setItem("PROLIFIC_PID", prolificPID);
      setUsername(prolificPID);
      setEmail(prolificPID + "@lilobottest.ewi.tudelft.nl");
      setPassword(randomUUID());
    }
    if (conditionOrder) {
      localStorage.setItem("CONDITION_ORDER", conditionOrder);
    }
  }, [location.search]);

  function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async function handleSubmit() {
    try {
      await authService.register(username, password, email, code);
      await delay(1000);
      await authService.login(username, password);
      await setUpBDI();
      console.log("NAVIGATING TO MAIN");      
      navigate("/main");
    } catch (error) {
      console.error('Auth error:', error);
    }
  }

  async function setUpBDI() {
  try {
    const sessionId = randomUUID(); 
    console.log("SESSION ID:", sessionId);
    localStorage.setItem('bdi_session_id', sessionId);
    
    // Add logging for request details
    console.log("Making request to:", `http://lilobottest.ewi.tudelft.nl/bdi/create/${sessionId}`);
    console.log("With username:", username);
    
    await axios.post(`http://lilobottest.ewi.tudelft.nl/bdi/create/${sessionId}`, null, {
      params: { username: username }
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
    setError('Failed to initialize chat session');
  }
}

  return (
      <div className="w-screen h-screen flex justify-center items-center bg-purple-600">
        <div className="flex flex-col w-1/5 justify-center items-center bg-slate-200 p-6 rounded-xl gap-3">
          <p className="text-purple-800"> Click start to interacting chatting with the virtual child!</p>
          <button
              className="bg-white p-2 rounded-lg"
              onClick={handleSubmit}
          >
            Start
          </button>
          {error && <p className="text-red-500">{error}</p>}
        </div>
      </div>
  );
}
