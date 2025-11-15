# Financial App - Personal Finance Management API

A robust Kotlin-based REST API for personal finance management, built with Ktor framework and PostgreSQL database.

## 🌟 Features

### Core Features
- 👤 **User Management** - Register, login, profile management
- 🔐 **JWT Authentication** - Secure access & refresh token system
- 💰 **Transaction Management** - Track income and expenses
- 📊 **Categories** - Custom categorization for transactions
- 📈 **Dashboard & Analytics** - Financial statistics and insights
- 🖼️ **File Upload** - Avatar and image management with MinIO

### Authentication Features
- ✅ **Standard Auth** - Email/username & password login
- 🔑 **Forgot Password** - Reset password via email (15-min token)
- 🔵 **Google OAuth2** - Sign in with Google account
- 🔵 **Facebook OAuth2** - Sign in with Facebook account
- 🔄 **Token Refresh** - Automatic token renewal
- 🚪 **Secure Logout** - Token revocation

### Security Features
- 🔒 BCrypt password hashing
- 🎫 JWT access & refresh tokens
- 📧 Email verification for password reset
- 🕐 Time-limited reset tokens (15 minutes)
- 🔐 Single-use reset tokens
- 🌐 OAuth2 token verification

## 🚀 Quick Start

### Prerequisites
- JDK 21+
- PostgreSQL 14+
- Gradle 9+
- MinIO (optional, for file storage)

### 1. Setup Database
```bash
# Create database
createdb -U root financial_db_dev

# Run migrations
psql -U root -d financial_db_dev -f migration_password_reset.sql
```

### 2. Configure Application
Edit `src/main/resources/application.yaml`:
```yaml
database:
  host: "localhost"
  port: 5432
  name: "financial_db_dev"
  user: "root"
  password: "your-password"

email:
  username: "your-gmail@gmail.com"
  password: "your-app-password"  # Gmail App Password
```

### 3. Build & Run
```bash
# Build
.\gradlew.bat build -x test

# Run
.\gradlew.bat run
```

Server will start at: `http://localhost:8080`

## 📡 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/register` | Register new user | No |
| POST | `/api/v1/auth/login` | Login with credentials | No |
| POST | `/api/v1/auth/google` | Login with Google OAuth2 | No |
| POST | `/api/v1/auth/facebook` | Login with Facebook OAuth2 | No |
| POST | `/api/v1/auth/forgot-password` | Request password reset | No |
| POST | `/api/v1/auth/reset-password` | Reset password with token | No |
| POST | `/api/v1/auth/refresh` | Refresh access token | No |
| POST | `/api/v1/auth/logout` | Logout (revoke token) | No |

### User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/user/me` | Get current user | Yes |
| GET | `/api/v1/user/profile` | Get user profile | Yes |
| PUT | `/api/v1/user/profile` | Update profile | Yes |
| POST | `/api/v1/user/upload-avatar` | Upload avatar | Yes |
| PUT | `/api/v1/user/change-password` | Change password | Yes |

### Category Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/categories` | List all categories | Yes |
| POST | `/api/v1/categories` | Create category | Yes |
| PUT | `/api/v1/categories/{id}` | Update category | Yes |
| DELETE | `/api/v1/categories/{id}` | Delete category | Yes |

### Transaction Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/transactions` | List transactions | Yes |
| POST | `/api/v1/transactions` | Create transaction | Yes |
| GET | `/api/v1/transactions/{id}` | Get transaction | Yes |
| PUT | `/api/v1/transactions/{id}` | Update transaction | Yes |
| DELETE | `/api/v1/transactions/{id}` | Delete transaction | Yes |
| GET | `/api/v1/transactions/dashboard` | Get dashboard stats | Yes |

## 🧪 Testing

### Quick Test - Forgot Password Flow
```bash
# 1. Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test123!"}'

# 2. Request password reset
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# 3. Get token from email or database
psql -U root -d financial_db_dev -c \
  "SELECT token FROM password_reset_tokens ORDER BY created_at DESC LIMIT 1;"

# 4. Reset password
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"YOUR_TOKEN","newPassword":"NewPass123!","confirmPassword":"NewPass123!"}'

# 5. Login with new password
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"test@example.com","password":"NewPass123!"}'
```

## 📚 Detailed Documentation

For complete setup and usage instructions, see:

- 📖 **[README_FORGOT_PASSWORD_OAUTH.md](./README_FORGOT_PASSWORD_OAUTH.md)** - Main documentation hub
- 🚀 **[QUICK_START_FORGOT_PASSWORD.md](./QUICK_START_FORGOT_PASSWORD.md)** - Quick start guide
- ✅ **[SETUP_CHECKLIST.md](./SETUP_CHECKLIST.md)** - Pre-deployment checklist
- 🧪 **[TEST_API_FORGOT_PASSWORD.md](./TEST_API_FORGOT_PASSWORD.md)** - API test cases
- 🔧 **[sql_helper_scripts.sql](./sql_helper_scripts.sql)** - Database helper queries

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Kotlin + Ktor 3.3.1 |
| **Database** | PostgreSQL 14+ |
| **ORM** | Exposed 0.47.0 |
| **Connection Pool** | HikariCP 5.0.1 |
| **Authentication** | JWT (JSON Web Tokens) |
| **Password Hash** | BCrypt |
| **Email** | Apache Commons Email 1.5 |
| **File Storage** | MinIO S3 |
| **OAuth2** | Google & Facebook |
| **HTTP Client** | Ktor Client CIO |

## 📦 Ktor Plugins

| Name | Description |
|------|-------------|
| Routing | Structured routing DSL |
| Status Pages | Exception handling for routes |
| Call Logging | Request logging |
| Content Negotiation | Automatic content conversion |
| kotlinx.serialization | JSON serialization |
| Authentication | Authorization header handling |
| Authentication JWT | JWT bearer authentication |
| CORS | Cross-Origin Resource Sharing |
| Default Headers | Default HTTP response headers |
| Compression | Response compression (GZIP) |

## 🔧 Building & Running

| Task | Description |
|------|-------------|
| `.\gradlew.bat test` | Run tests |
| `.\gradlew.bat build` | Build project |
| `.\gradlew.bat build -x test` | Build without tests |
| `.\gradlew.bat buildFatJar` | Build executable JAR |
| `.\gradlew.bat buildImage` | Build Docker image |
| `.\gradlew.bat run` | Run server |
| `.\gradlew.bat runDocker` | Run with Docker |

## 🐳 Docker Support

```bash
# Build image
.\gradlew.bat buildImage

# Run with docker-compose
docker-compose up -d

# Check logs
docker-compose logs -f app
```

## 📧 Email Configuration

For forgot password functionality, configure Gmail:

1. Generate App Password: https://myaccount.google.com/apppasswords
2. Update `application.yaml`:
```yaml
email:
  smtp:
    host: "smtp.gmail.com"
    port: 465
  username: "your-email@gmail.com"
  password: "app-password-16-chars"
```

## 🔐 OAuth2 Setup

### Google OAuth2
1. Create project: https://console.cloud.google.com
2. Enable Google+ API
3. Create OAuth2 credentials
4. Use idToken from frontend

### Facebook Login
1. Create app: https://developers.facebook.com
2. Add Facebook Login product
3. Configure OAuth redirect URIs
4. Use accessToken from frontend

## 🗃️ Database Schema

Main tables:
- `users` - User accounts
- `profiles` - User profiles
- `categories` - Transaction categories
- `transactions` - Financial transactions
- `refresh_tokens` - JWT refresh tokens
- `password_reset_tokens` - Password reset tokens

## 🔒 Security Best Practices

- ✅ All passwords are BCrypt hashed
- ✅ JWT tokens with expiration
- ✅ Refresh token rotation
- ✅ Password reset tokens expire in 15 minutes
- ✅ Single-use reset tokens
- ✅ OAuth2 token verification
- ✅ CORS configured
- ✅ SQL injection prevention (Exposed ORM)

## 📊 Project Structure

```
src/main/kotlin/
├── Application.kt                  # Main entry point
├── data/
│   ├── database/
│   │   ├── DatabaseFactory.kt     # DB connection
│   │   ├── dao/                   # Data Access Objects
│   │   └── tables/                # Table definitions
│   ├── model/                     # Domain models
│   └── repository/                # Repository pattern
│       ├── I*Repository.kt        # Interfaces
│       └── impl/                  # Implementations
├── domain/
│   ├── exceptions/                # Custom exceptions
│   └── services/                  # Business logic
│       ├── I*Service.kt           # Service interfaces
│       └── impl/                  # Service implementations
├── dtos/                          # Data Transfer Objects
│   ├── request/                   # Request DTOs
│   └── response/                  # Response DTOs
├── plugins/                       # Ktor plugins config
└── routes/                        # API route definitions

src/main/resources/
├── application.yaml               # App configuration
├── logback.xml                    # Logging config
└── db/
    └── init.sql                   # Database schema
```

## 🚦 API Response Format

All API responses follow this structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2025-11-16T10:30:00Z"
}
```

Error response:
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2025-11-16T10:30:00Z"
}
```

## 🐛 Troubleshooting

### Build fails
```bash
.\gradlew.bat clean build -x test
```

### Port already in use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Database connection error
- Check PostgreSQL is running
- Verify credentials in `application.yaml`
- Check database exists: `psql -U root -l`

### Email not sending
- Use Gmail App Password, not regular password
- Check port 465 not blocked by firewall
- Verify SMTP settings in config

## 📝 Environment Variables (Production)

```bash
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=financial_db
DATABASE_USER=root
DATABASE_PASSWORD=secure_password

JWT_SECRET=your-256-bit-secret-key
JWT_ISSUER=https://your-domain.com
JWT_AUDIENCE=financial-app-users

EMAIL_USERNAME=noreply@your-domain.com
EMAIL_PASSWORD=secure_email_password
EMAIL_FROM=noreply@your-domain.com

MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minio_access_key
MINIO_SECRET_KEY=minio_secret_key

APP_FRONTEND_URL=https://your-frontend.com
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

For issues and questions:
- Check documentation in `README_FORGOT_PASSWORD_OAUTH.md`
- Review logs in `logs/application.log`
- Use SQL helpers in `sql_helper_scripts.sql`

## 🎉 Acknowledgments

- [Ktor Framework](https://ktor.io/)
- [Exposed ORM](https://github.com/JetBrains/Exposed)
- [Kotlin](https://kotlinlang.org/)

---

**Built with ❤️ using Kotlin & Ktor**

*Last updated: November 16, 2025*


