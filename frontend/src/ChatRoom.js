import React, { useState, useEffect, useRef } from 'react';
import { sendMessage, subscribeToMessages } from '../services/chatService';

const ChatRoom = () => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const messagesEndRef = useRef(null); // Otomatik scroll için

    // Sayfa açıldığında mesajları dinlemeye başla
    useEffect(() => {
        const unsubscribe = subscribeToMessages((data) => {
            setMessages(data);
            // Yeni mesaj geldiğinde en alta kaydır
            messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
        });

        // Sayfadan çıkıldığında dinlemeyi durdur (Cleanup)
        return () => unsubscribe();
    }, []);

    const handleSend = async (e) => {
        e.preventDefault();
        if (!newMessage.trim()) return;

        try {
            await sendMessage(newMessage, "user");
            setNewMessage(""); // Input'u temizle
        } catch (error) {
            alert("Mesaj gönderilemedi!");
        }
    };

    return (
        <div className="chat-container">
            {/* Mesaj Listesi */}
            <div className="messages-list" style={{ height: '400px', overflowY: 'auto', border: '1px solid #ddd', padding: '10px' }}>
                {messages.map((msg) => (
                    <div key={msg.id} style={{ textAlign: msg.sender === 'user' ? 'right' : 'left', margin: '5px' }}>
                        <div style={{
                            display: 'inline-block',
                            padding: '8px 12px',
                            borderRadius: '10px',
                            background: msg.sender === 'user' ? '#007bff' : '#f1f0f0',
                            color: msg.sender === 'user' ? '#fff' : '#000'
                        }}>
                            <div>{msg.text}</div>
                            <small style={{ fontSize: '0.7em', opacity: 0.8 }}>{msg.time}</small>
                        </div>
                    </div>
                ))}
                <div ref={messagesEndRef} />
            </div>

            {/* Mesaj Gönderme Formu */}
            <form onSubmit={handleSend} style={{ marginTop: '10px', display: 'flex' }}>
                <input
                    type="text"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Mesajınızı yazın..."
                    style={{ flex: 1, padding: '10px' }}
                />
                <button type="submit" style={{ padding: '10px 20px' }}>Gönder</button>
            </form>
        </div>
    );
};

export default ChatRoom;