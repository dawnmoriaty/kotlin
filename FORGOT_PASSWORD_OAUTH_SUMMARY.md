# ✅ FORGOT PASSWORD & OAUTH2 - HOÀN THÀNH

## 📋 Tổng quan
Đã implement thành công 4 tính năng:
1. ✅ **Forgot Password** - Quên mật khẩu qua email
2. ✅ **Reset Password** - Đặt lại mật khẩu với token
3. ✅ **Google OAuth2 Login** - Đăng nhập bằng Google
4. ✅ **Facebook OAuth2 Login** - Đăng nhập bằng Facebook

---

## 🚀 Các bước Setup

### 1. Chạy Migration SQL
```bash
psql -U root -d financial_db_dev
```

Sau đó paste SQL này:
```sql
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT password_reset_tokens_expires_check CHECK (expires_at > created_at)
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires ON password_reset_tokens(expires_at);
```

### 2. Cấu hình Email (Gmail)

**application.yaml:**
```yaml
email:
  smtp:
    host: "smtp.gmail.com"
    port: 465
  username: "your-gmail@gmail.com"
  password: "your-app-password"
  from: "noreply@financial.app"
  fromName: "Financial App"

app:
  frontendUrl: "http://localhost:3000"
```

**Tạo Gmail App Password:**
1. Vào: https://myaccount.google.com/apppasswords
2. Chọn "App passwords"
3. Tạo password mới cho "Mail" app
4. Copy 16 ký tự vào `email.password`

### 3. Build & Run
```bash
# Build
.\gradlew.bat build -x test

# Run
.\gradlew.bat run
```

---

## 📡 API Endpoints

### 1. Forgot Password
```http
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Email đặt lại mật khẩu đã được gửi",
  "data": null,
  "timestamp": "2025-11-16T..."
}
```

---

### 2. Reset Password
```http
POST /api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "uuid-from-email",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công",
  "data": null,
  "timestamp": "2025-11-16T..."
}
```

**Validation:**
- ✅ Password phải trùng khớp
- ✅ Password tối thiểu 8 ký tự
- ✅ Token chỉ dùng được 1 lần
- ✅ Token hết hạn sau 15 phút

---

### 3. Google Login
```http
POST /api/v1/auth/google
Content-Type: application/json

{
  "idToken": "google-id-token-from-frontend"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Google login successful",
  "data": {
    "user": {
      "id": "uuid",
      "username": "user",
      "email": "user@gmail.com",
      "role": "user",
      "idGoogle": true,
      "idFacebook": false
    },
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token"
  }
}
```

---

### 4. Facebook Login
```http
POST /api/v1/auth/facebook
Content-Type: application/json

{
  "accessToken": "facebook-access-token-from-frontend"
}
```

---

## 🔄 Flow Diagram

### Forgot Password Flow:
```
User → Frontend → POST /forgot-password
                         ↓
                   Generate Token
                         ↓
                   Save to DB
                         ↓
                   Send Email
                         ↓
User nhận email → Click link → Frontend
                         ↓
               POST /reset-password với token
                         ↓
                   Validate token
                         ↓
                 Update password
                         ↓
                Mark token as used
```

### OAuth2 Flow:
```
User → Click "Login with Google/Facebook"
            ↓
      OAuth2 Provider (Google/Facebook)
            ↓
      Get idToken/accessToken
            ↓
      POST /auth/google or /auth/facebook
            ↓
      Verify token with OAuth provider
            ↓
      Find or Create user
            ↓
      Return JWT tokens
```

---

## 📂 Files Created/Modified

### ✅ New Files:
- `src/main/kotlin/data/database/tables/PasswordResetTokens.kt`
- `src/main/kotlin/domain/services/IEmailService.kt`
- `src/main/kotlin/domain/services/impl/EmailService.kt`
- `src/main/kotlin/dtos/request/PasswordRequest.kt`
- `migration_password_reset.sql`
- `FORGOT_PASSWORD_OAUTH_SETUP.md`
- `TEST_API_FORGOT_PASSWORD.md`

### ✅ Modified Files:
- `build.gradle.kts` - Added email & HTTP client dependencies
- `src/main/kotlin/Application.kt` - Added EmailService & HttpClient
- `src/main/kotlin/data/database/DatabaseFactory.kt` - Added PasswordResetTokens table
- `src/main/kotlin/data/repository/IUserRepository.kt` - Added updatePassword, linkGoogle/Facebook methods
- `src/main/kotlin/data/repository/impl/UserRepository.kt` - Implemented new methods
- `src/main/kotlin/domain/services/IAuthService.kt` - Added forgot/reset password & OAuth methods
- `src/main/kotlin/domain/services/impl/AuthService.kt` - Implemented all new features
- `src/main/kotlin/routes/AuthRoutes.kt` - Added new endpoints
- `src/main/resources/application.yaml` - Added email & frontend URL config

---

## 🧪 Testing

### Test với cURL:

**1. Forgot Password:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'
```

**2. Check email** → Lấy token

**3. Reset Password:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_TOKEN",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
  }'
```

**4. Login với password mới:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "NewPassword123!"
  }'
```

---

## 🔐 Security Features

✅ **Token Security:**
- UUID v4 random token (không đoán được)
- Hết hạn sau 15 phút
- Chỉ dùng 1 lần (is_used flag)
- Xóa tự động token cũ khi tạo mới

✅ **Email Validation:**
- Kiểm tra email tồn tại
- Kiểm tra account có password không (OAuth-only account không thể reset)

✅ **Password Validation:**
- Minimum 8 ký tự
- Confirm password phải khớp
- BCrypt hash

✅ **OAuth2 Security:**
- Verify token với provider (Google/Facebook)
- Check email verified
- Link existing account nếu email đã tồn tại

---

## 📧 Email Template

Email được gửi với HTML đẹp mắt, bao gồm:
- ✅ Header với gradient
- ✅ Button "Đặt lại mật khẩu"
- ✅ Link backup
- ✅ Cảnh báo thời gian hết hạn (15 phút)
- ✅ Footer professional

---

## 🎯 Next Steps (Optional)

1. **Email Templates Advanced:**
   - Thêm logo công ty
   - Customize theme theo brand
   - Multi-language support

2. **OAuth Providers:**
   - Thêm GitHub OAuth
   - Thêm Microsoft OAuth
   - Thêm Apple Sign-In

3. **Security Enhancements:**
   - Rate limiting cho forgot password
   - CAPTCHA trước khi gửi email
   - 2FA (Two-Factor Authentication)
   - Email verification khi register

4. **Monitoring:**
   - Log failed login attempts
   - Alert khi có nhiều request forgot password
   - Track OAuth login success rate

---

## 📝 Notes

- Token expiration: **15 minutes**
- Email SMTP: **Gmail (port 465 SSL)**
- OAuth2 verify endpoints:
  - Google: `https://oauth2.googleapis.com/tokeninfo`
  - Facebook: `https://graph.facebook.com/me`

---

## ✨ Done!

Bạn đã có đầy đủ chức năng forgot password và OAuth2 login! 🎉

**Để test ngay:**
1. Cấu hình email trong `application.yaml`
2. Chạy migration SQL
3. Build & run server
4. Test với Postman hoặc cURL

**Need help?**
- Check `FORGOT_PASSWORD_OAUTH_SETUP.md` cho setup chi tiết
- Check `TEST_API_FORGOT_PASSWORD.md` cho test cases

