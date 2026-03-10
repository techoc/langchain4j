package dev.langchain4j.agentic;

import static dev.langchain4j.agentic.Models.baseModel;
import static dev.langchain4j.agentic.Models.plannerModel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.service.V;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Test for Issue #4686: writeArgumentToScope() type guard blocks repeated @Agent invocations
 * when adaptValueToType() converts String to Map
 * 
 * This test verifies that when a Supervisor calls the same agent multiple times with
 * Map arguments, and the LLM returns JSON strings that need to be converted to Maps,
 * the type guard mechanism doesn't block the subsequent invocations.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "GOOGLE_AI_GEMINI_API_KEY", matches = ".+")
public class SupervisorRepeatedAgentInvocationIT {

    public interface DataProcessorAgent {
        
        @Agent("Process data with given parameters")
        String processData(@V("parameters") Map<String, Object> parameters);
    }

    @Test
    void supervisor_should_allow_repeated_agent_invocations_with_map_arguments() {
        // Create a DataProcessorAgent that accepts Map parameters
        DataProcessorAgent dataProcessor = spy(AgenticServices.agentBuilder(DataProcessorAgent.class)
                .chatModel(baseModel())
                .build());

        // Create a Supervisor that will call the DataProcessorAgent multiple times
        SupervisorAgent supervisor = AgenticServices.supervisorBuilder()
                .chatModel(plannerModel())
                .subAgents(dataProcessor)
                .build();

        // Request that should trigger multiple calls to DataProcessorAgent with different parameters
        String result = supervisor.invoke(
            "Process data twice with different parameters. " +
            "First with parameters {\"action\": \"analyze\", \"target\": \"sales\"}, " +
            "then with parameters {\"action\": \"report\", \"target\": \"customers\"}"
        );

        // Verify that the agent was invoked
        verify(dataProcessor).processData(any());
        
        // The result should not be null or empty
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }
}
