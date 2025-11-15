# ✅ Setup Checklist - Forgot Password & OAuth2

## 📋 Pre-deployment Checklist

### 1. Database Setup
- [ ] Đã chạy migration SQL `migration_password_reset.sql`
- [ ] Bảng `password_reset_tokens` đã tạo thành công
- [ ] Indexes đã được tạo (token, user_id, expires_at)
- [ ] Test query: `SELECT * FROM password_reset_tokens;` chạy OK

**Verify:**
```sql
\dt password_reset_tokens
```

---

### 2. Email Configuration (if using forgot password)
- [ ] Đã tạo Gmail App Password
- [ ] Đã cập nhật `application.yaml`:
  - [ ] `email.username` = your Gmail
  - [ ] `email.password` = App Password (16 ký tự)
  - [ ] `email.from` = sender email
  - [ ] `email.fromName` = sender name
- [ ] `app.frontendUrl` đã được set (cho reset link)

**Test:**
```bash
# Send test email
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "your-test-email@example.com"}'
```

---

### 3. Dependencies
- [ ] `build.gradle.kts` đã có dependencies:
  - [ ] `org.apache.commons:commons-email:1.5`
  - [ ] `io.ktor:ktor-client-core`
  - [ ] `io.ktor:ktor-client-cio`
  - [ ] `io.ktor:ktor-client-content-negotiation`

**Verify:**
```bash
.\gradlew.bat dependencies | findstr "commons-email"
.\gradlew.bat dependencies | findstr "ktor-client"
```

---

### 4. Code Files Created
- [ ] `src/main/kotlin/data/database/tables/PasswordResetTokens.kt`
- [ ] `src/main/kotlin/domain/services/IEmailService.kt`
- [ ] `src/main/kotlin/domain/services/impl/EmailService.kt`
- [ ] `src/main/kotlin/dtos/request/PasswordRequest.kt`

**Verify:**
```bash
dir src\main\kotlin\domain\services\IEmailService.kt
dir src\main\kotlin\domain\services\impl\EmailService.kt
```

---

### 5. Code Files Modified
- [ ] `src/main/kotlin/Application.kt` - Added EmailService & HttpClient
- [ ] `src/main/kotlin/data/database/DatabaseFactory.kt` - Added PasswordResetTokens
- [ ] `src/main/kotlin/data/repository/IUserRepository.kt` - Added methods
- [ ] `src/main/kotlin/data/repository/impl/UserRepository.kt` - Implemented methods
- [ ] `src/main/kotlin/domain/services/IAuthService.kt` - Added methods
- [ ] `src/main/kotlin/domain/services/impl/AuthService.kt` - Implemented methods
- [ ] `src/main/kotlin/routes/AuthRoutes.kt` - Added endpoints

---

### 6. Build & Compile
- [ ] Build thành công: `.\gradlew.bat build -x test`
- [ ] Không có compilation errors
- [ ] Warnings (nếu có) không ảnh hưởng functionality

**Verify:**
```bash
.\gradlew.bat build -x test
# Expected: BUILD SUCCESSFUL
```

---

### 7. Server Startup
- [ ] Server start thành công: `.\gradlew.bat run`
- [ ] Database connection thành công
- [ ] Bảng `password_reset_tokens` được verify
- [ ] Server listening trên port 8080

**Expected logs:**
```
✅ Connected to PostgreSQL: financial_db_dev @ localhost:5432
✅ Database tables created/verified
Application started in X.XXX seconds.
```

---

### 8. API Endpoints
- [ ] POST `/api/v1/auth/forgot-password` - Works
- [ ] POST `/api/v1/auth/reset-password` - Works
- [ ] POST `/api/v1/auth/google` - Works
- [ ] POST `/api/v1/auth/facebook` - Works

**Test với cURL:**
```bash
# Test forgot password
curl -X POST http://localhost:8080/api/v1/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"test@example.com\"}"
```

---

### 9. Forgot Password Flow
- [ ] User có thể request forgot password
- [ ] Email được gửi thành công (check inbox/spam)
- [ ] Token được lưu trong database
- [ ] Token hết hạn sau 15 phút
- [ ] Token chỉ dùng được 1 lần
- [ ] User có thể reset password với token
- [ ] User có thể login với password mới

**Test:**
1. Register user
2. Request forgot password
3. Check email
4. Get token
5. Reset password
6. Login with new password

---

### 10. OAuth2 (Optional - needs frontend)
- [ ] Google OAuth2 endpoint works
- [ ] Facebook OAuth2 endpoint works
- [ ] Token verification works
- [ ] User creation/linking works
- [ ] JWT tokens generated correctly

**Note:** OAuth2 needs frontend to get idToken/accessToken

---

### 11. Error Handling
- [ ] Email không tồn tại → Error message clear
- [ ] Token hết hạn → Error message clear
- [ ] Token đã dùng → Error message clear
- [ ] Password mismatch → Error message clear
- [ ] Password quá ngắn → Error message clear
- [ ] Invalid OAuth token → Error message clear

**Test error cases:**
```bash
# Email không tồn tại
curl -X POST http://localhost:8080/api/v1/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"notexist@example.com\"}"

# Password mismatch
curl -X POST http://localhost:8080/api/v1/auth/reset-password ^
  -H "Content-Type: application/json" ^
  -d "{\"token\": \"TOKEN\", \"newPassword\": \"Pass1\", \"confirmPassword\": \"Pass2\"}"
```

---

### 12. Security
- [ ] Tokens là UUID v4 (random)
- [ ] Tokens expire sau 15 phút
- [ ] Tokens chỉ dùng 1 lần
- [ ] Password được BCrypt hash
- [ ] OAuth tokens được verify với provider
- [ ] Email verification check (OAuth)

**Verify in database:**
```sql
SELECT token, expires_at, is_used FROM password_reset_tokens ORDER BY created_at DESC LIMIT 5;
```

---

### 13. Documentation
- [ ] `README_FORGOT_PASSWORD_OAUTH.md` - Main documentation
- [ ] `QUICK_START_FORGOT_PASSWORD.md` - Quick start guide
- [ ] `FORGOT_PASSWORD_OAUTH_SUMMARY.md` - Full summary
- [ ] `FORGOT_PASSWORD_OAUTH_SETUP.md` - Detailed setup
- [ ] `TEST_API_FORGOT_PASSWORD.md` - Test cases
- [ ] `sql_helper_scripts.sql` - SQL helpers

---

### 14. Testing Tools
- [ ] cURL commands tested
- [ ] Postman collection ready
- [ ] Database helper scripts ready
- [ ] Test users created

---

### 15. Production Ready (Optional)
- [ ] Email service changed to SendGrid/AWS SES
- [ ] Rate limiting implemented
- [ ] CAPTCHA added
- [ ] Monitoring & alerts setup
- [ ] HTTPS enabled
- [ ] CORS configured correctly
- [ ] Environment variables for secrets
- [ ] Logging configured

---

## 🎯 Quick Verification Commands

### Database
```sql
-- Check table exists
\dt password_reset_tokens

-- Check structure
\d password_reset_tokens

-- View recent tokens
SELECT * FROM password_reset_tokens ORDER BY created_at DESC LIMIT 5;
```

### Build
```bash
.\gradlew.bat clean build -x test
```

### Server
```bash
.\gradlew.bat run
```

### API Test
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"Test123!\"}"

# Forgot password
curl -X POST http://localhost:8080/api/v1/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"test@example.com\"}"

# Check token in DB
psql -U root -d financial_db_dev -c "SELECT token FROM password_reset_tokens ORDER BY created_at DESC LIMIT 1;"

# Reset password
curl -X POST http://localhost:8080/api/v1/auth/reset-password ^
  -H "Content-Type: application/json" ^
  -d "{\"token\": \"YOUR_TOKEN\", \"newPassword\": \"NewPass123!\", \"confirmPassword\": \"NewPass123!\"}"

# Login with new password
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\": \"test@example.com\", \"password\": \"NewPass123!\"}"
```

---

## ✅ Sign Off

**Developer:** _________________ **Date:** _________________

**Tested by:** _________________ **Date:** _________________

**Approved for Production:** _________________ **Date:** _________________

---

## 📝 Notes

Add any notes or issues encountered during setup:

```
[Space for notes]
```

---

## 🎉 Completion

When all items are checked:
- ✅ Feature is ready for use
- ✅ Documentation is complete
- ✅ Tests are passing
- ✅ Ready for integration with frontend

**Next Steps:**
1. Integrate with frontend
2. Setup OAuth2 credentials (Google & Facebook)
3. Configure production email service
4. Deploy to staging for testing
5. Deploy to production

---

**Last Updated:** 2025-11-16

