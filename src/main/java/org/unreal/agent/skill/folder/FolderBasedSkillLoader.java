package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Service for loading and managing folder-based skills.
 * This service can load skills from various folder structures and formats.
 */
@Component
public class FolderBasedSkillLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(FolderBasedSkillLoader.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, LoadedSkill> loadedSkills = new ConcurrentHashMap<>();
    private final Map<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();
    
    /**
     * Load skills from a directory.
     * 
     * @param skillsDirectory the directory containing skill folders
     * @return map of skill name to LoadedSkill
     */
    public Map<String, LoadedSkill> loadSkillsFromDirectory(Path skillsDirectory) {
        Map<String, LoadedSkill> skills = new HashMap<>();
        
        if (!Files.exists(skillsDirectory) || !Files.isDirectory(skillsDirectory)) {
            logger.warn("Skills directory does not exist: {}", skillsDirectory);
            return skills;
        }
        
        try {
            Files.list(skillsDirectory)
                .filter(Files::isDirectory)
                .forEach(skillFolder -> {
                    try {
                        LoadedSkill skill = loadSkillFromFolder(skillFolder);
                        if (skill != null) {
                            skills.put(skill.getDescriptor().getName(), skill);
                            loadedSkills.put(skill.getDescriptor().getName(), skill);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to load skill from folder: {}", skillFolder, e);
                    }
                });
        } catch (IOException e) {
            logger.error("Failed to list skill directories", e);
        }
        
        return skills;
    }
    
    /**
     * Load a single skill from a folder.
     * 
     * @param skillFolder the skill folder path
     * @return LoadedSkill instance or null if loading failed
     */
    public LoadedSkill loadSkillFromFolder(Path skillFolder) {
        try {
            // Look for SKILL.md (agentskills.io spec)
            Path skillMd = skillFolder.resolve("SKILL.md");
            
            SkillDescriptor descriptor = null;
            Path descriptorFile = null;
            
            if (Files.exists(skillMd)) {
                descriptor = SkillMarkdownParser.parse(skillMd);
                descriptorFile = skillMd;
            } else {
                // Look for other skill descriptor files
                Path jsonDescriptor = skillFolder.resolve("skill.json");
                Path yamlDescriptor = skillFolder.resolve("skill.yaml");
                Path ymlDescriptor = skillFolder.resolve("skill.yml");
                
                if (Files.exists(jsonDescriptor)) {
                    descriptor = SkillDescriptor.fromFile(jsonDescriptor);
                    descriptorFile = jsonDescriptor;
                } else if (Files.exists(yamlDescriptor)) {
                    descriptor = loadYamlDescriptor(yamlDescriptor);
                    descriptorFile = yamlDescriptor;
                } else if (Files.exists(ymlDescriptor)) {
                    descriptor = loadYamlDescriptor(ymlDescriptor);
                    descriptorFile = ymlDescriptor;
                }
            }
            
            if (descriptor == null) {
                logger.warn("No skill descriptor found in folder: {}", skillFolder);
                return null;
            }
            
            if (!descriptor.isValid()) {
                logger.warn("Invalid skill descriptor in folder: {}", skillFolder);
                return null;
            }
            
            if (!descriptor.isEnabled()) {
                logger.info("Skill is disabled: {}", descriptor.getName());
                return null;
            }
            
            // Load the skill implementation
            AgentSkill skillInstance;
            if (descriptor.getInstructions() != null && descriptor.getMain() == null) {
                // Instruction-only skill from SKILL.md
                skillInstance = new MarkdownAgentSkill(descriptor);
            } else {
                skillInstance = loadSkillInstance(skillFolder, descriptor);
            }
            
            if (skillInstance == null) {
                logger.warn("Failed to load skill instance for: {}", descriptor.getName());
                return null;
            }
            
            LoadedSkill loadedSkill = new LoadedSkill(descriptor, skillInstance, skillFolder, descriptorFile);
            logger.info("Successfully loaded skill: {} from {}", descriptor.getName(), skillFolder);
            
            return loadedSkill;
            
        } catch (Exception e) {
            logger.error("Failed to load skill from folder: {}", skillFolder, e);
            return null;
        }
    }
    
    /**
     * Load skill instance from descriptor and folder.
     * 
     * @param skillFolder the skill folder
     * @param descriptor the skill descriptor
     * @return AgentSkill instance or null
     */
    private AgentSkill loadSkillInstance(Path skillFolder, SkillDescriptor descriptor) {
        try {
            String mainClass = descriptor.getMain();
            
            // Try to load as Spring bean first
            try {
                Object bean = applicationContext.getBean(mainClass);
                if (bean instanceof AgentSkill) {
                    return (AgentSkill) bean;
                }
            } catch (Exception e) {
                // Not a Spring bean, try other loading methods
            }
            
            // Try to load from JAR file
            Path jarFile = skillFolder.resolve(descriptor.getName() + ".jar");
            if (Files.exists(jarFile)) {
                return loadSkillFromJar(jarFile, mainClass);
            }
            
            // Try to load from compiled class files
            Path classesDir = skillFolder.resolve("classes");
            if (Files.exists(classesDir)) {
                return loadSkillFromClassesDirectory(classesDir, mainClass);
            }
            
            // Try to load from script files
            return loadSkillFromScript(skillFolder, descriptor);
            
        } catch (Exception e) {
            logger.error("Failed to load skill instance: {}", descriptor.getName(), e);
            return null;
        }
    }
    
    /**
     * Load skill from JAR file.
     */
    private AgentSkill loadSkillFromJar(Path jarFile, String mainClass) throws Exception {
        URL jarUrl = jarFile.toUri().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
        
        Class<?> skillClass = classLoader.loadClass(mainClass);
        Object instance = skillClass.getDeclaredConstructor().newInstance();
        
        if (instance instanceof AgentSkill) {
            classLoaders.put(mainClass, classLoader);
            return (AgentSkill) instance;
        }
        
        return null;
    }
    
    /**
     * Load skill from classes directory.
     */
    private AgentSkill loadSkillFromClassesDirectory(Path classesDir, String mainClass) throws Exception {
        URL classesUrl = classesDir.toUri().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{classesUrl}, getClass().getClassLoader());
        
        Class<?> skillClass = classLoader.loadClass(mainClass);
        Object instance = skillClass.getDeclaredConstructor().newInstance();
        
        if (instance instanceof AgentSkill) {
            classLoaders.put(mainClass, classLoader);
            return (AgentSkill) instance;
        }
        
        return null;
    }
    
    /**
     * Load skill from script files.
     */
    private AgentSkill loadSkillFromScript(Path skillFolder, SkillDescriptor descriptor) {
        // This is a placeholder for script-based skill loading
        // In a real implementation, you might support JavaScript, Python, etc.
        logger.info("Script-based skill loading not yet implemented for: {}", descriptor.getName());
        return null;
    }
    
    /**
     * Load YAML descriptor.
     */
    private SkillDescriptor loadYamlDescriptor(Path yamlFile) {
        try {
            // For now, we'll create a basic descriptor
            // In a real implementation, you'd use a proper YAML parser
            Properties props = YamlPropertiesReader.readYamlAsProperties(yamlFile);
            
            SkillDescriptor descriptor = new SkillDescriptor();
            descriptor.setName(props.getProperty("name"));
            descriptor.setVersion(props.getProperty("version", "1.0.0"));
            descriptor.setDescription(props.getProperty("description", ""));
            descriptor.setAuthor(props.getProperty("author"));
            descriptor.setMain(props.getProperty("main"));
            descriptor.setEnabled(Boolean.parseBoolean(props.getProperty("enabled", "true")));
            
            return descriptor;
        } catch (Exception e) {
            logger.error("Failed to load YAML descriptor: {}", yamlFile, e);
            return null;
        }
    }
    
    /**
     * Unload a skill.
     * 
     * @param skillName the skill name to unload
     * @return true if successfully unloaded
     */
    public boolean unloadSkill(String skillName) {
        LoadedSkill skill = loadedSkills.remove(skillName);
        if (skill != null) {
            try {
                URLClassLoader classLoader = classLoaders.remove(skill.getDescriptor().getMain());
                if (classLoader != null) {
                    classLoader.close();
                }
                logger.info("Successfully unloaded skill: {}", skillName);
                return true;
            } catch (Exception e) {
                logger.error("Failed to unload skill: {}", skillName, e);
            }
        }
        return false;
    }
    
    /**
     * Reload a skill.
     * 
     * @param skillName the skill name to reload
     * @return reloaded LoadedSkill or null
     */
    public LoadedSkill reloadSkill(String skillName) {
        LoadedSkill oldSkill = loadedSkills.get(skillName);
        if (oldSkill != null) {
            Path skillFolder = oldSkill.getSkillFolder();
            unloadSkill(skillName);
            return loadSkillFromFolder(skillFolder);
        }
        return null;
    }
    
    /**
     * Get all loaded skills.
     * 
     * @return map of skill name to LoadedSkill
     */
    public Map<String, LoadedSkill> getLoadedSkills() {
        return new HashMap<>(loadedSkills);
    }
    
    /**
     * Get loaded skill by name.
     * 
     * @param skillName the skill name
     * @return LoadedSkill or null
     */
    public LoadedSkill getLoadedSkill(String skillName) {
        return loadedSkills.get(skillName);
    }
    
    /**
     * Represents a loaded folder-based skill.
     */
    public static class LoadedSkill {
        private final SkillDescriptor descriptor;
        private final AgentSkill skillInstance;
        private final Path skillFolder;
        private final Path descriptorFile;
        private final long loadTime;
        
        public LoadedSkill(SkillDescriptor descriptor, AgentSkill skillInstance, 
                          Path skillFolder, Path descriptorFile) {
            this.descriptor = descriptor;
            this.skillInstance = skillInstance;
            this.skillFolder = skillFolder;
            this.descriptorFile = descriptorFile;
            this.loadTime = System.currentTimeMillis();
        }
        
        public SkillDescriptor getDescriptor() {
            return descriptor;
        }
        
        public AgentSkill getSkillInstance() {
            return skillInstance;
        }
        
        public Path getSkillFolder() {
            return skillFolder;
        }
        
        public Path getDescriptorFile() {
            return descriptorFile;
        }
        
        public long getLoadTime() {
            return loadTime;
        }
        
        public AgentSkillResult execute(String request, Map<String, Object> parameters) {
            return skillInstance.execute(request, parameters);
        }
    }
}