package org.unreal.agent.skill.validator;

import org.unreal.agent.skill.core.AgentSkill;

/**
 * Interface for validating agent skills.
 */
public interface SkillValidator {
    
    /**
     * Validates a skill.
     *
     * @param skill the skill to validate
     * @return validation result
     */
    ValidationResult validate(AgentSkill skill);
    
    /**
     * Checks if this validator supports the given skill.
     *
     * @param skill the skill to check
     * @return true if this validator supports the skill
     */
    boolean supports(AgentSkill skill);
    
    /**
     * Validation result container.
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}