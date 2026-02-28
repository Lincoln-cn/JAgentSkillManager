package org.unreal.agent.skill;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentSkillResultTest {

    @Test
    void successBuilderCreatesSuccessfulResult() {
        AgentSkillResult result = AgentSkillResult.success()
            .message("Operation completed")
            .build();

        assertTrue(result.isSuccess());
        assertEquals("Operation completed", result.getMessage());
    }

    @Test
    void failureBuilderCreatesFailedResult() {
        AgentSkillResult result = AgentSkillResult.failure()
            .message("Operation failed")
            .build();

        assertFalse(result.isSuccess());
        assertEquals("Operation failed", result.getMessage());
    }

    @Test
    void builderSetsAllFields() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        AgentSkillResult result = AgentSkillResult.success()
            .message("Success message")
            .data("test data")
            .skillName("testSkill")
            .metadata(metadata)
            .build();

        assertTrue(result.isSuccess());
        assertEquals("Success message", result.getMessage());
        assertEquals("test data", result.getData());
        assertEquals("testSkill", result.getSkillName());
        assertEquals(metadata, result.getMetadata());
    }

    @Test
    void buildCreatesImmutableResult() {
        AgentSkillResult result = AgentSkillResult.success()
            .message("Original message")
            .build();

        assertEquals("Original message", result.getMessage());
    }

    @Test
    void isSuccessReturnsTrueForSuccessResult() {
        AgentSkillResult successResult = AgentSkillResult.success().build();

        assertTrue(successResult.isSuccess());
    }

    @Test
    void isSuccessReturnsFalseForFailureResult() {
        AgentSkillResult failureResult = AgentSkillResult.failure().build();

        assertFalse(failureResult.isSuccess());
    }

    @Test
    void gettersReturnCorrectValues() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("metaKey", "metaValue");
        Object data = new Object();

        AgentSkillResult result = AgentSkillResult.success()
            .message("Test message")
            .data(data)
            .skillName("testSkillName")
            .metadata(metadata)
            .build();

        assertEquals("Test message", result.getMessage());
        assertEquals(data, result.getData());
        assertEquals("testSkillName", result.getSkillName());
        assertEquals(metadata, result.getMetadata());
    }

    @Test
    void executionTimeIsSetAutomaticallyOnBuild() {
        LocalDateTime beforeBuild = LocalDateTime.now();

        AgentSkillResult result = AgentSkillResult.success().build();

        LocalDateTime afterBuild = LocalDateTime.now();

        assertNotNull(result.getExecutionTime());
        assertTrue(!result.getExecutionTime().isBefore(beforeBuild) || 
                   result.getExecutionTime().isEqual(beforeBuild));
        assertTrue(!result.getExecutionTime().isAfter(afterBuild) || 
                   result.getExecutionTime().isEqual(afterBuild));
    }

    @Test
    void executionTimeIsLocalDateTimeInstance() {
        AgentSkillResult result = AgentSkillResult.success().build();

        assertNotNull(result.getExecutionTime());
        assertTrue(result.getExecutionTime() instanceof LocalDateTime);
    }

    @Test
    void builderWorksWithNullValues() {
        AgentSkillResult result = AgentSkillResult.success()
            .message(null)
            .data(null)
            .skillName(null)
            .metadata(null)
            .build();

        assertTrue(result.isSuccess());
        assertNull(result.getMessage());
        assertNull(result.getData());
        assertNull(result.getSkillName());
        assertNull(result.getMetadata());
    }

    @Test
    void builderWorksWithEmptyStrings() {
        AgentSkillResult result = AgentSkillResult.success()
            .message("")
            .skillName("")
            .build();

        assertTrue(result.isSuccess());
        assertEquals("", result.getMessage());
        assertEquals("", result.getSkillName());
    }

    @Test
    void metadataMapIsCorrectlyStored() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        metadata.put("key3", true);

        AgentSkillResult result = AgentSkillResult.success()
            .metadata(metadata)
            .build();

        Map<String, Object> storedMetadata = result.getMetadata();
        assertNotNull(storedMetadata);
        assertEquals("value1", storedMetadata.get("key1"));
        assertEquals(123, storedMetadata.get("key2"));
        assertEquals(true, storedMetadata.get("key3"));
    }
}
