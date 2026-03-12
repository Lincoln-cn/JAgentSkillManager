package org.unreal.agent.skill.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Security utility methods for the Agent Skill framework.
 */
public class SecurityUtils {
    
    // Whitelist of allowed packages for dynamic loading
    private static final Set<String> ALLOWED_PACKAGES = new HashSet<>(Arrays.asList(
        "org.unreal.agent.skill.",
        "java.lang.",
        "java.util.",
        "java.math.",
        "java.time."
    ));
    
    /**
     * Validates if a class name is allowed for dynamic loading.
     * 
     * @param className the class name to validate
     * @return true if the class is allowed, false otherwise
     */
    public static boolean isAllowedClassName(String className) {
        if (className == null) {
            return false;
        }
        
        // Block dangerous packages
        if (className.startsWith("java.io.") ||
            className.startsWith("java.net.") ||
            className.startsWith("java.nio.") ||
            className.startsWith("javax.script.") ||
            className.startsWith("sun.") ||
            className.startsWith("com.sun.")) {
            return false;
        }
        
        // Allow specific safe packages
        for (String allowedPackage : ALLOWED_PACKAGES) {
            if (className.startsWith(allowedPackage)) {
                return true;
            }
        }
        
        // Allow classes in the skill's own package (which should be in the skill directory)
        return className.startsWith("skill.");
    }
    
    /**
     * Validates if a resource path is allowed for access.
     * 
     * @param resourcePath the resource path to validate
     * @return true if the resource is allowed, false otherwise
     */
    public static boolean isAllowedResourcePath(String resourcePath) {
        if (resourcePath == null) {
            return false;
        }
        
        // Normalize the path to prevent directory traversal
        String normalizedPath = normalizePath(resourcePath);
        
        // Block access to sensitive files/directories
        if (normalizedPath.contains("../") || 
            normalizedPath.contains("..\\") || 
            normalizedPath.startsWith("/") || 
            normalizedPath.startsWith("\\")) {
            return false;
        }
        
        // Only allow access to skill-specific resources
        return normalizedPath.startsWith("skill/") || 
               normalizedPath.startsWith("META-INF/");
    }
    
    /**
     * Normalizes a path to prevent directory traversal attacks.
     * 
     * @param path the path to normalize
     * @return the normalized path
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        
        // Replace backslashes with forward slashes for consistent processing
        path = path.replace('\\', '/');
        
        // Split the path into components
        String[] parts = path.split("/");
        java.util.Stack<String> stack = new java.util.Stack<>();
        
        for (String part : parts) {
            if (part.equals("..")) {
                if (!stack.isEmpty() && !stack.peek().equals("..")) {
                    stack.pop();
                } else {
                    // If we're trying to go above the root, add the .. anyway
                    // This will be caught by the validation logic
                    stack.push(part);
                }
            } else if (!part.isEmpty() && !part.equals(".")) {
                stack.push(part);
            }
        }
        
        StringBuilder normalized = new StringBuilder();
        for (String part : stack) {
            normalized.append("/").append(part);
        }
        
        return normalized.length() > 0 ? normalized.substring(1) : "";
    }
}