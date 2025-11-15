# 🔧 QUICK FIX GUIDE - Compilation Errors

## Lỗi và cách fix:

### 1. Repository Insert/Update Issues

Tất cả 3 repositories cần fix tương tự Budget. Pattern:

**Before (Lỗi):**
```kotlin
val id = Table.insertAndGetId { ... }
findById(id.value)
```

**After (Đúng):**
```kotlin
val newId = UUID.randomUUID()
Table.insert { 
    it[id] = newId
    // other fields...
}
findById(newId)
```

**Update fix:**
```kotlin
// Thay value -> tên biến khác
amount?.let { amt -> it[Table.amount] = amt }
```

### 2. Missing NotFoundException

Tạo file: `src/main/kotlin/domain/exceptions/NotFoundException.kt`

```kotlin
package com.financial.domain.exceptions

class NotFoundException(message: String) : Exception(message)
```

### 3. Missing getUserId Extension

Thêm vào `Security.kt`:

```kotlin
fun ApplicationCall.getUserId(): UUID {
    val principal = principal<JWTPrincipal>()
        ?: throw AuthException("No principal found")
    val userId = principal.payload.getClaim("userId").asString()
        ?: throw AuthException("No userId in token")
    return UUID.fromString(userId)
}
```

---

## ⚡ FAST FIX - Copy & Replace

### File 1: NotFoundException.kt (CREATE)
```kotlin
package com.financial.domain.exceptions

class NotFoundException(message: String) : Exception(message)
```

### File 2: Security.kt (ADD at end)
```kotlin
// Add to existing Security.kt
fun ApplicationCall.getUserId(): UUID {
    val principal = principal<JWTPrincipal>()
        ?: throw AuthException("No principal found")
    val userId = principal.payload.getClaim("userId").asString()
        ?: throw AuthException("No userId in token")
    return UUID.fromString(userId)
}
```

### File 3-5: Fix all 3 repositories

**RecurringTransactionRepository.kt** - Line 71:
```kotlin
val recurringId = UUID.randomUUID()
RecurringTransactions.insert {
    it[id] = recurringId
    // ... rest of fields
}
findById(recurringId)!!
```

**DebtRepository.kt** - Line 100:
```kotlin
val debtId = UUID.randomUUID()
Debts.insert {
    it[id] = debtId
    // ... rest of fields
}
findById(debtId)!!
```

**DebtPaymentRepository.kt** - Line 192:
```kotlin
val paymentId = UUID.randomUUID()
DebtPayments.insert {
    it[id] = paymentId
    // ... rest of fields
}
findById(paymentId)!!
```

---

## 🚀 Quick Commands

After fixing:

```bash
# Build
.\gradlew.bat build -x test

# If success, run
.\gradlew.bat run
```

---

## ✅ Checklist

- [ ] Create NotFoundException.kt
- [ ] Add getUserId() to Security.kt
- [ ] Fix RecurringTransactionRepository insert
- [ ] Fix RecurringTransactionRepository update (rename `value` parameters)
- [ ] Fix DebtRepository insert
- [ ] Fix DebtRepository update (rename `value` parameters)
- [ ] Fix DebtPaymentRepository insert
- [ ] Build project
- [ ] Test APIs

---

**Estimated fix time: 10 minutes**

Sau khi fix xong tất cả, project sẽ build thành công! 🎉

