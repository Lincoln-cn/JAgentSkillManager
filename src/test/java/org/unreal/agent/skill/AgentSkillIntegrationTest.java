package org.unreal.agent.skill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AgentSkillIntegrationTest {

    @Autowired
    private AgentSkillManager skillManager;

    @Test
    void contextLoads() {
        assertNotNull(skillManager);
    }

    @Test
    void testSkillManagerIsReady() {
        // Test that skill manager is initialized and ready
        assertNotNull(skillManager.getAllSkills());
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
    void testFindSkillWithEmptyManager() {
        // Test that find skill returns null when no skills registered
        var skill = skillManager.findSkillForRequest("test request");
        
        assertNull(skill);
    }

    @Test
    void testGetSkillWithEmptyManager() {
        // Test that get skill returns null for non-existent skill
        var skill = skillManager.getSkill("non-existent-skill");
        
        assertNull(skill);
    }
}
