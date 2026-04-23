package neoflex.chulkov.exception;

import lombok.Getter;

@Getter
public class ScoringException extends RuntimeException {
    public ScoringException(String msg) {
        super(msg);
    }
}
