import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { collection, addDoc, query, orderBy, onSnapshot, serverTimestamp, getDocs, deleteDoc, doc } from 'firebase/firestore';
import { db } from './firebase';
import './App.css';

const CHAT_COLLECTION = 'chatMessages';
const API_GATEWAY_URL = process.env.REACT_APP_API_GATEWAY_URL || 'https://gateway-group2-basar.azure-api.net';
const MAX_REQUESTS_PER_SESSION = 15;
const RATE_LIMIT_KEY = 'tuition_chat_requests';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [requestCount, setRequestCount] = useState(0);
  const [isBlocked, setIsBlocked] = useState(false);
  const chatEndRef = useRef(null);

  // Clear chat history and reset rate limit on page load
  useEffect(() => {
    const initializeChat = async () => {
      // Clear all previous messages from Firestore
      await clearChatHistory();

      // Reset rate limit counter for new session
      const savedCount = parseInt(sessionStorage.getItem(RATE_LIMIT_KEY) || '0');
      setRequestCount(savedCount);
      if (savedCount >= MAX_REQUESTS_PER_SESSION) {
        setIsBlocked(true);
      }

      // Add welcome message
      await addWelcomeMessage();
    };

    initializeChat();
  }, []);

  // Subscribe to real-time updates
  useEffect(() => {
    const q = query(collection(db, CHAT_COLLECTION), orderBy('timestamp', 'asc'));

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const loadedMessages = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setMessages(loadedMessages);
    });

    return () => unsubscribe();
  }, []);

  // Clear all chat messages from Firestore
  const clearChatHistory = async () => {
    try {
      const q = query(collection(db, CHAT_COLLECTION));
      const snapshot = await getDocs(q);
      const deletePromises = snapshot.docs.map(docSnapshot =>
        deleteDoc(doc(db, CHAT_COLLECTION, docSnapshot.id))
      );
      await Promise.all(deletePromises);
    } catch (error) {
      console.error("Error clearing chat history:", error);
    }
  };

  const addWelcomeMessage = async () => {
    const welcomeMsg = {
      text: "Hello! I am your University Tuition Assistant. How can I help you today?\n\nYou can ask me to:\n• Check tuition balance (e.g., 'Check my tuition for student 2023001')\n• Pay tuition (e.g., 'Pay 1000 for term 2025-SUMMER, student 2023001')\n• View unpaid tuitions (e.g., 'Show unpaid tuitions for 2025-SUMMER')",
      sender: "ai",
      timestamp: serverTimestamp(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    await addDoc(collection(db, CHAT_COLLECTION), welcomeMsg);
  };

  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(scrollToBottom, [messages]);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;

    // Check rate limit
    if (requestCount >= MAX_REQUESTS_PER_SESSION) {
      setIsBlocked(true);
      const blockedMsg = {
        text: "⚠️ You have reached the maximum limit of 15 requests per session. Please refresh the page to start a new session.",
        sender: "ai",
        timestamp: serverTimestamp(),
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      await addDoc(collection(db, CHAT_COLLECTION), blockedMsg);
      return;
    }

    const userTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const userMsg = {
      text: input,
      sender: "user",
      timestamp: serverTimestamp(),
      time: userTime
    };

    // Save user message to Firestore
    await addDoc(collection(db, CHAT_COLLECTION), userMsg);
    setInput("");
    setLoading(true);

    // Increment rate limit counter
    const newCount = requestCount + 1;
    setRequestCount(newCount);
    sessionStorage.setItem(RATE_LIMIT_KEY, newCount.toString());

    try {
      // Call API Gateway which routes to backend
      const response = await axios.post(`${API_GATEWAY_URL}/api/v1/ai/chat`, {
        message: input
      }, {
        headers: { "Content-Type": "application/json" },
        timeout: 30000
      });

      const aiTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      const aiMsg = {
        text: response.data,
        sender: "ai",
        timestamp: serverTimestamp(),
        time: aiTime
      };

      // Save AI response to Firestore (will trigger real-time update)
      await addDoc(collection(db, CHAT_COLLECTION), aiMsg);
    } catch (error) {
      console.error("API Error:", error);
      const errorMsg = {
        text: "I'm sorry, I am unable to process your request right now. Please try again later.",
        sender: "ai",
        timestamp: serverTimestamp(),
        time: "Error"
      };
      await addDoc(collection(db, CHAT_COLLECTION), errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <div className="chat-card">
        <header className="chat-header">
          <div className="ai-status">
            <div className="avatar">AI</div>
            <div>
              <h3>Tuition Assistant</h3>
              <p className="online-tag">
                {isBlocked ? '🔴 Rate Limited' : `🟢 Online (${MAX_REQUESTS_PER_SESSION - requestCount} requests left)`}
              </p>
            </div>
          </div>
        </header>

        <main className="chat-body">
          {messages.map((msg) => (
            <div key={msg.id || msg.timestamp} className={`message-row ${msg.sender}`}>
              <div className="bubble">
                {msg.text}
                <span className="msg-time">{msg.time || 'Now'}</span>
              </div>
            </div>
          ))}
          {loading && <div className="message-row ai"><div className="bubble typing">...</div></div>}
          <div ref={chatEndRef} />
        </main>

        <footer className="chat-footer">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
            placeholder={isBlocked ? "Rate limit reached. Refresh page to continue." : "Type your student ID or question..."}
            disabled={isBlocked}
          />
          <button onClick={sendMessage} disabled={!input.trim() || isBlocked}>Send</button>
        </footer>
      </div>
    </div>
  );
}

export default App;