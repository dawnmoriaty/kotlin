# 📚 Tài Liệu - Forgot Password & OAuth2 Login

## 🎯 Tính năng đã implement

✅ **Forgot Password** - Người dùng quên mật khẩu có thể reset qua email  
✅ **Reset Password** - Đặt lại mật khẩu với token có thời hạn (15 phút)  
✅ **Google OAuth2 Login** - Đăng nhập bằng tài khoản Google  
✅ **Facebook OAuth2 Login** - Đăng nhập bằng tài khoản Facebook  

---

## 📖 Hướng dẫn sử dụng

### 🚀 Bắt đầu nhanh (Quick Start)
👉 **[QUICK_START_FORGOT_PASSWORD.md](./QUICK_START_FORGOT_PASSWORD.md)**

Hướng dẫn test nhanh trong 5 phút:
- Setup database
- Cấu hình email (optional)
- Test API với cURL
- Check database

### 📋 Chi tiết đầy đủ (Full Documentation)
👉 **[FORGOT_PASSWORD_OAUTH_SUMMARY.md](./FORGOT_PASSWORD_OAUTH_SUMMARY.md)**

Tài liệu đầy đủ bao gồm:
- Architecture & Flow diagrams
- Security features
- Email templates
- Files created/modified
- Next steps & enhancements

### 🛠️ Setup chi tiết (Detailed Setup)
👉 **[FORGOT_PASSWORD_OAUTH_SETUP.md](./FORGOT_PASSWORD_OAUTH_SETUP.md)**

Hướng dẫn setup từng bước:
- Database migration
- Email configuration (Gmail App Password)
- OAuth2 integration (Google & Facebook)
- Troubleshooting common issues

### 🧪 Test Cases
👉 **[TEST_API_FORGOT_PASSWORD.md](./TEST_API_FORGOT_PASSWORD.md)**

Tất cả test cases:
- cURL examples
- Postman collection
- Error scenarios
- Expected responses

---

## 🔗 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/forgot-password` | Gửi email reset password |
| POST | `/api/v1/auth/reset-password` | Đặt lại password với token |
| POST | `/api/v1/auth/google` | Đăng nhập với Google |
| POST | `/api/v1/auth/facebook` | Đăng nhập với Facebook |

---

## ⚡ Quick Test

### 1. Forgot Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"user@example.com\"}"
```

### 2. Reset Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password ^
  -H "Content-Type: application/json" ^
  -d "{\"token\": \"YOUR_TOKEN\", \"newPassword\": \"NewPass123!\", \"confirmPassword\": \"NewPass123!\"}"
```

### 3. Google Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/google ^
  -H "Content-Type: application/json" ^
  -d "{\"idToken\": \"GOOGLE_ID_TOKEN\"}"
```

### 4. Facebook Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/facebook ^
  -H "Content-Type: application/json" ^
  -d "{\"accessToken\": \"FACEBOOK_ACCESS_TOKEN\"}"
```

---

## 🔐 Security

- ✅ Token hết hạn sau **15 phút**
- ✅ Token chỉ dùng được **1 lần**
- ✅ Password minimum **8 ký tự**
- ✅ BCrypt hashing
- ✅ OAuth2 token verification
- ✅ Email verification required

---

## 📦 Dependencies Added

```gradle
// Email
implementation("org.apache.commons:commons-email:1.5")

// HTTP Client for OAuth2
implementation("io.ktor:ktor-client-core")
implementation("io.ktor:ktor-client-cio")
implementation("io.ktor:ktor-client-content-negotiation")
```

---

## 🗃️ Database

### New Table: `password_reset_tokens`
```sql
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    token TEXT UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### Migration file:
👉 **[migration_password_reset.sql](./migration_password_reset.sql)**

---

## 📧 Email Configuration

### Gmail Setup (application.yaml):
```yaml
email:
  smtp:
    host: "smtp.gmail.com"
    port: 465
  username: "your-email@gmail.com"
  password: "your-app-password"  # Gmail App Password
  from: "noreply@financial.app"
  fromName: "Financial App"

app:
  frontendUrl: "http://localhost:3000"
```

### Tạo Gmail App Password:
1. https://myaccount.google.com/apppasswords
2. Tạo password cho "Mail"
3. Copy 16 ký tự vào config

---

## 🎨 Email Template

Email được gửi với HTML template đẹp:
- Gradient header
- Call-to-action button
- Link backup
- Expiry warning (15 phút)
- Professional footer

Preview template trong `EmailService.kt`

---

## 🧩 Integration với Frontend

### Google Sign-In (React example)
```javascript
import { GoogleLogin } from '@react-oauth/google';

<GoogleLogin
  onSuccess={(response) => {
    fetch('http://localhost:8080/api/v1/auth/google', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken: response.credential })
    })
    .then(res => res.json())
    .then(data => {
      localStorage.setItem('accessToken', data.data.accessToken);
      localStorage.setItem('refreshToken', data.data.refreshToken);
    });
  }}
/>
```

### Facebook Login (React example)
```javascript
import FacebookLogin from 'react-facebook-login';

<FacebookLogin
  appId="YOUR_FACEBOOK_APP_ID"
  fields="name,email"
  callback={(response) => {
    fetch('http://localhost:8080/api/v1/auth/facebook', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accessToken: response.accessToken })
    })
    .then(res => res.json())
    .then(data => {
      localStorage.setItem('accessToken', data.data.accessToken);
      localStorage.setItem('refreshToken', data.data.refreshToken);
    });
  }}
/>
```

---

## 📂 Project Structure

```
src/main/kotlin/
├── data/
│   ├── database/
│   │   └── tables/
│   │       └── PasswordResetTokens.kt          ← NEW
│   └── repository/
│       ├── IUserRepository.kt                   ← UPDATED
│       └── impl/
│           └── UserRepository.kt                ← UPDATED
├── domain/
│   └── services/
│       ├── IAuthService.kt                      ← UPDATED
│       ├── IEmailService.kt                     ← NEW
│       └── impl/
│           ├── AuthService.kt                   ← UPDATED
│           └── EmailService.kt                  ← NEW
├── dtos/
│   └── request/
│       └── PasswordRequest.kt                   ← NEW
├── routes/
│   └── AuthRoutes.kt                            ← UPDATED
└── Application.kt                               ← UPDATED

src/main/resources/
└── application.yaml                             ← UPDATED

migration_password_reset.sql                     ← NEW
```

---

## 🐛 Troubleshooting

### Server không chạy được
```bash
# Check port 8080
netstat -ano | findstr :8080

# Kill process nếu cần
taskkill /PID <PID> /F

# Restart server
.\gradlew.bat run
```

### Email không gửi được
- ✅ Check Gmail App Password đúng
- ✅ Check port 465 không bị block
- ✅ Check email config trong application.yaml

### Token không work
- ✅ Check token chưa hết hạn (< 15 phút)
- ✅ Check token chưa được sử dụng
- ✅ Check database: `SELECT * FROM password_reset_tokens;`

### OAuth2 errors
- ✅ Google: Check idToken valid
- ✅ Facebook: Check accessToken valid
- ✅ Check email verified trong response

---

## 🎓 Learn More

- [Ktor Documentation](https://ktor.io/docs/)
- [Google OAuth2 API](https://developers.google.com/identity/protocols/oauth2)
- [Facebook Login API](https://developers.facebook.com/docs/facebook-login)
- [Apache Commons Email](https://commons.apache.org/proper/commons-email/)

---

## ✨ Next Steps

- [ ] Setup rate limiting cho forgot password
- [ ] Add CAPTCHA before sending email
- [ ] Implement 2FA (Two-Factor Authentication)
- [ ] Email verification on register
- [ ] Add GitHub OAuth
- [ ] Add Microsoft OAuth
- [ ] Add Apple Sign-In

---

## 💡 Tips

1. **Development:** Dùng dummy email service để test nhanh
2. **Staging:** Dùng Gmail với App Password
3. **Production:** Dùng SendGrid/AWS SES cho reliability
4. **Monitoring:** Log failed attempts và suspicious activities
5. **Security:** Always use HTTPS in production

---

## 📞 Support

Nếu gặp vấn đề:
1. Check `QUICK_START_FORGOT_PASSWORD.md` cho troubleshooting
2. Check logs trong `logs/application.log`
3. Check database state với SQL queries
4. Review `FORGOT_PASSWORD_OAUTH_SETUP.md` cho detailed setup

---

## 🎉 Hoàn tất!

Bạn đã có đầy đủ chức năng:
- ✅ Forgot Password
- ✅ Reset Password  
- ✅ Google OAuth2 Login
- ✅ Facebook OAuth2 Login

**Happy coding! 🚀**

