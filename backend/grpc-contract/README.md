# grpc-contract/

**Why this module exists:** both services must agree on the exact shape of the messages they exchange.
Keeping the contract in its own module means it is defined once and the generated classes are shared.

At build time the `protobuf-maven-plugin` reads `src/main/proto/*.proto` and generates:
- message classes (`ReminderRequest`, `ReminderResponse`)
- `NotificationServiceGrpc` (client stubs and the server base class)

Nothing is written by hand here. Build it first: `mvn install` (the parent build does this for you).
