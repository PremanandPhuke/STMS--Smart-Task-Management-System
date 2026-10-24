# proto/

Contains `notification.proto`, the gRPC contract (Protocol Buffers v3).

- `service NotificationService` - the remote procedures (`SendReminder`).
- `message ReminderRequest / ReminderResponse` - the data sent and received.

Rule: **never reuse or renumber a field number** (`= 1`, `= 2`...) once clients exist. Only add new fields with new numbers.
