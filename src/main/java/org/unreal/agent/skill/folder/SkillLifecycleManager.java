package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;

import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing the lifecycle of folder-based skills.
 * Provides dynamic loading, unloading, and hot-reloading capabilities.
 */
@Component
public class SkillLifecycleManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SkillLifecycleManager.class);
    
    @Autowired
    private FolderBasedSkillLoader skillLoader;
    
    @Autowired
    private AgentSkillManager skillManager;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, WatchKey> watchKeys = new ConcurrentHashMap<>();
    private WatchService watchService;
    private Path skillsDirectory;
    private boolean hotReloadEnabled = true;
    
    /**
     * Initialize the lifecycle manager with a skills directory.
     * 
     * @param skillsDirectory the directory to watch for skills
     */
    public void initialize(Path skillsDirectory) {
        this.skillsDirectory = skillsDirectory;
        
        try {
            // Initialize watch service
            watchService = FileSystems.getDefault().newWatchService();
            
            // Register directory for changes
            registerWatchDirectory(skillsDirectory);
            
            // Start the file watcher thread
            startFileWatcher();
            
            // Load existing skills
            loadExistingSkills();
            
            logger.info("Skill lifecycle manager initialized with directory: {}", skillsDirectory);
            
        } catch (Exception e) {
            logger.error("Failed to initialize skill lifecycle manager", e);
        }
    }
    
    /**
     * Load all existing skills from the directory.
     */
    private void loadExistingSkills() {
        logger.info("Loading existing skills from directory: {}", skillsDirectory);
        
        Map<String, FolderBasedSkillLoader.LoadedSkill> loadedSkills = 
            skillLoader.loadSkillsFromDirectory(skillsDirectory);
        
        for (FolderBasedSkillLoader.LoadedSkill loadedSkill : loadedSkills.values()) {
            AgentSkill skillInstance = loadedSkill.getSkillInstance();
            skillManager.registerSkill(skillInstance);
            
            // Watch individual skill folders for changes
            registerWatchDirectory(loadedSkill.getSkillFolder());
        }
        
        logger.info("Loaded {} skills from directory", loadedSkills.size());
    }
    
    /**
     * Register a directory for file watching.
     * 
     * @param directory the directory to watch
     */
    private void registerWatchDirectory(Path directory) {
        try {
            if (!Files.exists(directory)) {
                logger.warn("Directory does not exist for watching: {}", directory);
                return;
            }
            
            WatchKey key = directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            
            watchKeys.put(directory.toString(), key);
            logger.debug("Registered watch for directory: {}", directory);
            
        } catch (Exception e) {
            logger.error("Failed to register watch for directory: {}", directory, e);
        }
    }
    
    /**
     * Start the file watcher thread.
     */
    private void startFileWatcher() {
        scheduler.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WatchKey key = watchService.take();
                    handleWatchEvents(key);
                    key.reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in file watcher", e);
                }
            }
        });
    }
    
    /**
     * Handle watch events for file changes.
     * 
     * @param key the watch key
     */
    private void handleWatchEvents(WatchKey key) {
        Path watchedDir = (Path) key.watchable();
        
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }
            
            Path eventPath = (Path) event.context();
            Path fullPath = watchedDir.resolve(eventPath);
            
            logger.debug("File event: {} on {}", kind, fullPath);
            
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                handleFileCreated(fullPath);
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                handleFileDeleted(fullPath);
            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                handleFileModified(fullPath);
            }
        }
    }
    
    /**
     * Handle file creation events.
     * 
     * @param filePath the created file path
     */
    private void handleFileCreated(Path filePath) {
        if (!hotReloadEnabled) {
            return;
        }
        
        // Debounce rapid file changes
        scheduler.schedule(() -> {
            try {
                if (Files.isDirectory(filePath)) {
                    // New skill folder created
                    handleNewSkillFolder(filePath);
                } else if (isSkillDescriptor(filePath)) {
                    // Skill descriptor created/updated
                    handleSkillDescriptorChange(filePath.getParent());
                }
            } catch (Exception e) {
                logger.error("Error handling file creation: {}", filePath, e);
            }
        }, 500, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Handle file deletion events.
     * 
     * @param filePath the deleted file path
     */
    private void handleFileDeleted(Path filePath) {
        if (!hotReloadEnabled) {
            return;
        }
        
        scheduler.schedule(() -> {
            try {
                if (isSkillDescriptor(filePath)) {
                    // Skill descriptor deleted, unload skill
                    String skillName = inferSkillNameFromPath(filePath.getParent());
                    unloadSkill(skillName);
                }
            } catch (Exception e) {
                logger.error("Error handling file deletion: {}", filePath, e);
            }
        }, 500, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Handle file modification events.
     * 
     * @param filePath the modified file path
     */
    private void handleFileModified(Path filePath) {
        if (!hotReloadEnabled) {
            return;
        }
        
        scheduler.schedule(() -> {
            try {
                if (isSkillDescriptor(filePath)) {
                    // Skill descriptor modified, reload skill
                    handleSkillDescriptorChange(filePath.getParent());
                }
            } catch (Exception e) {
                logger.error("Error handling file modification: {}", filePath, e);
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Handle new skill folder creation.
     * 
     * @param skillFolder the new skill folder
     */
    private void handleNewSkillFolder(Path skillFolder) {
        logger.info("New skill folder detected: {}", skillFolder);
        
        scheduler.schedule(() -> {
            try {
                FolderBasedSkillLoader.LoadedSkill loadedSkill = 
                    skillLoader.loadSkillFromFolder(skillFolder);
                
                if (loadedSkill != null) {
                    AgentSkill skillInstance = loadedSkill.getSkillInstance();
                    skillManager.registerSkill(skillInstance);
                    
                    // Watch the new skill folder
                    registerWatchDirectory(skillFolder);
                    
                    logger.info("Successfully loaded new skill: {}", loadedSkill.getDescriptor().getName());
                }
            } catch (Exception e) {
                logger.error("Failed to load new skill from folder: {}", skillFolder, e);
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Handle skill descriptor changes.
     * 
     * @param skillFolder the skill folder containing the descriptor
     */
    private void handleSkillDescriptorChange(Path skillFolder) {
        String skillName = inferSkillNameFromPath(skillFolder);
        logger.info("Skill descriptor changed for: {}", skillName);
        
        scheduler.schedule(() -> {
            try {
                // Unload existing skill
                unloadSkill(skillName);
                
                // Reload skill
                FolderBasedSkillLoader.LoadedSkill loadedSkill = 
                    skillLoader.loadSkillFromFolder(skillFolder);
                
                if (loadedSkill != null) {
                    AgentSkill skillInstance = loadedSkill.getSkillInstance();
                    skillManager.registerSkill(skillInstance);
                    
                    logger.info("Successfully reloaded skill: {}", skillName);
                }
            } catch (Exception e) {
                logger.error("Failed to reload skill from folder: {}", skillFolder, e);
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Unload a skill by name.
     * 
     * @param skillName the skill name
     */
    private void unloadSkill(String skillName) {
        try {
            FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
            if (loadedSkill != null) {
                skillManager.unregisterSkill(skillName);
                skillLoader.unloadSkill(skillName);
                logger.info("Successfully unloaded skill: {}", skillName);
            }
        } catch (Exception e) {
            logger.error("Failed to unload skill: {}", skillName, e);
        }
    }
    
    /**
     * Check if a file is a skill descriptor.
     * 
     * @param filePath the file path
     * @return true if it's a skill descriptor
     */
    private boolean isSkillDescriptor(Path filePath) {
        if (filePath == null || !Files.isRegularFile(filePath)) {
            return false;
        }
        
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.equals("skill.json") || fileName.equals("skill.yaml") || fileName.equals("skill.yml");
    }
    
    /**
     * Infer skill name from folder path.
     * 
     * @param skillFolder the skill folder path
     * @return inferred skill name
     */
    private String inferSkillNameFromPath(Path skillFolder) {
        if (skillFolder == null) {
            return "";
        }
        
        Path fileName = skillFolder.getFileName();
        return fileName != null ? fileName.toString() : "";
    }
    
    /**
     * Enable or disable hot reload.
     * 
     * @param enabled whether hot reload should be enabled
     */
    public void setHotReloadEnabled(boolean enabled) {
        this.hotReloadEnabled = enabled;
        logger.info("Hot reload {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Check if hot reload is enabled.
     * 
     * @return true if hot reload is enabled
     */
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }
    
    /**
     * Reload all skills.
     */
    public void reloadAllSkills() {
        logger.info("Reloading all skills");
        
        // Unload all existing skills
        Map<String, FolderBasedSkillLoader.LoadedSkill> loadedSkills = skillLoader.getLoadedSkills();
        for (String skillName : loadedSkills.keySet()) {
            unloadSkill(skillName);
        }
        
        // Load all skills again
        loadExistingSkills();
    }
    
    /**
     * Shutdown the lifecycle manager.
     */
    public void shutdown() {
        try {
            logger.info("Shutting down skill lifecycle manager");
            
            // Cancel watch keys
            for (WatchKey key : watchKeys.values()) {
                key.cancel();
            }
            watchKeys.clear();
            
            // Close watch service
            if (watchService != null) {
                watchService.close();
            }
            
            // Shutdown scheduler
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }
    }
}