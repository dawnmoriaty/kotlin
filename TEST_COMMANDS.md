# 🧪 QUICK TEST COMMANDS - Budget, Recurring & Debt

## 🔑 Step 1: Get Access Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\":\"your-email\",\"password\":\"your-password\"}"
```

**Save the accessToken!**

---

## 💰 Budget Tests

### Get Categories First
```bash
curl -X GET http://localhost:8080/api/v1/categories ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create Budget
```bash
curl -X POST http://localhost:8080/api/v1/budgets ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"categoryId\":\"CATEGORY_UUID\",\"amount\":\"5000000\",\"period\":\"monthly\",\"startDate\":\"2025-11-01\",\"alertPercentage\":\"80\"}"
```

### Get All Budgets
```bash
curl -X GET http://localhost:8080/api/v1/budgets ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Budget Spending
```bash
curl -X GET http://localhost:8080/api/v1/budgets/spending ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Budget Summary
```bash
curl -X GET http://localhost:8080/api/v1/budgets/summary ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update Budget
```bash
curl -X PUT http://localhost:8080/api/v1/budgets/BUDGET_UUID ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":\"6000000\",\"alertPercentage\":\"75\"}"
```

### Delete Budget
```bash
curl -X DELETE http://localhost:8080/api/v1/budgets/BUDGET_UUID ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🔄 Recurring Transaction Tests

### Create Recurring Transaction
```bash
curl -X POST http://localhost:8080/api/v1/recurring-transactions ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"categoryId\":\"CATEGORY_UUID\",\"description\":\"Tiền nhà\",\"amount\":\"3000000\",\"frequency\":\"monthly\",\"startDate\":\"2025-11-01\",\"autoCreate\":true,\"dayOfMonth\":1}"
```

### Get All Recurring Transactions
```bash
curl -X GET http://localhost:8080/api/v1/recurring-transactions ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Due Transactions
```bash
curl -X GET http://localhost:8080/api/v1/recurring-transactions/due ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Recurring Summary
```bash
curl -X GET http://localhost:8080/api/v1/recurring-transactions/summary ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Execute Recurring Transaction Manually
```bash
curl -X POST http://localhost:8080/api/v1/recurring-transactions/RECURRING_UUID/execute ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update Recurring Transaction
```bash
curl -X PUT http://localhost:8080/api/v1/recurring-transactions/RECURRING_UUID ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":\"3500000\",\"isActive\":true}"
```

### Delete Recurring Transaction
```bash
curl -X DELETE http://localhost:8080/api/v1/recurring-transactions/RECURRING_UUID ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 💳 Debt Tests

### Create Borrowed Debt
```bash
curl -X POST http://localhost:8080/api/v1/debts ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"type\":\"borrowed\",\"personName\":\"Nguyễn Văn A\",\"personContact\":\"0912345678\",\"amount\":\"10000000\",\"interestRate\":\"5.0\",\"dueDate\":\"2025-12-31\",\"description\":\"Vay tiền mua xe\"}"
```

### Create Lent Debt
```bash
curl -X POST http://localhost:8080/api/v1/debts ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"type\":\"lent\",\"personName\":\"Trần Thị B\",\"personContact\":\"0987654321\",\"amount\":\"5000000\",\"interestRate\":\"3.0\",\"dueDate\":\"2025-11-30\",\"description\":\"Cho vay bạn\"}"
```

### Get All Debts
```bash
curl -X GET http://localhost:8080/api/v1/debts ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Borrowed Debts Only
```bash
curl -X GET "http://localhost:8080/api/v1/debts?type=borrowed" ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Lent Debts Only
```bash
curl -X GET "http://localhost:8080/api/v1/debts?type=lent" ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Debt Detail
```bash
curl -X GET http://localhost:8080/api/v1/debts/DEBT_UUID/detail ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Debt Summary
```bash
curl -X GET http://localhost:8080/api/v1/debts/summary ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Overdue Debts
```bash
curl -X GET http://localhost:8080/api/v1/debts/overdue ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Add Payment to Debt
```bash
curl -X POST http://localhost:8080/api/v1/debts/DEBT_UUID/payments ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":\"2000000\",\"paymentDate\":\"2025-11-16\",\"notes\":\"Trả nợ đợt 1\"}"
```

### Get Debt Payments
```bash
curl -X GET http://localhost:8080/api/v1/debts/DEBT_UUID/payments ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update Debt
```bash
curl -X PUT http://localhost:8080/api/v1/debts/DEBT_UUID ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"interestRate\":\"4.5\",\"description\":\"Updated description\"}"
```

### Delete Payment
```bash
curl -X DELETE "http://localhost:8080/api/v1/debts/payments/PAYMENT_UUID?debtId=DEBT_UUID" ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Delete Debt
```bash
curl -X DELETE http://localhost:8080/api/v1/debts/DEBT_UUID ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 Complete Test Flow

### 1. Setup
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\":\"test@example.com\",\"password\":\"Test123!\"}"

# Get categories
curl -X GET http://localhost:8080/api/v1/categories ^
  -H "Authorization: Bearer TOKEN"
```

### 2. Test Budget Flow
```bash
# Create budget for Food category
curl -X POST http://localhost:8080/api/v1/budgets ^
  -H "Authorization: Bearer TOKEN" ^
  -d "{\"categoryId\":\"FOOD_CATEGORY_UUID\",\"amount\":\"5000000\",\"period\":\"monthly\",\"startDate\":\"2025-11-01\"}"

# Create some transactions
curl -X POST http://localhost:8080/api/v1/transactions ^
  -H "Authorization: Bearer TOKEN" ^
  -d "{\"categoryId\":\"FOOD_CATEGORY_UUID\",\"amount\":\"150000\",\"description\":\"Ăn sáng\",\"transactionDate\":\"2025-11-16\"}"

# Check spending
curl -X GET http://localhost:8080/api/v1/budgets/spending ^
  -H "Authorization: Bearer TOKEN"
```

### 3. Test Recurring Flow
```bash
# Create recurring for rent
curl -X POST http://localhost:8080/api/v1/recurring-transactions ^
  -H "Authorization: Bearer TOKEN" ^
  -d "{\"categoryId\":\"RENT_CATEGORY_UUID\",\"description\":\"Tiền nhà\",\"amount\":\"3000000\",\"frequency\":\"monthly\",\"startDate\":\"2025-11-01\",\"autoCreate\":true,\"dayOfMonth\":1}"

# Check summary
curl -X GET http://localhost:8080/api/v1/recurring-transactions/summary ^
  -H "Authorization: Bearer TOKEN"

# Execute manually
curl -X POST http://localhost:8080/api/v1/recurring-transactions/RECURRING_UUID/execute ^
  -H "Authorization: Bearer TOKEN"
```

### 4. Test Debt Flow
```bash
# Create borrowed debt
curl -X POST http://localhost:8080/api/v1/debts ^
  -H "Authorization: Bearer TOKEN" ^
  -d "{\"type\":\"borrowed\",\"personName\":\"Nguyễn Văn A\",\"amount\":\"10000000\",\"interestRate\":\"5.0\",\"dueDate\":\"2025-12-31\"}"

# Add first payment
curl -X POST http://localhost:8080/api/v1/debts/DEBT_UUID/payments ^
  -H "Authorization: Bearer TOKEN" ^
  -d "{\"amount\":\"2000000\",\"notes\":\"Trả nợ đợt 1\"}"

# Add second payment
curl -X POST http://localhost:8080/api/v1/debts/DEBT_UUID/payments ^
  -H "Authorization: Bearer TOKEN" ^
  -d "{\"amount\":\"2000000\",\"notes\":\"Trả nợ đợt 2\"}"

# Check debt detail
curl -X GET http://localhost:8080/api/v1/debts/DEBT_UUID/detail ^
  -H "Authorization: Bearer TOKEN"

# Check summary
curl -X GET http://localhost:8080/api/v1/debts/summary ^
  -H "Authorization: Bearer TOKEN"
```

---

## 📊 Expected Responses

### Budget Spending Response:
```json
{
  "success": true,
  "data": [{
    "id": "uuid",
    "categoryName": "Ăn uống",
    "budgetAmount": "5000000",
    "spentAmount": "150000",
    "remainingAmount": "4850000",
    "spentPercentage": "3.00",
    "isExceeded": false,
    "shouldAlert": false
  }]
}
```

### Recurring Summary Response:
```json
{
  "success": true,
  "data": {
    "totalActive": 1,
    "totalInactive": 0,
    "monthlyTotal": "3000000",
    "yearlyTotal": "36000000",
    "nextDueDate": "2025-12-01",
    "transactions": [...]
  }
}
```

### Debt Summary Response:
```json
{
  "success": true,
  "data": {
    "totalBorrowed": "10000000",
    "totalLent": "5000000",
    "totalBorrowedRemaining": "6000000",
    "totalLentRemaining": "5000000",
    "totalOverdue": 0,
    "borrowedDebts": [...],
    "lentDebts": [...]
  }
}
```

---

## ✅ Success Indicators

### Budget:
- ✅ Can create budget
- ✅ Spending tracked correctly
- ✅ Alert shows when > 80%
- ✅ Summary shows total budget

### Recurring:
- ✅ Can create recurring transaction
- ✅ Next occurrence calculated
- ✅ Can execute manually
- ✅ Summary shows monthly/yearly total

### Debt:
- ✅ Can create debt (borrowed/lent)
- ✅ Can add payments
- ✅ Remaining amount updates
- ✅ Status updates automatically
- ✅ Summary shows totals

---

**Happy Testing! 🚀**

