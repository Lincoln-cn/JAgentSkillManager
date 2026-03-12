package org.unreal.agent.skill.util;

import java.util.regex.Pattern;

/**
 * Input validation utilities for the Agent Skill framework.
 */
public class InputValidationUtils {
    
    private static final Pattern SKILL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("\\.\\.(/|\\\\)");
    private static final int MAX_INPUT_LENGTH = 10000; // 10KB limit
    
    /**
     * Validates a skill name according to security requirements.
     * 
     * @param skillName the skill name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSkillName(String skillName) {
        if (skillName == null) {
            return false;
        }
        
        return SKILL_NAME_PATTERN.matcher(skillName).matches();
    }
    
    /**
     * Validates a file path to prevent directory traversal attacks.
     * 
     * @param path the file path to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidFilePath(String path) {
        if (path == null) {
            return false;
        }
        
        // Check for path traversal attempts
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            return false;
        }
        
        // Additional checks could be added here
        
        return true;
    }
    
    /**
     * Validates input length to prevent excessive resource consumption.
     * 
     * @param input the input to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInputLength(String input) {
        if (input == null) {
            return true; // Null is considered valid (will be handled elsewhere)
        }
        
        return input.length() <= MAX_INPUT_LENGTH;
    }
    
    /**
     * Sanitizes user input by removing potentially dangerous characters.
     * 
     * @param input the input to sanitize
     * @return sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potential path traversal sequences
        input = PATH_TRAVERSAL_PATTERN.matcher(input).replaceAll("");
        
        // Additional sanitization could be added here
        
        return input;
    }
    
    /**
     * Validates that a string contains only safe characters.
     * 
     * @param input the input to validate
     * @param allowedPattern the regex pattern of allowed characters
     * @return true if valid, false otherwise
     */
    public static boolean isValidPattern(String input, String allowedPattern) {
        if (input == null) {
            return true; // Null is considered valid
        }
        
        if (allowedPattern == null) {
            return false;
        }
        
        try {
            return java.util.regex.Pattern.matches(allowedPattern, input);
        } catch (Exception e) {
            return false; // Invalid pattern
        }
    }
}