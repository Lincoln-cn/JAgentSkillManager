package org.unreal.agent.skill.loader;

import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;

import java.nio.file.Path;

/**
 * Interface for skill loaders that can load skills from different sources.
 */
public interface SkillLoader {
    
    /**
     * Loads a skill from the given source.
     *
     * @param source the source to load the skill from
     * @return the loaded skill or null if loading failed
     */
    FolderBasedSkillLoader.LoadedSkill loadSkill(Object source);
    
    /**
     * Checks if this loader supports the given source.
     *
     * @param source the source to check
     * @return true if this loader supports the source
     */
    boolean supports(Object source);
    
    /**
     * Gets the priority of this loader (higher priority loaders are tried first).
     *
     * @return the priority
     */
    default int getPriority() {
        return 0;
    }
}