# config/

`GrpcSecurityConfig` registers a global server interceptor that rejects any call without the correct
`x-api-key` header (`UNAUTHENTICATED`). It compares keys with `MessageDigest.isEqual` (constant time).
