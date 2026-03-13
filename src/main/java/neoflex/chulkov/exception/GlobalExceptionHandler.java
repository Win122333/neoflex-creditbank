package neoflex.chulkov.exception;

import neoflex.chulkov.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ScoringException.class)
    public ResponseEntity<ErrorResponseDto> handleScoringException(ScoringException e) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), e.getErr().getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(), e.getMessage(), "Ошибка валидации");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
