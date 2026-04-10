package neoflex.chulkov.exception;

import lombok.Getter;
import neoflex.chulkov.enums.ScoringError;

@Getter
public class ScoringException extends RuntimeException {
    public ScoringException(ScoringError msg) {
        super(msg.getMessage());
    }
}
