# Tuition Payment System - AI Agent Chat Application

University Tuition Payment System with AI-powered chat interface.

## 🌐 Live Deployment

| Component | URL |
|-----------|-----|
| **Azure API Gateway** | https://gateway-group2-basar.azure-api.net |
| **Backend** | https://tutation-payment-system.azurewebsites.net |
| **Frontend** | Run locally: `npm start` |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│              React Frontend (Chat Interface)             │
│  - Firebase Firestore for real-time messaging            │
│  - Rate limiting: 15 requests per session                │
└────────────────────┬────────────────────────────────────┘
                     │ HTTPS
┌────────────────────▼────────────────────────────────────┐
│         Azure API Management Gateway                     │
│  - https://gateway-group2-basar.azure-api.net            │
└────────────────────┬────────────────────────────────────┘
                     │ HTTPS
┌────────────────────▼────────────────────────────────────┐
│         Spring Boot Backend (Azure App Service)          │
│  - AI Intent Parsing (Google Gemini)                     │
│  - Rate limiting: 3 queries per student per day          │
└────────────────────┬────────────────────────────────────┘
         ┌───────────┴───────────┐
┌────────▼────────┐    ┌────────▼────────┐
│   PostgreSQL    │    │  Google Gemini  │
│   (Azure DB)    │    │  AI API         │
└─────────────────┘    └─────────────────┘
```

## 📋 API Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/v1/ai/chat` | POST | AI Assistant Chat | No |
| `/api/v1/tuition/{studentNo}` | GET | Query Tuition | No |
| `/api/v1/payment` | POST | Pay Tuition | No |
| `/api/v1/auth/login` | POST | Get JWT Token | No |
| `/api/v1/admin/add-tuition` | POST | Add Tuition | Yes |
| `/api/v1/admin/batch-upload` | POST | Batch Upload CSV | Yes |
| `/api/v1/admin/unpaid-status` | GET | Unpaid Status | Yes |

## 🤖 AI Chat Intents

1. **QUERY_TUITION**: "Check my tuition balance for student 2023001"
2. **PAY_TUITION**: "Pay 1000 for term 2025-SUMMER, student 2023001"
3. **UNPAID_TUITION**: "Show unpaid tuitions for 2025-SUMMER"

## 🚀 Running Locally

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm start
```

## ⚙️ Environment Variables

### Frontend (.env)
```env
REACT_APP_API_GATEWAY_URL=https://gateway-group2-basar.azure-api.net
REACT_APP_FIREBASE_API_KEY=your-key
REACT_APP_FIREBASE_PROJECT_ID=tutation-payment
```

### Backend (secret.properties)
```properties
gemini.api.key=your-gemini-api-key
spring.datasource.url=jdbc:postgresql://host:5432/db
spring.datasource.username=user
spring.datasource.password=pass
```

## 🧪 Running Tests
```bash
cd backend
./mvnw test
```

## 📊 Rate Limiting & Caching

| Feature | Limit |
|---------|-------|
| Frontend Rate Limit | 15 requests per session |
| Backend Query Limit | 3 requests per student per day |
| **Intent Cache TTL** | 1 hour |

### API Token Savings (Intent Caching)
The system caches parsed intents to save Gemini API tokens:
- Same question → Cache HIT → No API call
- Different question → Cache MISS → API call + Cache
- Cache key format: `intent:studentNo:term`

**Console output example:**
```
🔄 Cache HIT - Returning cached intent for: Check tuition for 2023001
🌐 Cache MISS - Calling Gemini API for: New question...
💾 Cached intent for future use. Cache size: 5
```

## 🔐 Authentication

**Default Credentials:** admin / admin123

## 🛠️ Technologies

- **Frontend**: React 19, Firebase Firestore
- **Backend**: Spring Boot 3.4, Java 21
- **AI**: Google Gemini API
- **Database**: PostgreSQL (Azure)
- **Gateway**: Azure API Management

## 📹 Demo Video

[Project Demo Video](https://youtube.com/your-video-link)

## 👥 Team

Group 2 - SE4458 Final Project
