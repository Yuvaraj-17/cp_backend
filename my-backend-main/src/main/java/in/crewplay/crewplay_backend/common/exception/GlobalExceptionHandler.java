package in.crewplay.crewplay_backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─────────────────────────────────────────────
    // 1️⃣ Validation Errors (@Valid)
    // ─────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err ->
                        fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(
                envelope(
                        HttpStatus.BAD_REQUEST,
                        "One or more fields are invalid",
                        fieldErrors
                )
        );
    }

    // ─────────────────────────────────────────────
    // 2️⃣ Custom Business Exceptions
    // ─────────────────────────────────────────────
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(envelope(
                        ex.getStatus(),
                        ex.getMessage(),
                        null
                ));
    }

    // ─────────────────────────────────────────────
    // 3️⃣ Authorization Errors (403)
    // ─────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(envelope(
                        HttpStatus.FORBIDDEN,
                        "You do not have permission to access this resource.",
                        null
                ));
    }

    // ─────────────────────────────────────────────
    // 4️⃣ Authentication Errors (401)
    // ─────────────────────────────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
            AuthenticationException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(envelope(
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required. Include a valid Bearer token.",
                        null
                ));
    }

    // ─────────────────────────────────────────────
    // 5️⃣ Fallback (500) — logs error
    // ─────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {

        log.error("Unhandled exception occurred", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(envelope(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred.",
                        null
                ));
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────
    private Map<String, Object> envelope(
            HttpStatus status,
            String message,
            Object details) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());

        if (details != null) {
            body.put("details", details);
        }

        return body;
    }
}
