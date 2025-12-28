# 💳 Tuition Payment System

A university tuition payment system with an AI-powered chat interface, built as the final project for **SE4458 - Software Architecture and Design of Modern Large-Scale Systems**.

> **Group 2** | December 2024

---

## 📑 Table of Contents

- [Features](#-features)
- [Live Deployment](#-live-deployment)
- [Architecture](#️-architecture)
- [Technology Stack](#️-technology-stack)
- [API Reference](#-api-reference)
- [AI Chat Assistant](#-ai-chat-assistant)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Configuration](#️-configuration)
- [Rate Limiting & Caching](#-rate-limiting--caching)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Demo](#-demo)

---

## ✨ Features

- 🤖 **AI Chat Assistant** - Natural language interface powered by Google Gemini
- 💰 **Tuition Management** - Query, pay, and track tuition balances
- 📊 **Admin Dashboard** - Batch upload tuitions via CSV, view unpaid status
- 🔐 **JWT Authentication** - Secure admin endpoints
- ⚡ **Smart Caching** - Intent caching to reduce API costs
- 🔒 **Rate Limiting** - Protection against abuse (frontend & backend)
- 📱 **Real-time Messaging** - Firebase Firestore integration
- 📖 **API Documentation** - Swagger/OpenAPI support

---

## 🌐 Live Deployment

| Component | URL |
|-----------|-----|
| 🌐 **Azure API Gateway** | [gateway-group2-basar.azure-api.net](https://gateway-group2-basar.azure-api.net) |
| ⚙️ **Backend API** | [tutation-payment-system.azurewebsites.net](https://tutation-payment-system.azurewebsites.net) |
| 📖 **Swagger UI** | [/swagger-ui/index.html](https://tutation-payment-system.azurewebsites.net/swagger-ui/index.html) |
| 📄 **OpenAPI Docs** | [/v3/api-docs](https://tutation-payment-system.azurewebsites.net/v3/api-docs) |

> **Note**: Frontend runs locally. See [Getting Started](#-getting-started) section.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│               React Frontend (Chat Interface)               │
│  ├─ Firebase Firestore for real-time messaging              │
│  └─ Rate limiting: 15 requests per session                  │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTPS
                          ▼
┌─────────────────────────────────────────────────────────────┐
│            Azure API Management Gateway                      │
│            gateway-group2-basar.azure-api.net                │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTPS
                          ▼
┌─────────────────────────────────────────────────────────────┐
│            Spring Boot Backend (Azure App Service)           │
│  ├─ AI Intent Parsing (Google Gemini API)                    │
│  ├─ JWT Authentication                                       │
│  └─ Rate limiting: 3 queries per student per day             │
└───────────────┬─────────────────────┬───────────────────────┘
                │                     │
                ▼                     ▼
┌───────────────────────┐   ┌───────────────────────┐
│    PostgreSQL         │   │   Google Gemini AI    │
│    (Azure Database)   │   │   (Intent Parsing)    │
└───────────────────────┘   └───────────────────────┘
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 19, Firebase Firestore |
| **Backend** | Spring Boot 3.4, Java 21 |
| **Database** | PostgreSQL (Azure Database) |
| **AI** | Google Gemini API |
| **Gateway** | Azure API Management |
| **Auth** | JWT (JSON Web Tokens) |
| **Docs** | SpringDoc OpenAPI (Swagger) |

---

## 📋 API Reference

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/ai/chat` | AI Assistant Chat |
| `GET` | `/api/v1/tuition/{studentNo}` | Query student tuition |
| `POST` | `/api/v1/payment` | Process tuition payment |
| `POST` | `/api/v1/auth/login` | Authenticate & get JWT |

### Admin Endpoints (JWT Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/admin/add-tuition` | Add tuition record |
| `POST` | `/api/v1/admin/batch-upload` | Batch upload via CSV |
| `GET` | `/api/v1/admin/unpaid-status` | Get unpaid tuitions |

> 🔐 **Default Admin Credentials**: `admin` / `admin123`

---

## 🤖 AI Chat Assistant

The AI assistant understands natural language and performs tuition operations automatically.

### Supported Intents

| Intent | Example Query |
|--------|---------------|
| **QUERY_TUITION** | "Check my tuition balance for student 2023001" |
| **PAY_TUITION** | "Pay 1000 for term 2025-SUMMER, student 2023001" |
| **UNPAID_TUITION** | "Show unpaid tuitions for 2025-SUMMER" |

### How It Works

1. User sends a natural language message
2. Google Gemini AI parses the intent and extracts parameters
3. Backend executes the corresponding tuition API
4. Response is formatted and returned to the user

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **PostgreSQL** database (or use Azure)
- **Google Gemini API Key**
- **Firebase Project** (for Firestore)

### Backend Setup

```bash
# Navigate to backend directory
cd backend

# Configure environment (see Configuration section)
# Create secret.properties file

# Run the application
./mvnw spring-boot:run
```

The backend will start at `http://localhost:8080`

### Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Configure environment (see Configuration section)
# Create .env file

# Start development server
npm start
```

The frontend will start at `http://localhost:3000`

---

## ⚙️ Configuration

### Backend Configuration

Create `backend/src/main/resources/secret.properties`:

```properties
# Google Gemini AI
gemini.api.key=your-gemini-api-key

# Database
spring.datasource.url=jdbc:postgresql://your-host:5432/your-db
spring.datasource.username=your-username
spring.datasource.password=your-password
```

### Frontend Configuration

Create `frontend/.env`:

```env
# API Gateway URL
REACT_APP_API_GATEWAY_URL=https://gateway-group2-basar.azure-api.net

# Firebase Configuration
REACT_APP_FIREBASE_API_KEY=your-firebase-api-key
REACT_APP_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
REACT_APP_FIREBASE_PROJECT_ID=your-project-id
REACT_APP_FIREBASE_STORAGE_BUCKET=your-project.appspot.com
REACT_APP_FIREBASE_MESSAGING_SENDER_ID=your-sender-id
REACT_APP_FIREBASE_APP_ID=your-app-id
```

> ⚠️ **Security Note**: Never commit `.env` or `secret.properties` files to version control!

---

## 📊 Rate Limiting & Caching

### Rate Limits

| Component | Limit |
|-----------|-------|
| Frontend | 15 requests per session |
| Backend (Query) | 3 requests per student per day |

### Intent Caching

The system caches AI-parsed intents to reduce Gemini API usage:

- **TTL**: 1 hour
- **Cache Key Format**: `intent:studentNo:term`

```
🔄 Cache HIT  → Returning cached intent (no API call)
🌐 Cache MISS → Calling Gemini API + caching result
```

---

## 🧪 Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

### API Testing with Postman

Import the included Postman collection:
- `TuitionPaymentAPI.postman_collection.json`

---

## 🚀 Deployment

### Azure Deployment Checklist

- [x] Backend deployed to Azure App Service
- [x] PostgreSQL database provisioned on Azure
- [x] Azure API Management Gateway configured
- [x] Environment variables set in Azure portal
- [ ] Frontend can be deployed to Azure Static Web Apps or run locally

### Environment Variables for Azure

Set these in Azure App Service → Configuration → Application Settings:

| Name | Value |
|------|-------|
| `GEMINI_API_KEY` | your-gemini-api-key |
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://... |
| `SPRING_DATASOURCE_USERNAME` | your-username |
| `SPRING_DATASOURCE_PASSWORD` | your-password |

---

## 📹 Demo

### Video Demonstration

[🎥 Watch Project Demo on YouTube](https://youtube.com/your-video-link)

### Screenshots

> *Add screenshots of the chat interface and admin dashboard here*

---

## 👥 Team

**Group 2** - SE4458 Final Project

---

## 📄 License

This project is developed for educational purposes as part of the SE4458 course.

---

<p align="center">
  Made with ❤️ by Group 2
</p>
