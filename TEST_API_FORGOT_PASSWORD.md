# Test API Script - Forgot Password & OAuth2

## 1. Register a test user
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "TestPassword123!"
  }'
```

## 2. Test Forgot Password
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Email đặt lại mật khẩu đã được gửi",
  "data": null,
  "timestamp": "2025-11-16T..."
}
```

## 3. Test Reset Password (sau khi nhận token từ email)
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_TOKEN_FROM_EMAIL",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công",
  "data": null,
  "timestamp": "2025-11-16T..."
}
```

## 4. Test Login with new password
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser@example.com",
    "password": "NewPassword123!"
  }'
```

## 5. Test Google Login (cần idToken từ Google OAuth2)
```bash
curl -X POST http://localhost:8080/api/v1/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "GOOGLE_ID_TOKEN_FROM_FRONTEND"
  }'
```

## 6. Test Facebook Login (cần accessToken từ Facebook OAuth2)
```bash
curl -X POST http://localhost:8080/api/v1/auth/facebook \
  -H "Content-Type: application/json" \
  -d '{
    "accessToken": "FACEBOOK_ACCESS_TOKEN_FROM_FRONTEND"
  }'
```

## Postman Collection JSON

```json
{
  "info": {
    "name": "Financial App - Auth with Password Reset & OAuth2",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Forgot Password",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"testuser@example.com\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/forgot-password",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "forgot-password"]
        }
      }
    },
    {
      "name": "Reset Password",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"token\": \"YOUR_TOKEN\",\n  \"newPassword\": \"NewPassword123!\",\n  \"confirmPassword\": \"NewPassword123!\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/reset-password",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "reset-password"]
        }
      }
    },
    {
      "name": "Google Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"idToken\": \"GOOGLE_ID_TOKEN\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/google",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "google"]
        }
      }
    },
    {
      "name": "Facebook Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"accessToken\": \"FACEBOOK_ACCESS_TOKEN\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/facebook",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "facebook"]
        }
      }
    }
  ]
}
```

## Error Cases to Test

### 1. Email không tồn tại
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "notexist@example.com"}'
```

### 2. Password mismatch
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "VALID_TOKEN",
    "newPassword": "Password123!",
    "confirmPassword": "DifferentPassword123!"
  }'
```

### 3. Token expired
```bash
# Wait 15+ minutes after requesting forgot password, then try reset
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "EXPIRED_TOKEN",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
  }'
```

### 4. Token already used
```bash
# Try to use the same token twice
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "USED_TOKEN",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
  }'
```

