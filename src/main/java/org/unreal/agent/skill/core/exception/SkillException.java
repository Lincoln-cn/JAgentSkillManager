package org.unreal.agent.skill.core.exception;

/**
 * Base exception for skill-related errors.
 */
public class SkillException extends Exception {
    
    public SkillException(String message) {
        super(message);
    }
    
    public SkillException(String message, Throwable cause) {
        super(message, cause);
    }
}