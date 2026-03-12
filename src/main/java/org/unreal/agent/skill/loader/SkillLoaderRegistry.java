package org.unreal.agent.skill.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.exception.SkillException;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Registry for skill loaders that manages multiple loaders and delegates loading to the appropriate one.
 */
@Component
public class SkillLoaderRegistry {
    
    @Autowired(required = false)
    private List<SkillLoader> skillLoaders;
    
    /**
     * Loads a skill using the appropriate loader.
     *
     * @param source the source to load the skill from
     * @return the loaded skill
     * @throws SkillException if no loader supports the source or loading fails
     */
    public FolderBasedSkillLoader.LoadedSkill loadSkill(Object source) throws SkillException {
        if (skillLoaders == null || skillLoaders.isEmpty()) {
            throw new SkillException("No skill loaders available");
        }
        
        // Sort loaders by priority (highest first)
        return skillLoaders.stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .filter(loader -> loader.supports(source))
                .findFirst()
                .map(loader -> loader.loadSkill(source))
                .orElseThrow(() -> new SkillException("No skill loader supports the given source: " + source));
    }
    
    /**
     * Checks if any loader supports the given source.
     *
     * @param source the source to check
     * @return true if any loader supports the source
     */
    public boolean supports(Object source) {
        if (skillLoaders == null) {
            return false;
        }
        
        return skillLoaders.stream()
                .anyMatch(loader -> loader.supports(source));
    }
    
    /**
     * Gets all registered skill loaders.
     *
     * @return list of skill loaders
     */
    public List<SkillLoader> getSkillLoaders() {
        return skillLoaders != null ? skillLoaders : List.of();
    }
}