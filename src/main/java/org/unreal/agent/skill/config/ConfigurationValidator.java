package org.unreal.agent.skill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

/**
 * Component that validates the AgentSkillProperties configuration.
 */
@Component
public class ConfigurationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);
    
    @Autowired
    private AgentSkillProperties properties;
    
    @Autowired
    private Validator validator;
    
    @PostConstruct
    public void validateConfiguration() {
        logger.info("Validating Agent Skill configuration...");
        
        Set<ConstraintViolation<AgentSkillProperties>> violations = validator.validate(properties);
        
        if (!violations.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Configuration validation failed:\n");
            for (ConstraintViolation<AgentSkillProperties> violation : violations) {
                errorMsg.append("  - ")
                        .append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append(" (was: ")
                        .append(violation.getInvalidValue())
                        .append(")\n");
            }
            
            logger.error(errorMsg.toString());
            throw new IllegalStateException(errorMsg.toString());
        }
        
        logger.info("Configuration validation passed.");
    }
}