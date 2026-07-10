package com.helpdesk.mcp.config;

import com.helpdesk.mcp.tooling.assistant.HelpdeskAssistantTools;
import com.helpdesk.mcp.tooling.data.IssueRequesterTools;
import com.helpdesk.mcp.tooling.data.IssueRequestTools;
import com.helpdesk.mcp.tooling.data.IssueResponseTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfiguration {

    @Bean
    public ToolCallbackProvider helpdeskToolCallbackProvider(
            IssueRequesterTools requesterTools,
            IssueRequestTools requestTools,
            IssueResponseTools responseTools,
            HelpdeskAssistantTools assistantTools
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(requesterTools, requestTools, responseTools, assistantTools).build();
    }
}
