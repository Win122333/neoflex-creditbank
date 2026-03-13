package neoflex.chulkov.exception;

import lombok.Getter;
import neoflex.chulkov.dto.enums.ScoringError;
@Getter
public class ScoringException extends RuntimeException {
    private ScoringError err;
    public ScoringException(ScoringError msg) {
        super(msg.getMessage());
        err = msg;
    }
}
