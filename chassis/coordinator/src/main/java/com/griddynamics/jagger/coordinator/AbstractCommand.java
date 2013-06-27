package com.griddynamics.jagger.coordinator;

import java.io.Serializable;

public abstract class AbstractCommand<R extends Serializable> implements Command<R> {
    private final String sessionId;

    protected AbstractCommand(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public final String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "sessionId='" + getSessionId() + '\'' +
                '}';
    }
}
