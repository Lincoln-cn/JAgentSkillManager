package org.unreal.agent.skill.loader.impl;

import org.springframework.stereotype.Component;
import org.unreal.agent.skill.loader.SkillLoader;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;

import java.nio.file.Path;
import java.nio.file.Files;

/**
 * Loads skills from folder-based sources.
 */
@Component
public class FolderSkillLoader implements SkillLoader {
    
    @Override
    public FolderBasedSkillLoader.LoadedSkill loadSkill(Object source) {
        if (!(source instanceof Path)) {
            return null;
        }
        
        Path skillPath = (Path) source;
        if (!Files.exists(skillPath) || !Files.isDirectory(skillPath)) {
            return null;
        }
        
        // Use the existing FolderBasedSkillLoader to load the skill
        FolderBasedSkillLoader loader = new FolderBasedSkillLoader();
        return loader.loadSkillFromFolder(skillPath);
    }
    
    @Override
    public boolean supports(Object source) {
        return source instanceof Path && Files.isDirectory((Path) source);
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority for folder-based loading
    }
}