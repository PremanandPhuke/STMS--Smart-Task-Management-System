# exception/

Custom exceptions plus `GlobalExceptionHandler` (`@RestControllerAdvice`) that converts every error into one
consistent JSON body (`ApiError`) with the right HTTP status (400, 401, 403, 404, 409, 500).
