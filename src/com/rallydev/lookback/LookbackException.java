package com.rallydev.lookback;

import com.google.common.base.Joiner;

import java.util.List;

/**
 * Represents an error interacting with the Lookback Api
 */
public class LookbackException extends RuntimeException {

    LookbackException(String message) {
        super(message);
    }

    LookbackException(Throwable thrown) {
        super(thrown);
    }

    LookbackException(List<String> errors) {
        super(Joiner.on(", ").join(errors));
    }
}
