package co.com.projectve.usecase.capacity.exception;

public class CapacityCalculationException extends RuntimeException {
    
    public CapacityCalculationException(String message) {
        super(message);
    }
    
    public CapacityCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
