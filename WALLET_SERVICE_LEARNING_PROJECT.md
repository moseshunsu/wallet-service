# Wallet Service Learning Project

## Purpose
This is a **learning project** to practice Spring WebFlux, reactive programming, and the tech stack you use at work (PostgreSQL, Redis, Kafka, etc.) in a simplified environment. This is NOT a production system and does NOT require real payment integrations.

## Why This Project?
- **Simplified domain**: Wallet operations mirror your gambling platform's deposit/withdrawal logic
- **Same tech stack**: Spring WebFlux, PostgreSQL, Redis, Kafka - just like work
- **No complex codebase**: You build it from scratch, so you understand every line
- **Incremental learning**: Each phase adds ONE new technology
- **Build confidence**: Writing code yourself without AI dependency

---

## IMPORTANT CLARIFICATIONS

### No External Payment Services Needed
You are NOT integrating with Paystack, PIQ, or any real payment provider. This is a mock system where:
- "Deposit" = just add money to a database record (no real money)
- "Withdraw" = just subtract money from a database record
- You're learning reactive patterns, not payment processing

### Keep It Simple
- One service (not microservices)
- SQLite or PostgreSQL running locally
- Basic validation only
- No authentication/authorization (learning focus is on reactive patterns)

---

## Tech Stack

**Core (Phase 1)**
- Spring Boot 3.x
- Spring WebFlux (reactive web framework)
- R2DBC with PostgreSQL (reactive database driver)
- Lombok (reduce boilerplate)

**Phase 2 Additions**
- Redis (caching)
- Spring Data Redis Reactive

**Phase 3 Additions**
- Apache Kafka
- Spring Kafka

---

## Project Phases

### Phase 1: Basic Reactive CRUD (Week 1-2)
**Goal**: Get comfortable with Mono, Flux, and reactive database operations

**Endpoints to build**:
1. `POST /api/wallets` - Create a wallet for a user
2. `GET /api/wallets/{userId}` - Get wallet balance
3. `POST /api/wallets/{userId}/deposit` - Add money to wallet
4. `POST /api/wallets/{userId}/withdraw` - Remove money from wallet

**What you'll learn**:
- How to return `Mono<T>` and `Flux<T>` from controllers
- Reactive database queries with R2DBC
- `.map()`, `.flatMap()`, `.filter()` operators
- Error handling in reactive streams

**Success criteria**:
- All 4 endpoints work
- Data persists in PostgreSQL
- You wrote the code yourself (no AI generation)

---

### Phase 2: Deposit Limits (Week 3)
**Goal**: Implement business logic similar to your work

**Feature**: Daily deposit limit per user
- Each user has a maximum daily deposit limit (e.g., $1000)
- Track all deposits in the last 24 hours
- Reject deposits that would exceed the limit

**What you'll learn**:
- Complex queries in reactive context
- Business logic in reactive streams
- Working with time windows (like your rolling window feature at work!)

**Database addition**:
- `deposit_limit` table to track deposit limits
- `transaction` table to log all deposits/withdrawals with timestamps

**Success criteria**:
- User cannot deposit more than daily limit
- You can query deposits in last 24 hours
- Proper error messages when limit exceeded

---

### Phase 3: Redis Caching (Week 4)
**Goal**: Learn reactive caching patterns

**Feature**: Cache wallet balances
- When you fetch a wallet balance, cache it in Redis
- When deposit/withdraw happens, invalidate the cache
- If cache hit, return immediately without DB query

**What you'll learn**:
- Spring Data Redis Reactive
- Cache invalidation strategies
- Combining multiple reactive operations (DB + Redis)

**Success criteria**:
- First balance fetch hits DB, subsequent fetches hit Redis
- Cache clears when money is deposited/withdrawn
- Observe performance improvement

---

### Phase 4: Kafka Events (Week 5-6)
**Goal**: Learn event-driven patterns

**Feature**: Publish events for important operations
- Publish `DepositMade` event when deposit succeeds
- Publish `WithdrawalMade` event when withdrawal succeeds
- Publish `LimitExceeded` event when deposit rejected due to limit
- Create a simple consumer that logs these events

**What you'll learn**:
- Producing messages to Kafka
- Consuming messages from Kafka
- Event-driven architecture patterns
- Reactive Kafka integration

**Success criteria**:
- Events are published to Kafka topics
- Consumer logs the events
- System continues to work even if Kafka is down (graceful degradation)

---

### Phase 5 (Optional): Outbox Pattern
**Goal**: Understand reliable event publishing

**Feature**: Implement transactional outbox
- When deposit happens, write to both wallet table AND outbox table in one transaction
- Background process reads outbox and publishes to Kafka
- Ensures events are never lost

**What you'll learn**:
- Transactional consistency
- Eventual consistency patterns
- Why outbox pattern exists (probably used at your company!)

---

## Getting Started

### Prerequisites
- Java 17 or higher
- Docker (for PostgreSQL, Redis, Kafka)
- Your IDE (IntelliJ IDEA recommended)

### Initial Setup (Phase 1)

**1. Create Spring Boot Project**

Go to [start.spring.io](https://start.spring.io) and select:
- Spring Boot: 3.2.x or higher
- Dependencies:
  - Spring Reactive Web
  - Spring Data R2DBC
  - PostgreSQL Driver
  - R2DBC Driver for PostgreSQL
  - Lombok
  - Spring Boot DevTools (for hot reload)

**2. Start PostgreSQL with Docker**

Create a `compose.yaml` file in your project root:

```yaml
services:
  wallet-db:
    image: 'postgres:18'
    environment:
      - 'POSTGRES_DB=wallet_db'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=myuser'
    ports:
      - '5431:5432'
```

Then start it with:

```bash
docker compose up -d
```

**3. Configure application.yaml**

```yaml
server:
  port: 8081

spring:
  application:
    name: wallet-service

  # R2DBC PostgreSQL Configuration
  r2dbc:
    url: r2dbc:postgresql://localhost:5431/wallet_db
    username: myuser
    password: secret
    # Connection Pool Configuration
    pool:
      initial-size: 10
      max-size: 20

logging:
  level:
    org.springframework.r2dbc: DEBUG
```

**4. Setup Liquibase for Database Migrations**

Add Liquibase dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

Create the changelog structure:

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.yaml
        ├── changes/
        │   ├── 001-initial-setup.yaml
        │   └── 002-create-wallet-table.yaml
        └── sql/
            └── 002-create-wallet-table.sql
```

**Master changelog** (`db.changelog-master.yaml`):

```yaml
databaseChangeLog:
  - includeAll:
      path: db/changelog/changes/
```

**Example migration** (`changes/002-create-wallet-table.yaml`):

```yaml
databaseChangeLog:
  - changeSet:
      id: 002-create-wallet-table
      author: your-name
      comment: Create wallet table
      changes:
        - sqlFile:
            path: db/changelog/sql/002-create-wallet-table.sql
```

**SQL file** (`sql/002-create-wallet-table.sql`):

```sql
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    balance DECIMAL(19, 4) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallet_user_id ON wallets(user_id);
```

Liquibase will automatically run migrations on application startup.

**5. Create Your First Entity**

```java
@Data
@Table("wallets")
public class Wallet {
    @Id
    private Long id;
    private String userId;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**6. Create Repository**

```java
public interface WalletRepository extends ReactiveCrudRepository<Wallet, Long> {
    Mono<Wallet> findByUserId(String userId);
}
```

**7. Create Your First Controller**

```java
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletRepository walletRepository;
    
    @PostMapping
    public Mono<Wallet> createWallet(@RequestBody CreateWalletRequest request) {
        Wallet wallet = new Wallet();
        wallet.setUserId(request.getUserId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        
        return walletRepository.save(wallet);
    }
    
    @GetMapping("/{userId}")
    public Mono<Wallet> getWallet(@PathVariable String userId) {
        return walletRepository.findByUserId(userId)
            .switchIfEmpty(Mono.error(new RuntimeException("Wallet not found")));
    }
}
```

**8. Test It**

```bash
# Create a wallet
curl -X POST http://localhost:8081/api/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123"}'

# Get wallet
curl http://localhost:8081/api/wallets/user123
```

Note: Using port 8081 (not default 8080) to avoid conflicts.

---

## Development Approach

### Rules for Learning
1. **Write code yourself** - Don't copy-paste from AI
2. **Understand each line** - If you don't understand something, look it up
3. **Make mistakes** - Errors teach you more than success
4. **Keep it simple** - Don't add features not in the plan
5. **Commit frequently** - Small commits help you track progress

### How to Use AI (If You Must)
- ✅ "Explain how `.flatMap()` works in reactive streams"
- ✅ "What's the difference between `map()` and `flatMap()`?"
- ✅ "Review my code and suggest improvements"
- ❌ "Write me a deposit endpoint in WebFlux"
- ❌ "Generate a wallet service for me"

### When You Get Stuck
1. Read the error message carefully
2. Check Spring WebFlux documentation
3. Look at similar code you've written
4. Try breaking the problem into smaller pieces
5. Ask specific questions (not "how do I build X")

---

## Phase 1 Detailed Walkthrough

### Step 1: Deposit Endpoint

**Goal**: Add money to a wallet

**Thought process**:
1. Find the wallet by userId (returns `Mono<Wallet>`)
2. Update the balance (transform the wallet)
3. Save it back to database
4. Return the updated wallet

**Implementation**:

```java
@PostMapping("/{userId}/deposit")
public Mono<Wallet> deposit(
    @PathVariable String userId, 
    @RequestBody DepositRequest request) {
    
    return walletRepository.findByUserId(userId)
        .switchIfEmpty(Mono.error(new RuntimeException("Wallet not found")))
        .flatMap(wallet -> {
            // Update balance
            BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
            wallet.setBalance(newBalance);
            wallet.setUpdatedAt(LocalDateTime.now());
            
            // Save and return
            return walletRepository.save(wallet);
        });
}
```

**What's happening**:
- `findByUserId()` returns `Mono<Wallet>` (might be empty)
- `.switchIfEmpty()` handles case where wallet doesn't exist
- `.flatMap()` transforms the wallet AND returns another Mono (from `save()`)
- The whole chain returns `Mono<Wallet>`

**Key pattern**: Find → Transform → Save → Return

This is probably 80% of what you do at work!

### Step 2: Withdraw Endpoint

**Your turn!** Try implementing this yourself using the deposit endpoint as a template.

**Hints**:
- It's almost identical to deposit
- Use `.subtract()` instead of `.add()`
- Consider: what if balance is insufficient?

---

## Common Reactive Patterns

### Pattern 1: Simple Query
```java
// Find one item
Mono<User> user = userRepository.findById(id);

// Find multiple items
Flux<User> users = userRepository.findAll();
```

### Pattern 2: Transform Data
```java
// Change each item
Mono<String> username = userRepository.findById(id)
    .map(user -> user.getName());

// Chain operations that return Mono/Flux
Mono<Order> order = userRepository.findById(id)
    .flatMap(user -> orderRepository.findByUserId(user.getId()));
```

### Pattern 3: Conditional Logic
```java
return walletRepository.findByUserId(userId)
    .flatMap(wallet -> {
        if (wallet.getBalance().compareTo(amount) < 0) {
            return Mono.error(new InsufficientBalanceException());
        }
        return processWithdrawal(wallet, amount);
    });
```

### Pattern 4: Combining Multiple Operations
```java
return walletRepository.findByUserId(userId)
    .flatMap(wallet -> {
        // Update wallet
        wallet.setBalance(wallet.getBalance().add(amount));
        
        // Save wallet and create transaction record (both operations)
        return walletRepository.save(wallet)
            .flatMap(savedWallet -> 
                transactionRepository.save(createTransaction(savedWallet, amount))
                    .thenReturn(savedWallet) // Return wallet, not transaction
            );
    });
```

---

## Troubleshooting

### Nothing happens / No data returned
**Problem**: You forgot to `.subscribe()` or return the Mono/Flux
**Solution**: Controllers automatically subscribe. Make sure you return the Mono/Flux from your controller method.

### "Mono is not subscribable" error
**Problem**: Trying to use imperative code with reactive streams
**Solution**: Everything must stay in the reactive chain. Use `.flatMap()` not `.map()` when the function returns another Mono/Flux.

### Database connection errors
**Problem**: PostgreSQL not running or wrong credentials
**Solution**: Check Docker container is running: `docker ps`

### Data not persisting
**Problem**: Transaction not committing
**Solution**: R2DBC handles this automatically. Check your entity has `@Id` and `@Table` annotations.

---

## Resources

### Essential Reading
- [Spring WebFlux Official Docs](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Documentation](https://r2dbc.io/)
- [Project Reactor Guide](https://projectreactor.io/docs/core/release/reference/) - Understanding Mono and Flux

### Video Tutorials
- Search YouTube for "Spring WebFlux tutorial"
- Look for "Reactive Programming with Spring Boot"

### When You're Ready for More
- "Reactive Spring" by Josh Long (book)
- Baeldung articles on Spring WebFlux

---

## Milestones & Rewards

**Week 1-2**: Phase 1 complete
- ✅ You can create, read, and modify wallets reactively
- ✅ You understand Mono, Flux, flatMap
- **Reward**: You now know 50% of reactive programming

**Week 3**: Phase 2 complete
- ✅ You implemented business logic (deposit limits)
- ✅ You're comfortable with complex reactive queries
- **Reward**: You can now modify features at work with confidence

**Week 4**: Phase 3 complete
- ✅ You integrated Redis caching
- ✅ You understand reactive caching patterns
- **Reward**: You know how caching works in your company's services

**Week 5-6**: Phase 4 complete
- ✅ You published and consumed Kafka events
- ✅ You understand event-driven architecture
- **Reward**: You understand 80% of your company's tech stack

---

## Next Steps After Completion

Once you finish this project:
1. **Apply it at work**: You'll recognize patterns in your company's codebase
2. **Own a feature**: Volunteer to implement the next deposit-related feature solo
3. **Help others**: When L3 engineers ask about reactive code, you can explain it
4. **Extend the project**: Add the outbox pattern, add multiple currencies, add transaction history

---

## Final Notes

**This project is your playground**. Make mistakes. Break things. That's how you learn.

**Timeline is flexible**. If Phase 1 takes 3 weeks, that's fine. Quality over speed.

**Ask for help**. When stuck, reach out to colleagues or communities. But try solving it yourself first for 30 minutes.

**Most importantly**: By the end of this, you'll have hands-on experience with the exact tech stack you use at work, and you'll be able to confidently write reactive code independently.

Good luck! 🚀
