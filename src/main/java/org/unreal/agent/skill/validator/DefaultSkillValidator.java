package org.unreal.agent.skill.validator;

import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;

/**
 * Default skill validator that performs basic validation checks.
 */
@Component
public class DefaultSkillValidator implements SkillValidator {
    
    @Override
    public boolean supports(AgentSkill skill) {
        // This validator supports all skills
        return true;
    }
    
    @Override
    public ValidationResult validate(AgentSkill skill) {
        if (skill == null) {
            return new ValidationResult(false, "Skill cannot be null");
        }
        
        if (skill.getName() == null || skill.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Skill name cannot be null or empty");
        }
        
        if (skill.getDescription() == null || skill.getDescription().trim().isEmpty()) {
            return new ValidationResult(false, "Skill description cannot be null or empty");
        }
        
        // Check for valid name format (alphanumeric, hyphens, underscores)
        if (!skill.getName().matches("^[a-zA-Z0-9_-]+$")) {
            return new ValidationResult(false, "Skill name contains invalid characters. Only alphanumeric, hyphens, and underscores are allowed.");
        }
        
        // Check name length
        if (skill.getName().length() > 64) {
            return new ValidationResult(false, "Skill name exceeds maximum length of 64 characters");
        }
        
        return new ValidationResult(true, null);
    }
}