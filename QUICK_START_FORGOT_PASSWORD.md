# 🚀 QUICK START - Test Forgot Password & OAuth2

## ⚡ Bước 1: Chạy Migration SQL

Mở terminal và chạy:

```bash
psql -U root -d financial_db_dev
```

Paste SQL này:

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

-- Kiểm tra bảng đã tạo
\dt password_reset_tokens
```

## ⚡ Bước 2: Cấu hình Email (Tùy chọn - để test forgot password)

**Nếu bạn muốn test forgot password:**

1. Tạo Gmail App Password:
   - Vào: https://myaccount.google.com/apppasswords
   - Tạo password cho "Mail" app
   - Copy password (16 ký tự)

2. Cập nhật `src/main/resources/application.yaml`:
   ```yaml
   email:
     smtp:
       host: "smtp.gmail.com"
       port: 465
     username: "your-email@gmail.com"  # Email của bạn
     password: "abcd efgh ijkl mnop"    # App password vừa tạo
     from: "noreply@financial.app"
     fromName: "Financial App"
   
   app:
     frontendUrl: "http://localhost:3000"
   ```

**Nếu không muốn test email ngay:** Server vẫn chạy được, chỉ khi gọi `/forgot-password` sẽ lỗi.

## ⚡ Bước 3: Start Server

```bash
.\gradlew.bat run
```

Đợi đến khi thấy:
```
Application started in X.XXX seconds.
```

## ⚡ Bước 4: Test API

### Test 1: Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"Test123456!\"}"
```

**Expected:** 
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {...},
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

---

### Test 2: Forgot Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"test@example.com\"}"
```

**Expected:**
```json
{
  "success": true,
  "message": "Email đặt lại mật khẩu đã được gửi",
  "data": null
}
```

**Next:** Check email → lấy token từ link (hoặc check database)

---

### Test 3: Get Token từ Database (để test nhanh)

```bash
psql -U root -d financial_db_dev -c "SELECT token, expires_at, is_used FROM password_reset_tokens ORDER BY created_at DESC LIMIT 1;"
```

Copy token vừa lấy được.

---

### Test 4: Reset Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password ^
  -H "Content-Type: application/json" ^
  -d "{\"token\": \"YOUR_TOKEN_HERE\", \"newPassword\": \"NewPass123!\", \"confirmPassword\": \"NewPass123!\"}"
```

**Expected:**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công",
  "data": null
}
```

---

### Test 5: Login với password mới
```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\": \"test@example.com\", \"password\": \"NewPass123!\"}"
```

**Expected:** Login thành công với access token mới!

---

## ⚡ Bước 5: Test OAuth2 (Cần frontend)

OAuth2 cần frontend để lấy idToken/accessToken từ Google/Facebook.

### Google Login endpoint:
```http
POST /api/v1/auth/google
Content-Type: application/json

{
  "idToken": "google-id-token-from-frontend"
}
```

### Facebook Login endpoint:
```http
POST /api/v1/auth/facebook
Content-Type: application/json

{
  "accessToken": "facebook-access-token-from-frontend"
}
```

---

## 🧪 Test Error Cases

### 1. Email không tồn tại
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"notexist@example.com\"}"
```

**Expected:** Error với message "Email không tồn tại"

---

### 2. Password không khớp
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password ^
  -H "Content-Type: application/json" ^
  -d "{\"token\": \"YOUR_TOKEN\", \"newPassword\": \"Pass123!\", \"confirmPassword\": \"DifferentPass123!\"}"
```

**Expected:** Error với message "Mật khẩu xác nhận không khớp"

---

### 3. Token đã sử dụng
```bash
# Gọi lại API reset password với cùng token đã dùng
```

**Expected:** Error với message "Token đã được sử dụng"

---

### 4. Token hết hạn
Đợi 15 phút sau khi request forgot password, sau đó gọi reset password.

**Expected:** Error với message "Token đã hết hạn"

---

## 📊 Check Database

### Xem tất cả reset tokens:
```bash
psql -U root -d financial_db_dev -c "SELECT id, user_id, LEFT(token, 10) || '...' as token_preview, expires_at, is_used, created_at FROM password_reset_tokens ORDER BY created_at DESC;"
```

### Xem user vừa tạo:
```bash
psql -U root -d financial_db_dev -c "SELECT id, username, email, role, id_google IS NOT NULL as has_google, id_facebook IS NOT NULL as has_facebook FROM users WHERE email = 'test@example.com';"
```

### Clean up test data:
```bash
psql -U root -d financial_db_dev -c "DELETE FROM password_reset_tokens WHERE is_used = true OR expires_at < NOW();"
```

---

## 🎯 Postman Collection

Import collection này vào Postman:

1. Mở Postman
2. Import → Raw text
3. Paste nội dung từ `TEST_API_FORGOT_PASSWORD.md`
4. Hoặc tạo manual:

**Endpoints:**
- POST `/api/v1/auth/register`
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/forgot-password`
- POST `/api/v1/auth/reset-password`
- POST `/api/v1/auth/google`
- POST `/api/v1/auth/facebook`
- POST `/api/v1/auth/refresh`
- POST `/api/v1/auth/logout`

---

## ❓ Troubleshooting

### Lỗi "Connection refused"
✅ **Fix:** Server chưa chạy. Run `.\gradlew.bat run`

### Lỗi "Email không tồn tại"
✅ **Fix:** Register user trước với `/api/v1/auth/register`

### Lỗi gửi email
✅ **Fix:** 
- Check email config trong `application.yaml`
- Dùng Gmail App Password, không phải password thường
- Check port 465 không bị block

### Token không work
✅ **Fix:**
- Check token chưa hết hạn (< 15 phút)
- Check token chưa được sử dụng
- Copy đúng token từ email/database

### OAuth2 không work
✅ **Fix:**
- Cần frontend để lấy idToken/accessToken
- Hoặc dùng Google/Facebook OAuth Playground để test

---

## ✅ Checklist

- [ ] Chạy migration SQL
- [ ] Cấu hình email (nếu cần)
- [ ] Start server
- [ ] Test register user
- [ ] Test forgot password
- [ ] Check email hoặc database để lấy token
- [ ] Test reset password
- [ ] Test login với password mới
- [ ] (Optional) Test OAuth2 với frontend

---

## 🎉 Success!

Nếu tất cả test cases đều pass, bạn đã setup thành công! 

**Next steps:**
- Integrate với frontend
- Setup Google/Facebook OAuth credentials
- Deploy to production
- Add monitoring & logging

**Docs:**
- `FORGOT_PASSWORD_OAUTH_SUMMARY.md` - Tổng quan đầy đủ
- `FORGOT_PASSWORD_OAUTH_SETUP.md` - Chi tiết setup
- `TEST_API_FORGOT_PASSWORD.md` - Test cases chi tiết

