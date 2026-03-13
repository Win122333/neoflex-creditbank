package neoflex.chulkov.exception;

import neoflex.chulkov.dto.enums.ScoringError;

public class ScoringException extends RuntimeException {
    private ScoringError msg;
    public ScoringException(ScoringError msg) {
        super(msg.getMessage());
    }
}
