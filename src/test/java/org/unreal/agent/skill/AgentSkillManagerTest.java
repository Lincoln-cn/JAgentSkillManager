package org.unreal.agent.skill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentSkillManagerTest {

    private AgentSkillManager skillManager;

    @Mock
    private AgentSkill mockSkill;

    @Mock
    private AgentSkillResult mockResult;

    @Mock
    private AgentSkillManager.SkillExecutionListener mockListener;

    @BeforeEach
    void setUp() {
        skillManager = new AgentSkillManager();
    }

    // ==================== Skill Registration Tests ====================

    @Test
    void registerSkill_addsSkillToManager() {
        when(mockSkill.getName()).thenReturn("test-skill");

        skillManager.registerSkill(mockSkill);

        assertNotNull(skillManager.getSkill("test-skill"));
        assertEquals(mockSkill, skillManager.getSkill("test-skill"));
    }

    @Test
    void registerSkill_throwsExceptionForNullSkill() {
        assertThrows(NullPointerException.class, () -> skillManager.registerSkill(null));
    }

    @Test
    void unregisterSkill_removesSkillFromManager() {
        when(mockSkill.getName()).thenReturn("test-skill");

        skillManager.registerSkill(mockSkill);
        assertNotNull(skillManager.getSkill("test-skill"));

        skillManager.unregisterSkill("test-skill");
        assertNull(skillManager.getSkill("test-skill"));
    }

    @Test
    void getAllSkills_returnsAllRegisteredSkills() {
        when(mockSkill.getName()).thenReturn("skill-1");
        AgentSkill mockSkill2 = mock(AgentSkill.class);
        when(mockSkill2.getName()).thenReturn("skill-2");

        skillManager.registerSkill(mockSkill);
        skillManager.registerSkill(mockSkill2);

        var skills = skillManager.getAllSkills();

        assertEquals(2, skills.size());
        assertTrue(skills.contains(mockSkill));
        assertTrue(skills.contains(mockSkill2));
    }

    @Test
    void getSkill_returnsCorrectSkillByName() {
        when(mockSkill.getName()).thenReturn("test-skill");

        skillManager.registerSkill(mockSkill);

        assertEquals(mockSkill, skillManager.getSkill("test-skill"));
    }

    @Test
    void getSkill_returnsNullForNonExistentSkill() {
        assertNull(skillManager.getSkill("non-existent"));
    }

    // ==================== Skill Lookup Tests ====================

    @Test
    void findSkillForRequest_returnsSkillThatCanHandleRequest() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.canHandle("test request")).thenReturn(true);

        skillManager.registerSkill(mockSkill);

        AgentSkill found = skillManager.findSkillForRequest("test request");

        assertNotNull(found);
        assertEquals(mockSkill, found);
        verify(mockSkill).canHandle("test request");
    }

    @Test
    void findSkillForRequest_returnsNullWhenNoSkillMatches() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.canHandle(anyString())).thenReturn(false);

        skillManager.registerSkill(mockSkill);

        AgentSkill found = skillManager.findSkillForRequest("unhandled request");

        assertNull(found);
    }

    @Test
    void findSkillForRequest_returnsAnyMatchingSkill() {
        AgentSkill skill1 = mock(AgentSkill.class);
        AgentSkill skill2 = mock(AgentSkill.class);

        // Only stub the skills we need for this test
        when(skill1.getName()).thenReturn("skill-1");
        when(skill2.getName()).thenReturn("skill-2");

        // skill1 cannot handle, skill2 can handle
        when(skill1.canHandle("test request")).thenReturn(false);
        when(skill2.canHandle("test request")).thenReturn(true);

        skillManager.registerSkill(skill1);
        skillManager.registerSkill(skill2);

        AgentSkill found = skillManager.findSkillForRequest("test request");

        assertNotNull(found);
        assertEquals("skill-2", found.getName());
    }

    // ==================== Skill Execution Tests ====================

    @Test
    void executeSkillByName_returnsSuccessResultForValidSkill() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);
        when(mockResult.isSuccess()).thenReturn(true);

        skillManager.registerSkill(mockSkill);

        AgentSkillResult result = skillManager.executeSkill("test-skill", "request", Map.of());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(mockSkill).execute(eq("request"), anyMap());
    }

    @Test
    void executeSkillByName_returnsFailureResultForNonExistentSkill() {
        AgentSkillResult result = skillManager.executeSkill("non-existent", "request", Map.of());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Skill not found"));
        assertEquals("non-existent", result.getSkillName());
    }

    @Test
    void executeSkillByRequest_findsAndExecutesMatchingSkill() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.canHandle("test request")).thenReturn(true);
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);
        when(mockResult.isSuccess()).thenReturn(true);

        skillManager.registerSkill(mockSkill);

        AgentSkillResult result = skillManager.executeSkill("test request", Map.of());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(mockSkill).canHandle("test request");
        verify(mockSkill).execute(eq("test request"), anyMap());
    }

    @Test
    void executeSkillByRequest_returnsFailureWhenNoSkillFound() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.canHandle(anyString())).thenReturn(false);

        skillManager.registerSkill(mockSkill);

        AgentSkillResult result = skillManager.executeSkill("unhandled request", Map.of());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No skill found"));
    }

    @Test
    void executeSkill_handlesExceptionsGracefully() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenThrow(new RuntimeException("Test exception"));

        skillManager.registerSkill(mockSkill);

        AgentSkillResult result = skillManager.executeSkill("test-skill", "request", Map.of());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Skill execution failed"));
        assertEquals("test-skill", result.getSkillName());
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("error"));
    }

    @Test
    void executeSkillByRequest_handlesExceptionsGracefully() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.canHandle(anyString())).thenReturn(true);
        when(mockSkill.execute(anyString(), anyMap())).thenThrow(new RuntimeException("Test exception"));

        skillManager.registerSkill(mockSkill);

        AgentSkillResult result = skillManager.executeSkill("test request", Map.of());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Skill execution failed"));
    }

    // ==================== Listener Tests ====================

    @Test
    void addListener_registersListener() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(mockListener);

        skillManager.executeSkill("test-skill", "request", Map.of());

        verify(mockListener).onExecutionStarted(eq(mockSkill), eq("request"), anyMap());
    }

    @Test
    void removeListener_unregistersListener() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(mockListener);
        skillManager.removeListener(mockListener);

        skillManager.executeSkill("test-skill", "request", Map.of());

        verifyNoInteractions(mockListener);
    }

    @Test
    void onExecutionStarted_calledBeforeExecution() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(mockListener);

        skillManager.executeSkill("test-skill", "request", Map.of("param", "value"));

        verify(mockListener).onExecutionStarted(mockSkill, "request", Map.of("param", "value"));
    }

    @Test
    void onExecutionCompleted_calledAfterSuccessfulExecution() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(mockListener);

        skillManager.executeSkill("test-skill", "request", Map.of());

        verify(mockListener).onExecutionCompleted(eq(mockSkill), eq("request"), anyMap(), eq(mockResult));
    }

    @Test
    void onExecutionFailed_calledWhenExecutionThrowsException() {
        when(mockSkill.getName()).thenReturn("test-skill");
        RuntimeException exception = new RuntimeException("Test exception");
        when(mockSkill.execute(anyString(), anyMap())).thenThrow(exception);

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(mockListener);

        skillManager.executeSkill("test-skill", "request", Map.of());

        verify(mockListener).onExecutionFailed(eq(mockSkill), eq("request"), anyMap(), eq(exception));
    }

    @Test
    void listenerErrorsDoNotPropagate() {
        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);

        doThrow(new RuntimeException("Listener error"))
            .when(mockListener)
            .onExecutionStarted(any(), anyString(), anyMap());

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(mockListener);

        // Should not throw exception
        AgentSkillResult result = skillManager.executeSkill("test-skill", "request", Map.of());

        assertNotNull(result);
        // Execution should still complete
        verify(mockSkill).execute(anyString(), anyMap());
    }

    @Test
    void multipleListeners_allCalled() {
        AgentSkillManager.SkillExecutionListener listener1 = mock(AgentSkillManager.SkillExecutionListener.class);
        AgentSkillManager.SkillExecutionListener listener2 = mock(AgentSkillManager.SkillExecutionListener.class);

        when(mockSkill.getName()).thenReturn("test-skill");
        when(mockSkill.execute(anyString(), anyMap())).thenReturn(mockResult);

        skillManager.registerSkill(mockSkill);
        skillManager.addListener(listener1);
        skillManager.addListener(listener2);

        skillManager.executeSkill("test-skill", "request", Map.of());

        verify(listener1).onExecutionStarted(eq(mockSkill), eq("request"), anyMap());
        verify(listener2).onExecutionStarted(eq(mockSkill), eq("request"), anyMap());
        verify(listener1).onExecutionCompleted(eq(mockSkill), eq("request"), anyMap(), eq(mockResult));
        verify(listener2).onExecutionCompleted(eq(mockSkill), eq("request"), anyMap(), eq(mockResult));
    }
}
