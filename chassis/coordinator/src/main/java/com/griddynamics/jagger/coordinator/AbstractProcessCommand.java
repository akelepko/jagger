package com.griddynamics.jagger.coordinator;

import java.io.Serializable;

public abstract class AbstractProcessCommand<R extends Serializable> extends AbstractCommand<R> {
    private final String processId;

    protected AbstractProcessCommand(String sessionId, String processId) {
        super(sessionId);
        this.processId = processId;
    }

    public final String getProcessId() {
        return processId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "sessionId='" + getSessionId() + '\'' +
                ", processId='" + processId + '\'' +
                '}';
    }
}
