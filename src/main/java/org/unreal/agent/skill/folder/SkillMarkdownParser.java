package org.unreal.agent.skill.folder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Parser for SKILL.md files following the agentskills.io specification.
 * Extracts YAML frontmatter and Markdown body.
 */
public class SkillMarkdownParser {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Parse a SKILL.md file.
     * 
     * @param skillFile the path to SKILL.md
     * @return SkillDescriptor with populated metadata and instructions
     * @throws IOException if file cannot be read or parsed
     */
    public static SkillDescriptor parse(Path skillFile) throws IOException {
        List<String> lines = Files.readAllLines(skillFile);
        
        StringBuilder frontmatter = new StringBuilder();
        StringBuilder body = new StringBuilder();
        
        boolean inFrontmatter = false;
        boolean frontmatterFound = false;
        
        for (String line : lines) {
            if (line.trim().equals("---")) {
                if (!inFrontmatter && !frontmatterFound) {
                    inFrontmatter = true;
                } else if (inFrontmatter) {
                    inFrontmatter = false;
                    frontmatterFound = true;
                } else {
                    body.append(line).append("\n");
                }
                continue;
            }
            
            if (inFrontmatter) {
                frontmatter.append(line).append("\n");
            } else {
                body.append(line).append("\n");
            }
        }
        
        SkillDescriptor descriptor;
        if (frontmatter.length() > 0) {
            descriptor = yamlMapper.readValue(frontmatter.toString(), SkillDescriptor.class);
        } else {
            descriptor = new SkillDescriptor();
        }
        
        descriptor.setInstructions(body.toString().trim());
        
        // Set default version if missing as per spec it's optional but we might want it
        if (descriptor.getVersion() == null) {
            descriptor.setVersion("1.0");
        }
        
        return descriptor;
    }
}