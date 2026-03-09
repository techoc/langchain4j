package dev.langchain4j.agentic;

import dev.langchain4j.agentic.declarative.TypedKey;
import org.junit.jupiter.api.Test;

import static dev.langchain4j.agentic.AgenticServices.agentBuilder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for issue #4682: NonAIAgent does not support typedOutputKey
 */
public class NonAiAgentTypedKeyIT {

    public interface ExampleKey extends TypedKey<String> {
    }

    public interface NonAiAgentWithTypedKey {
        @Agent(value = "Do anything", typedOutputKey = ExampleKey.class)
        String doAnything();
    }

    @Test
    void should_support_typedOutputKey_in_non_ai_agent() {
        // This test verifies that NonAIAgent properly supports typedOutputKey
        // Previously, this would throw AgenticSystemConfigurationException
        // with error: "Conflicting types for key '': void and java.lang.String"

        NonAiAgentWithTypedKey agent = agentBuilder(NonAiAgentWithTypedKey.class).build();

        assertThat(agent).isNotNull();
        
        String result = agent.doAnything();
        assertThat(result).isNotNull();
    }

    public interface NonAiAgentWithOutputKey {
        @Agent(value = "Do something", outputKey = "result")
        String doSomething();
    }

    @Test
    void should_support_outputKey_in_non_ai_agent() {
        // This test verifies that the regular outputKey still works

        NonAiAgentWithOutputKey agent = agentBuilder(NonAiAgentWithOutputKey.class).build();

        assertThat(agent).isNotNull();
        
        String result = agent.doSomething();
        assertThat(result).isNotNull();
    }

    public static class CustomKey implements TypedKey<String> {
        @Override
        public String defaultValue() {
            return "default";
        }

        @Override
        public String name() {
            return "customKey";
        }
    }

    public interface NonAiAgentWithCustomTypedKey {
        @Agent(value = "Custom action", typedOutputKey = CustomKey.class)
        String performCustomAction();
    }

    @Test
    void should_support_custom_typed_key_in_non_ai_agent() {
        // This test verifies that custom TypedKey implementations work

        NonAiAgentWithCustomTypedKey agent = agentBuilder(NonAiAgentWithCustomTypedKey.class).build();

        assertThat(agent).isNotNull();
        
        String result = agent.performCustomAction();
        assertThat(result).isNotNull();
    }
}
