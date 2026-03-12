package org.unreal.agent.skill.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Skill manager specifically for Spring Bean skills.
 */
@Component
public class SpringBeanSkillManager implements SkillManager {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void registerSkill(AgentSkill skill) {
        // Spring Bean skills are typically registered automatically
        // This method might not be commonly used for Spring Bean skills
        throw new UnsupportedOperationException("Spring Bean skills are registered automatically");
    }
    
    @Override
    public void unregisterSkill(String skillName) {
        // Spring Bean skills are managed by Spring context
        // This method might not be commonly used for Spring Bean skills
        throw new UnsupportedOperationException("Spring Bean skills are managed by Spring context");
    }
    
    @Override
    public Collection<AgentSkill> getAllSkills() {
        Map<String, AgentSkill> beans = applicationContext.getBeansOfType(AgentSkill.class);
        return beans.values();
    }
    
    @Override
    public AgentSkill getSkill(String name) {
        try {
            return applicationContext.getBean(name, AgentSkill.class);
        } catch (Exception e) {
            return null; // Bean not found
        }
    }
    
    @Override
    public AgentSkill findSkillForRequest(String request) {
        Map<String, AgentSkill> beans = applicationContext.getBeansOfType(AgentSkill.class);
        return beans.values().stream()
                .filter(skill -> skill.canHandle(request))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public AgentSkillResult executeSkill(String skillName, String request, Map<String, Object> parameters) {
        AgentSkill skill = getSkill(skillName);
        if (skill == null) {
            return AgentSkillResult.failure()
                    .message("Spring Bean skill not found: " + skillName)
                    .skillName(skillName)
                    .build();
        }
        
        return executeSkill(skill, request, parameters);
    }
    
    @Override
    public AgentSkillResult executeSkill(String request, Map<String, Object> parameters) {
        AgentSkill skill = findSkillForRequest(request);
        if (skill == null) {
            return AgentSkillResult.failure()
                    .message("No Spring Bean skill found to handle request: " + request)
                    .build();
        }
        
        return executeSkill(skill, request, parameters);
    }
    
    /**
     * Execute a specific skill instance.
     *
     * @param skill the skill to execute
     * @param request the request to process
     * @param parameters the parameters for skill execution
     * @return the result of skill execution
     */
    private AgentSkillResult executeSkill(AgentSkill skill, String request, Map<String, Object> parameters) {
        try {
            return skill.execute(request, parameters);
        } catch (Exception e) {
            return AgentSkillResult.failure()
                    .message("Spring Bean skill execution failed: " + e.getMessage())
                    .skillName(skill.getName())
                    .build();
        }
    }
}