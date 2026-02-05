package org.unreal.agent.skill.folder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Utility class for reading YAML files as Properties.
 */
public class YamlPropertiesReader {
    
    /**
     * Read YAML file and convert to Properties.
     * 
     * @param yamlFile the YAML file path
     * @return Properties object
     * @throws IOException if file cannot be read
     */
    public static Properties readYamlAsProperties(Path yamlFile) throws IOException {
        Properties properties = new Properties();
        
        if (!Files.exists(yamlFile)) {
            throw new IOException("YAML file not found: " + yamlFile);
        }
        
        try (InputStream inputStream = Files.newInputStream(yamlFile)) {
            return readYamlAsProperties(inputStream);
        }
    }
    
    /**
     * Read YAML input stream and convert to Properties.
     * 
     * @param inputStream the YAML input stream
     * @return Properties object
     * @throws IOException if stream cannot be read
     */
    public static Properties readYamlAsProperties(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        
        // Simple YAML parser for basic structure
        // For more complex YAML, consider using SnakeYAML library
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            content.append(new String(buffer, 0, bytesRead));
        }
        
        String[] lines = content.toString().split("\\n");
        String currentKey = null;
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // Handle nested properties with indentation
            if (line.startsWith("  ")) {
                if (currentKey != null) {
                    line = line.trim();
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        String subKey = currentKey + "." + parts[0].trim();
                        String value = parts.length > 1 ? parts[1].trim() : "";
                        properties.setProperty(subKey, value);
                    }
                }
            } else {
                // Top-level property
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    currentKey = parts[0].trim();
                    String value = parts.length > 1 ? parts[1].trim() : "";
                    properties.setProperty(currentKey, value);
                }
            }
        }
        
        return properties;
    }
}