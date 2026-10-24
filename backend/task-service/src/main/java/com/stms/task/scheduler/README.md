# scheduler/

`ScheduledJobs` - background jobs.

1. **Reminders** (every 15 min, configurable) - finds open tasks due soon and calls notification-service over gRPC.
2. **Score refresh** (00:05 daily) - recalculates priority scores because tasks get more urgent as days pass.
