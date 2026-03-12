package org.unreal.agent.skill.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.loader.SkillLoader;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;

import java.nio.file.Path;

/**
 * Loads skills from Spring Bean sources.
 */
@Component
public class SpringBeanSkillLoader implements SkillLoader {
    
    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;
    
    @Override
    public FolderBasedSkillLoader.LoadedSkill loadSkill(Object source) {
        if (!(source instanceof String)) {
            return null;
        }
        
        String beanName = (String) source;
        try {
            AgentSkill skill = applicationContext.getBean(beanName, AgentSkill.class);
            if (skill != null) {
                // Create a mock LoadedSkill since Spring Beans don't have a physical folder
                // This is a simplified representation - in practice, you might need a more sophisticated approach
                return new MockLoadedSkill(skill);
            }
        } catch (Exception e) {
            // Bean not found or other error
        }
        
        return null;
    }
    
    @Override
    public boolean supports(Object source) {
        if (!(source instanceof String)) {
            return false;
        }
        
        String beanName = (String) source;
        return applicationContext.containsBean(beanName) && 
               applicationContext.isTypeMatch(beanName, AgentSkill.class);
    }
    
    @Override
    public int getPriority() {
        return 5; // Medium priority for Spring beans
    }
    
    /**
     * Mock implementation of LoadedSkill for Spring beans.
     */
    private static class MockLoadedSkill extends FolderBasedSkillLoader.LoadedSkill {
        public MockLoadedSkill(AgentSkill skill) {
            super(createMockDescriptor(skill), skill, null, null);
        }
        
        private static org.unreal.agent.skill.folder.SkillDescriptor createMockDescriptor(AgentSkill skill) {
            org.unreal.agent.skill.folder.SkillDescriptor descriptor = new org.unreal.agent.skill.folder.SkillDescriptor();
            descriptor.setName(skill.getName());
            descriptor.setDescription(skill.getDescription());
            descriptor.setVersion(skill.getVersion());
            return descriptor;
        }
    }
}