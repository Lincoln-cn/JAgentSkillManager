package org.unreal.agent.skill.core.exception;

/**
 * Exception thrown when skill validation fails.
 */
public class SkillValidationException extends SkillException {
    
    public SkillValidationException(String message) {
        super(message);
    }
    
    public SkillValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}