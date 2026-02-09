package org.unreal.agent.skill.folder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unreal.agent.skill.folder.model.SkillEntryPoint;
import org.unreal.agent.skill.folder.model.SkillParameters;
import org.unreal.agent.skill.folder.model.SkillResources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Metadata descriptor for folder-based skills.
 * This class defines the structure of skill.json or skill.yaml files.
 */
public class SkillDescriptor {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("author")
    private String author;
    
    @JsonProperty("tags")
    private List<String> tags;
    
    @JsonProperty("main")
    private String main;
    
    @JsonProperty("dependencies")
    private Map<String, String> dependencies;
    
    @JsonProperty("parameters")
    private SkillParameters parameters;
    
    @JsonProperty("entryPoints")
    private List<SkillEntryPoint> entryPoints;
    
    @JsonProperty("resources")
    private SkillResources resources;
    
    @JsonProperty("enabled")
    private boolean enabled = true;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("keywords")
    private List<String> keywords;
    
    @JsonProperty("allowed-tools")
    private String allowedTools;

    @JsonProperty("compatibility")
    private String compatibility;

    @JsonProperty("license")
    private String license;

    private String instructions;

    @JsonProperty("metadata")
    private Map<String, Object> extraMetadata;

    public SkillDescriptor() {}

    public SkillDescriptor(String name, String version, String description) {
        this.name = name;
        this.version = version;
        this.description = description;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getMain() { return main; }
    public void setMain(String main) { this.main = main; }

    public Map<String, String> getDependencies() { return dependencies; }
    public void setDependencies(Map<String, String> dependencies) { this.dependencies = dependencies; }

    public SkillParameters getParameters() { return parameters; }
    public void setParameters(SkillParameters parameters) { this.parameters = parameters; }

    public List<SkillEntryPoint> getEntryPoints() { return entryPoints; }
    public void setEntryPoints(List<SkillEntryPoint> entryPoints) { this.entryPoints = entryPoints; }

    public SkillResources getResources() { return resources; }
    public void setResources(SkillResources resources) { this.resources = resources; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getAllowedTools() { return allowedTools; }
    public void setAllowedTools(String allowedTools) { this.allowedTools = allowedTools; }

    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Map<String, Object> getExtraMetadata() { return extraMetadata; }
    public void setExtraMetadata(Map<String, Object> extraMetadata) { this.extraMetadata = extraMetadata; }

    // helper to safely add metadata
    public void addExtraMetadata(String key, Object value) {
        if (this.extraMetadata == null) {
            this.extraMetadata = new java.util.LinkedHashMap<>();
        }
        this.extraMetadata.put(key, value);
    }

    public static SkillDescriptor fromFile(Path descriptorFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(descriptorFile.toFile(), SkillDescriptor.class);
    }

    public void toFile(Path descriptorFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(descriptorFile.toFile(), this);
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               version != null && !version.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               (main != null || instructions != null);
    }

    public static String getSkillFolderName(Path descriptorFile) {
        Path parent = descriptorFile.getParent();
        return parent != null ? parent.getFileName().toString() : "";
    }
}
