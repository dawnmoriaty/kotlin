# Setup Instructions cho Forgot Password và OAuth2

## 1. Chạy SQL Migration

Kết nối vào PostgreSQL và chạy file migration:

```bash
psql -U root -d financial_db_dev -f migration_password_reset.sql
```

Hoặc chạy trực tiếp trong PostgreSQL:

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

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires ON password_reset_tokens(expires_at);

ALTER TABLE password_reset_tokens OWNER TO root;
```

## 2. Cấu hình Email trong application.yaml

Cập nhật thông tin email của bạn:

```yaml
email:
  smtp:
    host: "smtp.gmail.com"
    port: 465
  username: "your-email@gmail.com"  # Email của bạn
  password: "your-app-password"      # Gmail App Password (không phải mật khẩu thường)
  from: "noreply@financial.app"
  fromName: "Financial App"

app:
  frontendUrl: "http://localhost:3000"  # URL frontend của bạn
```

### Cách tạo Gmail App Password:

1. Truy cập: https://myaccount.google.com/apppasswords
2. Đăng nhập Gmail của bạn
3. Chọn "App passwords"
4. Chọn app: "Mail", device: "Other" (nhập "Financial App")
5. Click "Generate"
6. Copy mật khẩu 16 ký tự và paste vào `email.password`

## 3. API Endpoints Mới

### Forgot Password
```bash
POST http://localhost:8080/api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Reset Password
```bash
POST http://localhost:8080/api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "uuid-token-from-email",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

### Google Login
```bash
POST http://localhost:8080/api/v1/auth/google
Content-Type: application/json

{
  "idToken": "google-id-token-from-frontend"
}
```

### Facebook Login
```bash
POST http://localhost:8080/api/v1/auth/facebook
Content-Type: application/json

{
  "accessToken": "facebook-access-token-from-frontend"
}
```

## 4. Flow Forgot Password

1. User nhập email → gửi request đến `/api/v1/auth/forgot-password`
2. Server tạo token và gửi email với link reset
3. User click link trong email → mở trang frontend với token trong URL
4. User nhập mật khẩu mới → gửi request đến `/api/v1/auth/reset-password` với token
5. Server validate token và cập nhật mật khẩu

## 5. OAuth2 Integration

### Google Sign-In (Frontend)

```javascript
// Sử dụng Google Sign-In SDK
<script src="https://accounts.google.com/gsi/client" async defer></script>

<div id="g_id_onload"
     data-client_id="YOUR_GOOGLE_CLIENT_ID"
     data-callback="handleGoogleSignIn">
</div>

function handleGoogleSignIn(response) {
  const idToken = response.credential;
  
  fetch('http://localhost:8080/api/v1/auth/google', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken })
  })
  .then(res => res.json())
  .then(data => {
    // Lưu access token và refresh token
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
  });
}
```

### Facebook Login (Frontend)

```javascript
// Sử dụng Facebook SDK
<script async defer crossorigin="anonymous" 
  src="https://connect.facebook.net/en_US/sdk.js"></script>

FB.init({
  appId: 'YOUR_FACEBOOK_APP_ID',
  cookie: true,
  xfbml: true,
  version: 'v12.0'
});

function loginWithFacebook() {
  FB.login(function(response) {
    if (response.authResponse) {
      const accessToken = response.authResponse.accessToken;
      
      fetch('http://localhost:8080/api/v1/auth/facebook', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accessToken })
      })
      .then(res => res.json())
      .then(data => {
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
      });
    }
  }, {scope: 'public_profile,email'});
}
```

## 6. Rebuild Project

```bash
# Windows
gradlew clean build

# Linux/Mac
./gradlew clean build
```

## 7. Chạy Server

```bash
# Windows
gradlew run

# Linux/Mac
./gradlew run
```

## 8. Test với Postman/cURL

### Test Forgot Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'
```

### Test Reset Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your-token-from-email",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
  }'
```

## 9. Lưu ý quan trọng

- Token reset password có hiệu lực **15 phút**
- Token chỉ dùng được **1 lần**
- Email phải được xác thực trước khi gửi (Gmail App Password)
- OAuth2 yêu cầu HTTPS trong production
- Kiểm tra firewall/antivirus có block port 465 (SMTP) không

## 10. Troubleshooting

### Lỗi không gửi được email:
- Kiểm tra Gmail App Password đúng chưa
- Kiểm tra "Less secure app access" đã bật chưa (nếu không dùng App Password)
- Kiểm tra port 465 có bị block không

### Lỗi OAuth2:
- Google: Kiểm tra client ID đúng chưa
- Facebook: Kiểm tra app ID và app secret
- Kiểm tra email verified trong response

### Lỗi database:
```bash
# Kiểm tra bảng đã tạo chưa
psql -U root -d financial_db_dev -c "\dt password_reset_tokens"

# Xem dữ liệu
psql -U root -d financial_db_dev -c "SELECT * FROM password_reset_tokens;"
```

