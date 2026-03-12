package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.folder.model.SkillEntryPoint;
import org.unreal.agent.skill.folder.model.SkillParameters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for migrating skills between different formats and platforms.
 * Supports migration from Spring beans to folder-based skills and vice versa.
 */
@Component
public class SkillMigrationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SkillMigrationUtils.class);
    
    /**
     * Migrate a Spring bean skill to folder-based structure.
     * 
     * @param skill the skill to migrate
     * @param targetDirectory target directory for the migrated skill
     * @return path to the created skill folder
     */
    public Path migrateSkillToFolder(AgentSkill skill, Path targetDirectory) {
        try {
            String skillName = skill.getName();
            Path skillFolder = targetDirectory.resolve(skillName);
            
            // Create skill directory
            Files.createDirectories(skillFolder);
            
            // Generate skill descriptor
            SkillDescriptor descriptor = generateDescriptorFromSkill(skill);
            Path descriptorFile = skillFolder.resolve("skill.json");
            descriptor.toFile(descriptorFile);
            
            // Generate skill implementation template
            generateSkillImplementation(skill, skillFolder);
            
            // Create additional folders and files
            createSkillStructure(skillFolder);
            
            logger.info("Successfully migrated skill '{}' to folder: {}", skillName, skillFolder);
            return skillFolder;
            
        } catch (Exception e) {
            logger.error("Failed to migrate skill to folder", e);
            return null;
        }
    }
    
    /**
     * Generate skill descriptor from an AgentSkill instance.
     * 
     * @param skill the skill instance
     * @return generated SkillDescriptor
     */
    private SkillDescriptor generateDescriptorFromSkill(AgentSkill skill) {
        SkillDescriptor descriptor = new SkillDescriptor();
        
        descriptor.setName(skill.getName());
        descriptor.setVersion(skill.getVersion());
        descriptor.setDescription(skill.getDescription());
        descriptor.setAuthor("Migrated from Spring Bean");
        descriptor.setMain(skill.getClass().getName());
        descriptor.setEnabled(true);
        descriptor.setCategory("migrated");
        
        // Generate parameters
        SkillParameters parameters = new SkillParameters();
        parameters.setRequired(skill.getRequiredParameters());
        parameters.setOptional(skill.getOptionalParameters());
        descriptor.setParameters(parameters);

        // Generate keywords from description
        List<String> keywords = extractKeywords(skill.getDescription());
        descriptor.setKeywords(keywords);

        // Generate default entry point
        SkillEntryPoint entryPoint = new SkillEntryPoint();
        entryPoint.setName("main");
        entryPoint.setDescription("Main entry point");
        entryPoint.setPath(skill.getClass().getSimpleName() + ".java");
        entryPoint.setMethod("execute");
        entryPoint.setKeywords(keywords);
        descriptor.setEntryPoints(Collections.singletonList(entryPoint));
        
        return descriptor;
    }
    
    /**
     * Generate skill implementation template.
     * 
     * @param skill the skill to template
     * @param skillFolder target folder
     */
    private void generateSkillImplementation(AgentSkill skill, Path skillFolder) {
        String className = skill.getClass().getSimpleName();
        String packageName = skill.getClass().getPackage().getName();
        String template = generateSkillTemplate(skill, className, packageName);
        
        Path implFile = skillFolder.resolve(className + ".java");
        try {
            Files.write(implFile, template.getBytes());
        } catch (IOException e) {
            logger.error("Failed to write skill implementation template", e);
        }
    }
    
    /**
     * Generate Java template for skill implementation.
     */
    private String generateSkillTemplate(AgentSkill skill, String className, String packageName) {
        StringBuilder template = new StringBuilder();
        
        template.append("package ").append(packageName).append(";\n\n");
        template.append("import org.springframework.stereotype.Component;\n");
        template.append("import org.unreal.agent.skill.AgentSkill;\n");
        template.append("import org.unreal.agent.skill.AgentSkillResult;\n");
        template.append("import java.util.Map;\n\n");
        
        template.append("/**\n");
        template.append(" * Auto-generated skill implementation.\n");
        template.append(" * Migrated from: ").append(skill.getClass().getName()).append("\n");
        template.append(" * Original version: ").append(skill.getVersion()).append("\n");
        template.append(" */\n");
        template.append("@Component\n");
        template.append("public class ").append(className).append(" implements AgentSkill {\n\n");
        
        // getName method
        template.append("    @Override\n");
        template.append("    public String getName() {\n");
        template.append("        return \"").append(skill.getName()).append("\";\n");
        template.append("    }\n\n");
        
        // getDescription method
        template.append("    @Override\n");
        template.append("    public String getDescription() {\n");
        template.append("        return \"").append(escapeJavaString(skill.getDescription())).append("\";\n");
        template.append("    }\n\n");
        
        // getVersion method
        template.append("    @Override\n");
        template.append("    public String getVersion() {\n");
        template.append("        return \"").append(skill.getVersion()).append("\";\n");
        template.append("    }\n\n");
        
        // canHandle method
        template.append("    @Override\n");
        template.append("    public boolean canHandle(String request) {\n");
        template.append("        // Determine if this skill can handle the given request\n");
        template.append("        // This is a basic implementation - customize based on your needs\n");
        template.append("        String lowerDesc = getDescription().toLowerCase();\n");
        template.append("        String lowerRequest = request.toLowerCase();\n");
        template.append("        \n");
        template.append("        // Check if request contains keywords from description\n");
        template.append("        if (lowerRequest.contains(lowerDesc.substring(0, Math.min(lowerDesc.length(), 20)).toLowerCase())) {\n");
        template.append("            return true;\n");
        template.append("        }\n");
        template.append("        \n");
        template.append("        // Add more sophisticated matching logic as needed\n");
        template.append("        return false;\n");
        template.append("    }\n\n");

        // execute method
        template.append("    @Override\n");
        template.append("    public AgentSkillResult execute(String request, Map<String, Object> parameters) {\n");
        template.append("        try {\n");
        template.append("            // TODO: Implement the actual skill logic here\n");
        template.append("            // Access required parameters with: parameters.get(\"paramName\")\n");
        template.append("            // Perform the skill's intended function\n");
        template.append("            \n");
        template.append("            // Example implementation - replace with actual logic\n");
        template.append("            Object result = performSkillLogic(request, parameters);\n");
        template.append("            \n");
        template.append("            return AgentSkillResult.success()\n");
        template.append("                    .message(\"Skill executed successfully\")\n");
        template.append("                    .data(result)\n");
        template.append("                    .skillName(getName())\n");
        template.append("                    .build();\n");
        template.append("        } catch (Exception e) {\n");
        template.append("            return AgentSkillResult.failure()\n");
        template.append("                    .message(\"Skill execution failed: \" + e.getMessage())\n");
        template.append("                    .skillName(getName())\n");
        template.append("                    .build();\n");
        template.append("        }\n");
        template.append("    }\n\n");

        // performSkillLogic helper method
        template.append("    /**\n");
        template.append("     * Performs the actual skill logic.\n");
        template.append("     * This is a placeholder method - implement the actual functionality here.\n");
        template.append("     *\n");
        template.append("     * @param request the input request\n");
        template.append("     * @param parameters the skill parameters\n");
        template.append("     * @return the result of the skill execution\n");
        template.append("     */\n");
        template.append("    private Object performSkillLogic(String request, Map<String, Object> parameters) {\n");
        template.append("        // TODO: Implement the actual skill functionality\n");
        template.append("        // This is where you would put the business logic for your skill\n");
        template.append("        // For example, calling external APIs, processing data, etc.\n");
        template.append("        \n");
        template.append("        // Placeholder implementation\n");
        template.append("        return \"Skill result for request: \" + request;\n");
        template.append("    }\n\n");

        // getRequiredParameters method
        Map<String, String> requiredParams = skill.getRequiredParameters();
        template.append("    @Override\n");
        template.append("    public Map<String, String> getRequiredParameters() {\n");
        template.append("        Map<String, String> required = new HashMap<>();\n");
        requiredParams.forEach((key, value) -> {
            template.append("        required.put(\"").append(key).append("\", \"")
                   .append(escapeJavaString(value)).append("\");\n");
        });
        template.append("        return required;\n");
        template.append("    }\n\n");
        
        // getOptionalParameters method
        Map<String, String> optionalParams = skill.getOptionalParameters();
        template.append("    @Override\n");
        template.append("    public Map<String, String> getOptionalParameters() {\n");
        template.append("        Map<String, String> optional = new HashMap<>();\n");
        optionalParams.forEach((key, value) -> {
            template.append("        optional.put(\"").append(key).append("\", \"")
                   .append(escapeJavaString(value)).append("\");\n");
        });
        template.append("        return optional;\n");
        template.append("    }\n\n");
        
        template.append("}\n");
        
        return template.toString();
    }
    
    /**
     * Create standard skill folder structure.
     * 
     * @param skillFolder the skill folder
     */
    private void createSkillStructure(Path skillFolder) {
        try {
            // Create subdirectories
            Files.createDirectories(skillFolder.resolve("src"));
            Files.createDirectories(skillFolder.resolve("src/main/java"));
            Files.createDirectories(skillFolder.resolve("src/main/resources"));
            Files.createDirectories(skillFolder.resolve("templates"));
            Files.createDirectories(skillFolder.resolve("configs"));
            Files.createDirectories(skillFolder.resolve("assets"));
            Files.createDirectories(skillFolder.resolve("docs"));
            
            // Create README.md
            String readmeContent = generateReadmeTemplate();
            Files.write(skillFolder.resolve("README.md"), readmeContent.getBytes());
            
            // Create .gitignore
            String gitignoreContent = generateGitignoreTemplate();
            Files.write(skillFolder.resolve(".gitignore"), gitignoreContent.getBytes());
            
        } catch (IOException e) {
            logger.error("Failed to create skill folder structure", e);
        }
    }
    
    /**
     * Generate README template for the skill.
     */
    private String generateReadmeTemplate() {
        return "# Skill Name\n\n" +
               "## Description\n" +
               "Provide a detailed description of what this skill does and its purpose.\n\n" +
               "## Usage\n" +
               "Describe how to use this skill, with examples of typical requests.\n\n" +
               "## Parameters\n" +
               "### Required\n" +
               "- parameter1: Description of required parameter 1\n" +
               "- parameter2: Description of required parameter 2\n\n" +
               "### Optional\n" +
               "- parameter3: Description of optional parameter 3 (default value)\n" +
               "- parameter4: Description of optional parameter 4 (default value)\n\n" +
               "## Installation\n" +
               "Provide instructions on how to install and configure this skill.\n\n" +
               "## Development\n" +
               "Include information for developers who want to modify or extend this skill.\n";
    }
    
    /**
     * Generate .gitignore template.
     */
    private String generateGitignoreTemplate() {
        return "# Compiled class files\n" +
               "*.class\n\n" +
               "# Log files\n" +
               "*.log\n\n" +
               "# Build directories\n" +
               "target/\n" +
               "build/\n" +
               "out/\n\n" +
               "# IDE files\n" +
               ".idea/\n" +
               "*.iml\n" +
               ".vscode/\n\n" +
               "# OS generated files\n" +
               ".DS_Store\n" +
               "Thumbs.db\n";
    }
    
    /**
     * Extract keywords from description text.
     * 
     * @param description the description text
     * @return list of keywords
     */
    private List<String> extractKeywords(String description) {
        if (description == null || description.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Simple keyword extraction - split by common separators
        String[] words = description.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+");
        
        return Arrays.stream(words)
                .filter(word -> word.length() > 3)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Escape Java string literals.
     * 
     * @param input the input string
     * @return escaped string
     */
    private String escapeJavaString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Batch migrate multiple skills.
     * 
     * @param skills list of skills to migrate
     * @param targetDirectory target directory
     * @return map of skill name to migration result
     */
    public Map<String, MigrationResult> batchMigrateSkills(Collection<AgentSkill> skills, Path targetDirectory) {
        Map<String, MigrationResult> results = new HashMap<>();
        
        for (AgentSkill skill : skills) {
            try {
                Path migratedPath = migrateSkillToFolder(skill, targetDirectory);
                results.put(skill.getName(), 
                    new MigrationResult(true, "Successfully migrated", migratedPath, null));
            } catch (Exception e) {
                results.put(skill.getName(), 
                    new MigrationResult(false, e.getMessage(), null, e));
            }
        }
        
        return results;
    }
    
    /**
     * Validate migrated skill structure.
     * 
     * @param skillFolder the migrated skill folder
     * @return validation result
     */
    public ValidationResult validateMigratedSkill(Path skillFolder) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Check skill descriptor
            Path descriptorFile = skillFolder.resolve("skill.json");
            if (!Files.exists(descriptorFile)) {
                errors.add("skill.json descriptor file not found");
            } else {
                SkillDescriptor descriptor = SkillDescriptor.fromFile(descriptorFile);
                if (!descriptor.isValid()) {
                    errors.add("Invalid skill descriptor");
                }
            }
            
            // Check folder structure
            String[] requiredFolders = {"src", "templates", "docs"};
            for (String folder : requiredFolders) {
                Path folderPath = skillFolder.resolve(folder);
                if (!Files.exists(folderPath)) {
                    warnings.add("Missing recommended folder: " + folder);
                }
            }
            
            // Check for implementation files
            boolean hasJavaFiles = Files.list(skillFolder)
                    .filter(path -> path.toString().endsWith(".java"))
                    .findAny()
                    .isPresent();
            
            if (!hasJavaFiles) {
                warnings.add("No Java implementation files found");
            }
            
        } catch (Exception e) {
            errors.add("Validation error: " + e.getMessage());
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Result of a migration operation.
     */
    public static class MigrationResult {
        private final boolean success;
        private final String message;
        private final Path targetPath;
        private final Exception error;
        
        public MigrationResult(boolean success, String message, Path targetPath, Exception error) {
            this.success = success;
            this.message = message;
            this.targetPath = targetPath;
            this.error = error;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Path getTargetPath() {
            return targetPath;
        }
        
        public Exception getError() {
            return error;
        }
    }
    
    /**
     * Result of skill validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}