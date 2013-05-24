package com.rallydev.lookback;

public class LookbackException extends RuntimeException {

    LookbackException(String message) {
        super(message);
    }

    LookbackException(Throwable thrown) {
        super(thrown);
    }
}
