# Wallet Service - User Stories

## Phase 1: Basic Reactive CRUD Operations

### Epic: Wallet Management
As a user, I want to manage my digital wallet so that I can store and track my balance.

---

### Story 1.1: Create Wallet
**As a** user  
**I want to** create a new wallet  
**So that** I can start managing my money digitally

**Acceptance Criteria:**
- Given I provide a valid userId
- When I call POST /api/wallets
- Then a new wallet is created with balance = 0.00
- And the wallet is saved in the database
- And I receive the wallet details (id, userId, balance, timestamps)

**Technical Notes:**
- Returns `Mono<Wallet>`
- Initial balance should be BigDecimal.ZERO
- Set createdAt and updatedAt timestamps

**Example Request:**
```json
POST /api/wallets
{
  "userId": "user123"
}
```

**Example Response:**
```json
{
  "id": 1,
  "userId": "user123",
  "balance": 0.00,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T10:30:00"
}
```

---

### Story 1.2: Get Wallet Balance
**As a** user  
**I want to** view my current wallet balance  
**So that** I know how much money I have available

**Acceptance Criteria:**
- Given a wallet exists for my userId
- When I call GET /api/wallets/{userId}
- Then I receive my current wallet details
- And if wallet doesn't exist, I get a 404 error

**Technical Notes:**
- Returns `Mono<Wallet>`
- Use `.switchIfEmpty()` for error handling
- Repository method: `findByUserId()`

**Example Request:**
```
GET /api/wallets/user123
```

**Example Response:**
```json
{
  "id": 1,
  "userId": "user123",
  "balance": 150.50,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T11:45:00"
}
```

---

### Story 1.3: Deposit Money
**As a** user  
**I want to** add money to my wallet  
**So that** I can increase my available balance

**Acceptance Criteria:**
- Given my wallet exists
- And I provide a valid positive amount
- When I call POST /api/wallets/{userId}/deposit
- Then the amount is added to my current balance
- And the updatedAt timestamp is refreshed
- And I receive the updated wallet details
- And if amount is negative or zero, I get an error
- And if wallet doesn't exist, I get a 404 error

**Technical Notes:**
- Returns `Mono<Wallet>`
- Use `.flatMap()` to fetch wallet, update it, and save
- Update balance: `currentBalance.add(depositAmount)`
- Validate amount > 0

**Example Request:**
```json
POST /api/wallets/user123/deposit
{
  "amount": 100.00
}
```

**Example Response:**
```json
{
  "id": 1,
  "userId": "user123",
  "balance": 250.50,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T12:00:00"
}
```

---

### Story 1.4: Withdraw Money
**As a** user  
**I want to** remove money from my wallet  
**So that** I can use my funds

**Acceptance Criteria:**
- Given my wallet exists
- And I provide a valid positive amount
- And my balance is sufficient (balance >= amount)
- When I call POST /api/wallets/{userId}/withdraw
- Then the amount is subtracted from my current balance
- And the updatedAt timestamp is refreshed
- And I receive the updated wallet details
- And if amount is negative or zero, I get an error
- And if balance is insufficient, I get an error
- And if wallet doesn't exist, I get a 404 error

**Technical Notes:**
- Returns `Mono<Wallet>`
- Use `.flatMap()` chain: fetch → validate → update → save
- Validate: `currentBalance.compareTo(withdrawAmount) >= 0`
- Update balance: `currentBalance.subtract(withdrawAmount)`

**Example Request:**
```json
POST /api/wallets/user123/withdraw
{
  "amount": 50.00
}
```

**Example Response:**
```json
{
  "id": 1,
  "userId": "user123",
  "balance": 200.50,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T12:15:00"
}
```

---

## Phase 2: Business Logic - Deposit Limits

### Epic: Deposit Limit Enforcement
As a platform, I want to enforce daily deposit limits so that I can comply with responsible gambling regulations.

---

### Story 2.1: Configure Daily Deposit Limit
**As a** user  
**I want to** have a daily deposit limit set on my wallet  
**So that** I'm protected from overspending

**Acceptance Criteria:**
- Given a wallet exists
- When the wallet is created
- Then a default daily deposit limit is set (e.g., $1000)
- And the limit can be configured per wallet

**Technical Notes:**
- Add `dailyDepositLimit` field to Wallet entity (default: 1000.00)
- You can make it configurable later

---

### Story 2.2: Track Deposits Within 24 Hours
**As a** system  
**I want to** track all deposits made in the last 24 hours  
**So that** I can enforce rolling window limits

**Acceptance Criteria:**
- Given deposits are being made
- When I query deposits for a userId
- Then I can retrieve all deposits from the last 24 hours
- And calculate the total deposited amount

**Technical Notes:**
- Create `Transaction` entity (id, walletId, type, amount, timestamp)
- Create `TransactionRepository`
- Method: `findByWalletIdAndTypeAndTimestampAfter()`
- Use `.flatMap()` and `.reduce()` to sum amounts

---

### Story 2.3: Enforce Daily Deposit Limit
**As a** user  
**I want to** be prevented from depositing more than my daily limit  
**So that** I don't overspend

**Acceptance Criteria:**
- Given my wallet has a daily deposit limit of $1000
- And I've already deposited $800 in the last 24 hours
- When I try to deposit $300
- Then the deposit is rejected with error "Daily deposit limit exceeded"
- And my balance remains unchanged
- And if the deposit is within limits, it succeeds

**Technical Notes:**
- Before processing deposit:
  1. Fetch all deposits in last 24 hours (reactive query)
  2. Sum the amounts using `.reduce(BigDecimal.ZERO, BigDecimal::add)`
  3. Check if `(sumDeposits + newAmount) <= dailyLimit`
  4. If yes, proceed with deposit
  5. If no, return error using `Mono.error()`

**Example Flow:**
```
1. User deposits $300
2. System fetches last 24h deposits: [$200, $300, $300] = $800
3. Check: $800 + $300 = $1100 > $1000 (limit)
4. Reject: "Daily deposit limit exceeded"
```

---

## Phase 3: Caching with Redis

### Story 3.1: Cache Wallet Balance
**As a** system  
**I want to** cache wallet balances in Redis  
**So that** I can serve balance queries faster

**Acceptance Criteria:**
- Given a wallet balance is fetched from database
- When I query the same wallet again within cache TTL
- Then the balance is returned from Redis cache
- And database query is skipped
- And cache expires after configured TTL (e.g., 5 minutes)

**Technical Notes:**
- Use `@Cacheable` annotation or manual Redis operations
- Cache key: `wallet:{userId}`
- TTL: 300 seconds (5 minutes)
- Remember to invalidate cache on deposit/withdraw

---

### Story 3.2: Invalidate Cache on Balance Change
**As a** system  
**I want to** clear cached balance when money is deposited or withdrawn  
**So that** users always see accurate balances

**Acceptance Criteria:**
- Given a cached wallet balance exists
- When a deposit or withdrawal occurs
- Then the cache entry is deleted
- And the next balance query fetches fresh data from database

**Technical Notes:**
- Use `@CacheEvict` or manual cache deletion
- Evict on: deposit, withdraw operations

---

## Phase 4: Event-Driven Architecture with Kafka

### Story 4.1: Publish Transaction Events
**As a** system  
**I want to** publish events when transactions occur  
**So that** other services can react to wallet changes

**Acceptance Criteria:**
- Given a deposit or withdrawal is successful
- When the transaction completes
- Then a "WalletTransactionEvent" is published to Kafka topic
- And the event contains: userId, transactionType, amount, newBalance, timestamp

**Technical Notes:**
- Create Kafka producer
- Topic: `wallet.transactions`
- Event payload: JSON with transaction details
- Use reactive Kafka sender

---

### Story 4.2: Consume Transaction Events
**As a** reporting service  
**I want to** listen to transaction events  
**So that** I can build transaction history reports

**Acceptance Criteria:**
- Given transaction events are published to Kafka
- When I consume events from the topic
- Then I can log or process each transaction
- And track all wallet activities across the platform

**Technical Notes:**
- Create Kafka consumer
- Subscribe to: `wallet.transactions`
- Simple consumer: just log events for now
- Later: store in separate reporting database

---

## Phase 5: Transaction History

### Story 5.1: View Transaction History
**As a** user  
**I want to** view my transaction history  
**So that** I can track my wallet activity

**Acceptance Criteria:**
- Given I have made deposits and withdrawals
- When I call GET /api/wallets/{userId}/transactions
- Then I receive a list of all my transactions
- And transactions are sorted by timestamp (newest first)
- And each transaction shows: type, amount, timestamp, resulting balance

**Technical Notes:**
- Returns `Flux<Transaction>`
- Query: `findByWalletIdOrderByTimestampDesc()`
- Paginate if needed (add page, size params)

---

## Phase 6: Outbox Pattern

### Story 6.1: Guarantee Event Publishing
**As a** system  
**I want to** ensure events are published even if Kafka is temporarily down  
**So that** I don't lose transaction events

**Acceptance Criteria:**
- Given a transaction is saved to database
- When the transaction commits
- Then an event record is saved to outbox table in the same transaction
- And a background job processes outbox entries
- And successfully published events are deleted from outbox

**Technical Notes:**
- Create `Outbox` table (id, eventType, payload, status, timestamp)
- Save to outbox in same DB transaction as wallet update
- Scheduled job: poll outbox, publish to Kafka, mark as sent

---

## Testing Stories (Across All Phases)

### Story T.1: API Testing with WebTestClient
**As a** developer  
**I want to** write integration tests for all endpoints  
**So that** I can verify my reactive code works correctly

**Acceptance Criteria:**
- Given I have implemented an endpoint
- When I write a test using WebTestClient
- Then I can verify the reactive behavior
- And test error cases (wallet not found, insufficient balance, etc.)

---

### Story T.2: Unit Testing Reactive Streams
**As a** developer  
**I want to** unit test my service layer methods  
**So that** I can verify business logic independently

**Acceptance Criteria:**
- Given I have a service method returning Mono/Flux
- When I write a test using StepVerifier
- Then I can verify the emitted values
- And test error scenarios

---

## Definition of Done (for each story)

✅ Code written and compiles  
✅ Endpoint works via curl/Postman  
✅ Data persists in database (if applicable)  
✅ Error cases handled appropriately  
✅ You understand what each line of code does  
✅ Code committed to Git with meaningful message  

---

## Priority Order

**Week 1-2**: Stories 1.1, 1.2, 1.3, 1.4 (Phase 1)  
**Week 3**: Stories 2.1, 2.2, 2.3 (Phase 2)  
**Week 4**: Stories 3.1, 3.2 (Phase 3)  
**Week 5**: Stories 4.1, 4.2 (Phase 4)  
**Week 6**: Story 5.1 (Phase 5)  
**Week 7-8**: Story 6.1 (Phase 6)  

---

## How to Use These Stories

1. **Pick one story at a time** - Don't try to build everything at once
2. **Read the acceptance criteria** - These are your test cases
3. **Check the technical notes** - These give you implementation hints
4. **Build incrementally** - Get it working, then refactor
5. **Test manually first** - Use curl or Postman to verify
6. **Mark it done** - Check off the Definition of Done
7. **Move to next story** - Keep momentum going

Start with Story 1.1 tonight. Just get that first endpoint working. Everything else builds on that foundation.

Good luck! 🚀
