import { db } from "../firebase"; // Yukarıda oluşturduğumuz dosya
import axios from "axios";
import {
    collection,
    addDoc,
    serverTimestamp,
    query,
    orderBy,
    onSnapshot
} from "firebase/firestore";

// Mesaj Gönderme Fonksiyonu
export const sendMessage = async (text, sender = "user") => {
    try {
        // Şu anki saati formatla (Rehberindeki "14:30" formatı için)
        const now = new Date();
        const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        // 1. Mesajı Firestore'a kaydet (User mesajı)
        await addDoc(collection(db, "chatMessages"), {
            text: text,
            sender: sender,
            timestamp: serverTimestamp(), // Sunucu saati (sıralama için)
            time: timeString // Görüntülenecek saat (string)
        });

        // 2. Eğer gönderen kullanıcı ise, AI Agent'ı tetikle
        if (sender === 'user') {
            const gatewayUrl = process.env.REACT_APP_API_GATEWAY_URL || 'https://gateway-group2-basar.azure-api.net';

            // Backend'e isteği gönder (API Gateway üzerinden)
            // Not: Yanıtı beklemiyoruz, çünkü backend yanıtı ürettiğinde Firestore'a yazacak 
            // ve biz onSnapshot ile otomatik alacağız.
            // ANCAK: Mevcut backend implementation return ediyor, ve kimse Firestore'a yazmıyor.
            // Bu yüzden backend yanıtını alıp bizim yazmamız lazım.

            const response = await axios.post(`${gatewayUrl}/api/v1/ai/chat`, {
                message: text
            });

            // 3. AI Yanıtını Firestore'a kaydet
            if (response.data) {
                await addDoc(collection(db, "chatMessages"), {
                    text: typeof response.data === 'string' ? response.data : JSON.stringify(response.data),
                    sender: "ai",
                    timestamp: serverTimestamp(),
                    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                });
            }
        }
    } catch (error) {
        console.error("Mesaj gönderilirken hata oluştu:", error);
        throw error;
    }
};

// Mesajları Dinleme Fonksiyonu (Real-time)
export const subscribeToMessages = (callback) => {
    // Mesajları zamana göre eskiden yeniye sırala
    const q = query(collection(db, "chatMessages"), orderBy("timestamp", "asc"));

    // onSnapshot ile veritabanındaki her değişikliği anlık yakalarız
    const unsubscribe = onSnapshot(q, (snapshot) => {
        const messages = snapshot.docs.map((doc) => ({
            id: doc.id,
            ...doc.data(),
        }));
        callback(messages);
    });

    return unsubscribe; // Dinlemeyi durdurmak için kullanılır
};