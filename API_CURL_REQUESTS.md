# API CURL Requests

## Public APIs

### 1. Subscribe to Newsletter API
**Endpoint:** `POST /api/newsletter/subscribe`

```bash
curl -X POST http://localhost:8080/api/newsletter/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Successfully subscribed to newsletter",
  "token": null
}
```

### 2. Google Authentication API
**Endpoint:** `POST /api/auth/google`

```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "googleToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@gmail.com",
    "picture": "https://lh3.googleusercontent.com/a/profile-picture"
  }'
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Google authentication successful",
  "token": null
}
```

---

## Dashboard APIs

### 1. Today's Bookings Count
**Endpoint:** `GET /api/dashboard/bookings/today`

```bash
curl -X GET http://localhost:8080/api/dashboard/bookings/today \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Today's bookings retrieved successfully",
  "todayBookings": 15
}
```

### 2. Total Customers Count
**Endpoint:** `GET /api/dashboard/customers/total`

```bash
curl -X GET http://localhost:8080/api/dashboard/customers/total \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Total customers retrieved successfully",
  "totalCustomers": 1250
}
```

### 3. Monthly Revenue
**Endpoint:** `GET /api/dashboard/revenue/monthly`

```bash
curl -X GET http://localhost:8080/api/dashboard/revenue/monthly \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Monthly revenue retrieved successfully",
  "revenueByCurrency": [
    {
      "currency": "USD",
      "totalRevenue": 95000.50
    },
    {
      "currency": "EUR",
      "totalRevenue": 25000.30
    },
    {
      "currency": "GBP",
      "totalRevenue": 5000.20
    }
  ],
  "dailyBreakdown": [
    {
      "date": "2024-01-01",
      "currency": "USD",
      "revenue": 5500.00
    },
    {
      "date": "2024-01-01",
      "currency": "EUR",
      "revenue": 1200.75
    },
    {
      "date": "2024-01-02",
      "currency": "USD",
      "revenue": 4200.75
    }
  ]
}
```

### 4. Average Ticket Price
**Endpoint:** `GET /api/dashboard/average-ticket-price`

```bash
curl -X GET http://localhost:8080/api/dashboard/average-ticket-price \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Average ticket price retrieved successfully",
  "averagePriceByCurrency": [
    {
      "currency": "USD",
      "averagePrice": 450.75,
      "totalTickets": 120
    },
    {
      "currency": "EUR",
      "averagePrice": 380.50,
      "totalTickets": 45
    },
    {
      "currency": "GBP",
      "averagePrice": 520.25,
      "totalTickets": 15
    }
  ]
}
```

### 5. Recent Bookings (Top 10)
**Endpoint:** `GET /api/dashboard/bookings/recent`

```bash
curl -X GET http://localhost:8080/api/dashboard/bookings/recent \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Recent successful bookings retrieved successfully",
  "recentBookings": [
    {
      "id": 1,
      "pnr": "ABC123",
      "origin": "NYC",
      "destination": "LAX",
      "ticketAmount": 350.00,
      "totalAmount": 385.00,
      "currency": "USD",
      "createdDate": "2024-01-15T10:30:00",
      "status": 1
    },
    {
      "id": 2,
      "pnr": "DEF456",
      "origin": "LHR",
      "destination": "CDG",
      "ticketAmount": 280.00,
      "totalAmount": 315.00,
      "currency": "EUR",
      "createdDate": "2024-01-15T09:15:00",
      "status": 1
    }
  ]
}
```

### 6. Recent Customers (Top 10)
**Endpoint:** `GET /api/dashboard/customers/recent`

```bash
curl -X GET http://localhost:8080/api/dashboard/customers/recent \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Recent customers retrieved successfully",
  "recentCustomers": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "createdDate": "2024-01-15T09:00:00",
      "status": 1
    }
  ]
}
```

### 7. Booking Target (Monthly)
**Endpoint:** `GET /api/dashboard/bookings/target`

```bash
curl -X GET http://localhost:8080/api/dashboard/bookings/target \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Booking target retrieved successfully",
  "targetBookings": 100,
  "actualBookings": 85,
  "achievementPercentage": 85.0
}
```

### 8. Revenue Target (Monthly)
**Endpoint:** `GET /api/dashboard/revenue/target`

```bash
curl -X GET http://localhost:8080/api/dashboard/revenue/target \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "status": 200,
  "message": "Revenue target retrieved successfully",
  "targetByCurrency": [
    {
      "currency": "USD",
      "targetRevenue": 50000.0,
      "actualRevenue": 42500.75,
      "achievementPercentage": 85.0
    },
    {
      "currency": "EUR",
      "targetRevenue": 40000.0,
      "actualRevenue": 35000.50,
      "achievementPercentage": 87.5
    },
    {
      "currency": "GBP",
      "targetRevenue": 35000.0,
      "actualRevenue": 28000.25,
      "achievementPercentage": 80.0
    }
  ]
}
```

---

## User Management APIs

### 9. Update User
**Endpoint:** `PUT /api/v1/user/{userId}`

```bash
curl -X PUT http://localhost:8080/api/v1/user/123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "updated_username",
    "email": "updated.email@example.com",
    "phoneNumber": "+1234567890",
    "fullName": "Updated Full Name",
    "roleId": 2,
    "status": 1
  }'
```

**Response Example:**
```json
{
  "status": 200,
  "message": "User updated successfully",
  "token": null
}
```

---

## Example Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "message": "Email is required",
  "token": null
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "User not found",
  "token": null
}
```

### 409 Conflict
```json
{
  "status": 409,
  "message": "Email already subscribed to newsletter",
  "token": null
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "Unable to process request",
  "token": null
}
```

---

## Notes

1. **Base URL**: Replace `http://localhost:8080` with your actual server URL
2. **Authentication**: Dashboard APIs may require JWT authentication. Add the Authorization header when needed:
   ```bash
   -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```
3. **Content-Type**: Always include `Content-Type: application/json` for POST/PUT requests
4. **Optional Fields**: In the update user API, all fields are optional. Only include the fields you want to update
5. **Date Format**: All dates are in ISO-8601 format (yyyy-MM-ddTHH:mm:ss)
6. **Multi-Currency Support**: Revenue and pricing APIs now support multiple currencies with separate calculations per currency
7. **Successful Bookings Only**: Recent bookings API returns only successful bookings (status = 1)

## Testing with Postman

You can also import these requests into Postman by creating a new collection and adding each request with the appropriate:
- Method (GET, POST, PUT)
- URL
- Headers
- Request Body (for POST/PUT requests)

## Authentication Flow

For protected endpoints, first authenticate using the login API:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'
```

Then use the returned JWT token in the Authorization header for subsequent requests. 