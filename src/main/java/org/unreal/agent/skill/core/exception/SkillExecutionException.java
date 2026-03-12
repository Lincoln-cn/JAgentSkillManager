package org.unreal.agent.skill.core.exception;

/**
 * Exception thrown when skill execution fails.
 */
public class SkillExecutionException extends SkillException {
    
    public SkillExecutionException(String message) {
        super(message);
    }
    
    public SkillExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}