/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.agent.model;

import com.griddynamics.jagger.coordinator.AbstractCommand;
import com.griddynamics.jagger.coordinator.VoidResult;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Alexey Kiselyov
 *         Date: 05.09.11
 */
public class ManageAgent extends AbstractCommand<VoidResult> {

    public enum ActionProp {
        WAIT_BEFORE(60000L),
        NEW_MESSAGE_SERVICE_URL(""),
        HALT(false);

        private final Serializable defaultValue;

        public Serializable getDefaultValue() {
            return defaultValue;
        }

        ActionProp(Serializable defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public static Serializable extractParameter(Map<ActionProp, Serializable> parameters, ActionProp actionProp) {
        return extractParameter(parameters, actionProp, actionProp.getDefaultValue());
    }

    public static Serializable extractParameter(Map<ActionProp, Serializable> parameters, ActionProp actionProp, Serializable defaultValue) {
        return parameters == null ? defaultValue :
                parameters.containsKey(actionProp) ? parameters.get(actionProp) : defaultValue;
    }

    private Map<ActionProp, Serializable> params;

    public ManageAgent(String sessionId, Map<ActionProp, Serializable> params) {
        super(sessionId);
        this.params = params;
    }

    public Map<ActionProp, Serializable> getParams() {
        return this.params;
    }

    @Override
    public String toString() {
        return "ManageAgent{" +
                "params=" + params +
                ", sessionId='" + getSessionId() + '\'' +
                '}';
    }
}
