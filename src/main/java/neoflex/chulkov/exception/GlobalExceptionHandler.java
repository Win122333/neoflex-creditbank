package neoflex.chulkov.exception;

import neoflex.chulkov.dto.ErrorResponseDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
                HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), "Не прошёл скоринг");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                e.getFieldErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .reduce("", (x, z) -> x.concat(", ").concat(z))
                , "Ошибка валидации");
        //TODO
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
