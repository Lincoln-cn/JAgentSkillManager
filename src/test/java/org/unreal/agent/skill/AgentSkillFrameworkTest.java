package org.unreal.agent.skill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;
import org.unreal.agent.skill.manager.DefaultSkillManager;
import org.unreal.agent.skill.manager.SkillManager;
import org.unreal.agent.skill.util.InputValidationUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "agent.skill.enabled=true",
    "agent.skill.auto-register=true"
})
public class AgentSkillFrameworkTest {

    @Test
    public void testInputValidationUtils() {
        // Test skill name validation
        assertTrue(InputValidationUtils.isValidSkillName("valid-skill-name"));
        assertTrue(InputValidationUtils.isValidSkillName("valid_skill_name"));
        assertTrue(InputValidationUtils.isValidSkillName("validSkillName123"));
        
        assertFalse(InputValidationUtils.isValidSkillName("invalid skill name")); // Contains space
        assertFalse(InputValidationUtils.isValidSkillName("invalid@skill")); // Contains special character
        assertFalse(InputValidationUtils.isValidSkillName(null)); // Null value
        
        // Test path validation
        assertTrue(InputValidationUtils.isValidFilePath("valid/path/to/file"));
        assertFalse(InputValidationUtils.isValidFilePath("../../etc/passwd")); // Path traversal
        assertFalse(InputValidationUtils.isValidFilePath("path/../../../file")); // Path traversal
        
        // Test input length validation
        assertTrue(InputValidationUtils.isValidInputLength("short input"));
        assertTrue(InputValidationUtils.isValidInputLength(null)); // Null is valid
    }

    @Test
    public void testSkillManagerRegistration() {
        // Create a mock skill
        AgentSkill mockSkill = new MockAgentSkill("test-skill", "A test skill");
        
        // Create skill manager
        SkillManager skillManager = new DefaultSkillManager();
        
        // Register the skill
        skillManager.registerSkill(mockSkill);
        
        // Verify the skill was registered
        assertEquals(1, skillManager.getAllSkills().size());
        assertNotNull(skillManager.getSkill("test-skill"));
        assertEquals("test-skill", skillManager.getSkill("test-skill").getName());
        
        // Test finding skill by request
        AgentSkill foundSkill = skillManager.findSkillForRequest("need to test something");
        assertNotNull(foundSkill);
        assertEquals("test-skill", foundSkill.getName());
        
        // Unregister the skill
        skillManager.unregisterSkill("test-skill");
        
        // Verify the skill was unregistered
        assertEquals(0, skillManager.getAllSkills().size());
        assertNull(skillManager.getSkill("test-skill"));
    }

    @Test
    public void testSkillExecution() {
        // Create a mock skill
        AgentSkill mockSkill = new MockAgentSkill("execution-test", "A test skill for execution");
        
        // Create skill manager
        SkillManager skillManager = new DefaultSkillManager();
        skillManager.registerSkill(mockSkill);
        
        // Execute the skill
        AgentSkillResult result = skillManager.executeSkill("execution-test", "test request", Map.of());
        
        // Verify the result
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("execution-test", result.getSkillName());
        assertEquals("Test skill executed successfully", result.getMessage());
    }

    @Test
    public void testSkillNotFound() {
        // Create skill manager without registering any skills
        SkillManager skillManager = new DefaultSkillManager();
        
        // Try to execute a non-existent skill
        AgentSkillResult result = skillManager.executeSkill("non-existent-skill", "test request", Map.of());
        
        // Verify the result indicates failure
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("non-existent-skill", result.getSkillName());
        assertTrue(result.getMessage().contains("Skill not found"));
    }

    // Mock implementation for testing
    private static class MockAgentSkill implements AgentSkill {
        private final String name;
        private final String description;

        public MockAgentSkill(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public boolean canHandle(String request) {
            return request.toLowerCase().contains("test") || 
                   request.toLowerCase().contains("something");
        }

        @Override
        public AgentSkillResult execute(String request, Map<String, Object> parameters) {
            return AgentSkillResult.success()
                    .message("Test skill executed successfully")
                    .data("Test data")
                    .skillName(getName())
                    .build();
        }

        @Override
        public Map<String, String> getRequiredParameters() {
            return Map.of();
        }

        @Override
        public Map<String, String> getOptionalParameters() {
            return Map.of();
        }
    }
}