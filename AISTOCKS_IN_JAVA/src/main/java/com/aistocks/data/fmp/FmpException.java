package com.aistocks.data.fmp;

public class FmpException extends RuntimeException {
    public FmpException(String message) {
        super(message);
    }

    public FmpException(String message, Throwable cause) {
        super(message, cause);
    }
}
