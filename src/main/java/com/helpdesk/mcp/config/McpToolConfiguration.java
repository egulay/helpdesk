package com.helpdesk.mcp.config;

import com.helpdesk.mcp.ai.HelpdeskAssistantTools;
import com.helpdesk.mcp.tool.IssueRequesterTools;
import com.helpdesk.mcp.tool.IssueRequestTools;
import com.helpdesk.mcp.tool.IssueResponseTools;
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
