package fptu.sba301.ats.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    // =============================
    // 1. Validation @Valid error
    // =============================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(error);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    // =============================
    // 2. ConstraintViolation (path param, request param)
    // =============================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(error);
    }

    // =============================
    // 3. Spring Security Auth Exceptions
    // =============================
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledException(
            DisabledException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.UNAUTHORIZED,
                "Your account has not been activated. Please contact your administrator.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLockedException(
            LockedException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.FORBIDDEN,
                "Your account has been locked. Please contact your administrator.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // =============================
    // 4. Business Exception
    // =============================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // =============================
    // 4. JWT Exception
    // =============================
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwtException(
            JwtException ex,
            HttpServletRequest request) {

        log.warn("JWT error: {}", ex.getMessage());

        ApiError error = buildError(
                HttpStatus.UNAUTHORIZED,
                "Invalid or Expired Token",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // =============================
    // 5. Access Denied (403)
    // =============================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // =============================
    // 6. Request Parsing & Payload (400, 413)
    // =============================
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter value for: " + ex.getName(),
                request.getRequestURI(),
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request or unreadable request body",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Uploaded file exceeds maximum allowed size",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // =============================
    // 7. Concurrency & Constraint Violations (409)
    // =============================
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.CONFLICT,
                "Database constraint violation or duplicate entry",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingFailure(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {

        ApiError error = buildError(
                HttpStatus.CONFLICT,
                "Resource was modified by another transaction. Please refresh and try again.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // =============================
    // 8. Generic Exception
    // =============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception: ", ex);

        ApiError error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // =============================
    // Helper
    // =============================
    private ApiError buildError(HttpStatus status,
                                String message,
                                String path,
                                List<String> details) {

        return ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}
