package com.mavora.agents.application;

import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;

public interface WorkflowHandler {

    boolean supports(WorkflowType type);

    HandlerResult execute(WorkflowExecution execution, LlmGateway llm);
}
