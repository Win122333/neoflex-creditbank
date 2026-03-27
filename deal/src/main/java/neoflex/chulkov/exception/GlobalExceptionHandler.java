package neoflex.chulkov.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.ErrorResponseDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Неправильно введеные данные: {}", e.getMessage());
        ErrorResponseDto response = new ErrorResponseDto()
                .status(e.getStatusCode().value())
                .error(e.getFieldErrors().stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .reduce("", (x, z) -> x.concat(" ").concat(z)))
                .message("Ошибка валидации");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpClientErrorException(HttpClientErrorException e) {
        log.warn("Неправильно введеные данные: {}", e.getMessage());

        try {
            ErrorResponseDto errorBody = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorResponseDto.class
            );
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
        catch (Exception ex) {
            ErrorResponseDto response = new ErrorResponseDto()
                    .status(e.getStatusCode().value())
                    .error("Ошибка валидации")
                    .message(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.warn("Попытка регистрации дубликата email: {}", e.getMessage());
        ErrorResponseDto response = new ErrorResponseDto()
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .error("Пользователь с таким email уже существует");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(StatementNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleStatementNotFoundException(StatementNotFoundException e) {
        log.warn("Заявка с неверным id: {}", e.getMessage());
        ErrorResponseDto response = new ErrorResponseDto()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .error("Этой заявки не существует");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidStatementStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidStatementStatusException(InvalidStatementStatusException e) {
        ErrorResponseDto response = new ErrorResponseDto()
                .status(HttpStatus.CONFLICT.value())
                .error("Заявка в неверном статусе")
                .message(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllUnhandledExceptions(Exception e) {
        log.error("Непредвиденная ошибка сервера: ", e);
        ErrorResponseDto response = new ErrorResponseDto()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Произошла непредвиденная ошибка на сервере");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
