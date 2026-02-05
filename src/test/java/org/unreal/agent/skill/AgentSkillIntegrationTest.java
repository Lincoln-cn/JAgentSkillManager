package org.unreal.agent.skill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.unreal.agent.skill.springai.SpringAIAgentSkillAdapter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AgentSkillIntegrationTest {

    @Autowired
    private AgentSkillManager skillManager;

    @Autowired
    private SpringAIAgentSkillAdapter springAIAgentSkillAdapter;

    @Test
    void contextLoads() {
        assertNotNull(skillManager);
        assertNotNull(springAIAgentSkillAdapter);
    }

    @Test
    void testSkillManagerIsReady() {
        // Test that skill manager is initialized and ready
        assertNotNull(skillManager.getAllSkills());
    }

    @Test
    void testSpringAIFunctionDefinitions() {
        var functionDefinitions = springAIAgentSkillAdapter.getFunctionDefinitions();
        
        // Function definitions list should exist (may be empty if no skills registered)
        assertNotNull(functionDefinitions);
    }

    @Test
    void testSkillDiscoveryInfo() {
        var discoveryInfo = springAIAgentSkillAdapter.getSkillDiscoveryInfo();
        
        // Discovery info should exist (may be empty if no skills registered)
        assertNotNull(discoveryInfo);
    }

    @Test
    void testSkillExecutionWithEmptyManager() {
        // Test that skill manager handles empty state gracefully
        var result = skillManager.executeSkill("non-existent-skill", "test request", new HashMap<>());
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not found") || result.getMessage().contains("No skill"));
    }

    @Test
    void testAllSkillsEndpoint() {
        var allSkills = springAIAgentSkillAdapter.getAllSkillsForAgentskillsIo();
        
        assertNotNull(allSkills);
        assertTrue(allSkills.containsKey("discovery"));
        assertTrue(allSkills.containsKey("activation"));
    }
}