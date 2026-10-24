# grpc/

`NotificationClient` - wraps the generated gRPC stub (`@GrpcClient`). It sets a 5-second deadline and never
throws, so a notification outage cannot break the scheduler.
