# 💳 Tuition Payment System
youtube https://youtu.be/tYmTkjGuDP0




---

## 📑 Table of Contents

- [Features](#-features)
- [Live Deployment](#-live-deployment)
- [Architecture](#️-architecture)
- [Technology Stack](#️-technology-stack)
- [API Reference](#-api-reference)
- [AI Chat Assistant](#-ai-chat-assistant)
- [Testing Guide](#-testing-guide)
- [Rate Limiting & Caching](#-rate-limiting--caching)
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
- ☁️ **Cloud Deployment** - Fully deployed on Microsoft Azure

---

## 🌐 Live Deployment

| Component | URL | Status |
|-----------|-----|--------|
| 🎨 **Frontend** | [tutation-payment-system.azurewebsites.net](https://tutation-payment-system.azurewebsites.net) | ✅ Live |
| 🌐 **API Gateway** | [gateway-group2-basar.azure-api.net](https://gateway-group2-basar.azure-api.net) | ✅ Live |
| ⚙️ **Backend API** | [tutation-payment-system.azurewebsites.net](https://tutation-payment-system.azurewebsites.net) | ✅ Live |
| 📖 **Swagger UI** | [/swagger-ui/index.html](https://tutation-payment-system.azurewebsites.net/swagger-ui/index.html) | ✅ Live |
| 📄 **OpenAPI Docs** | [/v3/api-docs](https://tutation-payment-system.azurewebsites.net/v3/api-docs) | ✅ Live |

> 🚀 **All components are deployed and running on Azure!**

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│         React Frontend (Azure App Service)                  │
│  ├─ AI Chat Interface                                       │
│  ├─ Firebase Firestore for real-time messaging              │
│  └─ Rate limiting: 15 requests per session                  │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTPS
                          ▼
┌─────────────────────────────────────────────────────────────┐
│            Azure API Management Gateway                      │
│            gateway-group2-basar.azure-api.net                │
│  ├─ CORS Policy Configuration                               │
│  └─ API Routing & Management                                │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTPS
                          ▼
┌─────────────────────────────────────────────────────────────┐
│            Spring Boot Backend (Azure App Service)           │
│  ├─ AI Intent Parsing (Google Gemini v1 API)                │
│  ├─ JWT Authentication                                       │
│  ├─ Rate limiting: 3 queries per student per day             │
│  └─ RESTful API Endpoints                                   │
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
| **AI** | Google Gemini API (v1 Stable) |
| **Gateway** | Azure API Management |
| **Auth** | JWT (JSON Web Tokens) |
| **Docs** | SpringDoc OpenAPI (Swagger) |
| **Deployment** | Azure App Service, Azure API Management |

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
2. Google Gemini AI (v1 stable) parses the intent and extracts parameters
3. Backend executes the corresponding tuition API
4. Response is formatted and returned to the user
5. Results are cached for 1 hour to reduce API costs

---

## 🧪 Testing Guide

### Test Scenario 1: AI Chat - Query Tuition

**Objective**: Verify AI can understand natural language and query tuition balance.

**Steps**:
1. Open [https://tutation-payment-system.azurewebsites.net](https://tutation-payment-system.azurewebsites.net)
2. In the chat input, type: `"Check my tuition balance for student 2023001"`
3. Press Send

**Expected Result**:
- ✅ AI responds with tuition balance information
- ✅ No CORS errors in browser console
- ✅ Response time < 3 seconds

**Sample Response**:
```
Your tuition balance for student 2023001:
- Term: 2025-SUMMER
- Amount: 5000 TL
- Status: Unpaid
```

---

### Test Scenario 2: AI Chat - Pay Tuition

**Objective**: Verify AI can process payment requests.

**Steps**:
1. In the chat, type: `"Pay 1000 TL for term 2025-SUMMER, student 2023001"`
2. Press Send

**Expected Result**:
- ✅ Payment is processed successfully
- ✅ Confirmation message with updated balance
- ✅ Database is updated

**Sample Response**:
```
Payment successful!
- Amount paid: 1000 TL
- Remaining balance: 4000 TL
- Term: 2025-SUMMER
```

---

### Test Scenario 3: AI Chat - Unpaid Tuitions

**Objective**: Verify AI can query unpaid tuitions for a term.

**Steps**:
1. In the chat, type: `"Show unpaid tuitions for 2025-SUMMER"`
2. Press Send

**Expected Result**:
- ✅ List of all unpaid tuitions for the term
- ✅ Formatted table or list

**Sample Response**:
```
Unpaid tuitions for 2025-SUMMER:
1. Student 2023001 - 4000 TL
2. Student 2023002 - 5000 TL
3. Student 2023003 - 3500 TL
```

---

### Test Scenario 4: Rate Limiting

**Objective**: Verify rate limiting is working correctly.

**Steps**:
1. Send 15 messages in quick succession
2. Try to send the 16th message

**Expected Result**:
- ✅ First 15 messages are processed
- ✅ 16th message shows rate limit error
- ❌ Error message: "You have reached the maximum number of requests (15). Please refresh the page."

---

### Test Scenario 5: API Direct Access

**Objective**: Test API endpoints directly via Swagger.

**Steps**:
1. Open [Swagger UI](https://tutation-payment-system.azurewebsites.net/swagger-ui/index.html)
2. Navigate to `/api/v1/tuition/{studentNo}`
3. Click "Try it out"
4. Enter student number: `2023001`
5. Click "Execute"

**Expected Result**:
- ✅ Status Code: 200 OK
- ✅ JSON response with tuition data

**Sample Response**:
```json
{
  "studentNo": "2023001",
  "term": "2025-SUMMER",
  "amount": 5000.0,
  "paid": false
}
```

---

### Test Scenario 6: Admin Authentication

**Objective**: Verify JWT authentication for admin endpoints.

**Steps**:
1. Open [Swagger UI](https://tutation-payment-system.azurewebsites.net/swagger-ui/index.html)
2. Navigate to `/api/v1/auth/login`
3. Click "Try it out"
4. Enter credentials:
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
5. Click "Execute"

**Expected Result**:
- ✅ Status Code: 200 OK
- ✅ JWT token in response

**Sample Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Test Scenario 7: Batch Upload (Admin)

**Objective**: Test CSV batch upload functionality.

**Steps**:
1. Login and get JWT token (see Test Scenario 6)
2. Navigate to `/api/v1/admin/batch-upload` in Swagger
3. Click "Authorize" and paste JWT token
4. Upload a CSV file with format:
   ```csv
   studentNo,term,amount
   2023001,2025-SUMMER,5000
   2023002,2025-SUMMER,5500
   ```
5. Click "Execute"

**Expected Result**:
- ✅ Status Code: 200 OK
- ✅ All records uploaded successfully
- ✅ Database contains new records

---

### Test Scenario 8: CORS Verification

**Objective**: Verify CORS is properly configured.

**Steps**:
1. Open browser DevTools (F12)
2. Go to Console tab
3. Send a chat message
4. Check for CORS errors

**Expected Result**:
- ✅ No CORS errors
- ✅ Requests to `gateway-group2-basar.azure-api.net` succeed
- ✅ Response headers include `Access-Control-Allow-Origin`

---

### Test Scenario 9: Caching Verification

**Objective**: Verify intent caching is working.

**Steps**:
1. Send: `"Check balance for student 2023001"`
2. Wait 2 seconds
3. Send the exact same message again
4. Check backend logs

**Expected Result**:
- ✅ First request: Cache MISS → Gemini API called
- ✅ Second request: Cache HIT → No Gemini API call
- ✅ Response time for second request < 500ms

---

### Test Scenario 10: Error Handling

**Objective**: Verify graceful error handling.

**Steps**:
1. Send: `"Check balance for student 9999999"` (non-existent student)
2. Observe response

**Expected Result**:
- ✅ Friendly error message
- ✅ No stack traces exposed
- ❌ Error: "Student not found"

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

## 📹 Demo

### Video Demonstration

> 🎥 **Demo video will be added here**

### Screenshots

> 📸 **Screenshots will be added here**

---

## 🚀 Deployment Architecture

### Azure Resources

- **App Service**: Hosts both frontend and backend
- **API Management**: Gateway for API routing and CORS
- **PostgreSQL Database**: Azure Database for PostgreSQL
- **Application Insights**: Monitoring and logging

### CI/CD Pipeline

- **Source Control**: GitHub
- **Deployment**: Automatic deployment via GitHub Actions
- **Commits are automatically deployed to Azure**

---

## 👥 Team

**Group 2** - SE4458 Final Project

---


2
</p>
