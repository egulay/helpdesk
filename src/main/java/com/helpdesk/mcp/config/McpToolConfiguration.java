package com.helpdesk.mcp.config;

import com.helpdesk.mcp.tools.assistant.HelpdeskAssistantTools;
import com.helpdesk.mcp.tools.data.IssueRequesterTools;
import com.helpdesk.mcp.tools.data.IssueRequestTools;
import com.helpdesk.mcp.tools.data.IssueResponseTools;
import com.helpdesk.mcp.tools.data.HelpdeskMutationTools;
import lombok.val;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfiguration {

    @Bean
    public ToolCallbackProvider helpdeskToolCallbackProvider(
            IssueRequesterTools requesterTools,
            IssueRequestTools requestTools,
            IssueResponseTools responseTools,
            HelpdeskAssistantTools assistantTools,
            ObjectProvider<HelpdeskMutationTools> mutationTools
    ) {
        val toolObjects = new java.util.ArrayList<Object>();
        toolObjects.add(requesterTools);
        toolObjects.add(requestTools);
        toolObjects.add(responseTools);
        toolObjects.add(assistantTools);
        mutationTools.ifAvailable(toolObjects::add);

        return MethodToolCallbackProvider.builder()
                .toolObjects(toolObjects.toArray())
                .build();
    }
}
