package org.unreal.agent.skill.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

/**
 * A secure class loader that restricts what classes can be loaded based on security policies.
 */
public class SecureClassLoader extends URLClassLoader {
    
    private final Path allowedBasePath;
    
    public SecureClassLoader(URL[] urls, ClassLoader parent, Path allowedBasePath) {
        super(urls, parent);
        this.allowedBasePath = allowedBasePath;
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        
        // Validate the class name against security policy
        if (!SecurityUtils.isAllowedClassName(name)) {
            throw new SecurityException("Class loading denied: " + name + 
                ". This class is not in the allowed packages list.");
        }
        
        // Check if the class is in the parent loader
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // Class not found in parent, try to find it in this loader
        }
        
        // Attempt to find and load the class from our URLs
        c = findClass(name);
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Validate the class name
        if (!SecurityUtils.isAllowedClassName(name)) {
            throw new SecurityException("Class loading denied: " + name);
        }
        
        // Convert class name to file path
        String path = name.replace('.', '/').concat(".class");
        
        // Find the class file in our URLs
        byte[] classData = loadClassData(path);
        if (classData != null) {
            // Define the class with a protection domain that restricts permissions
            ProtectionDomain domain = createRestrictedProtectionDomain();
            return defineClass(name, classData, 0, classData.length, domain);
        }
        
        return super.findClass(name);
    }
    
    private byte[] loadClassData(String path) {
        // Search for the class file in our URLs
        for (URL url : getURLs()) {
            try {
                // Ensure the URL is within the allowed base path
                if (isPathAllowed(url)) {
                    URL classUrl = new URL(url, path);
                    try (InputStream is = classUrl.openStream();
                         ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                        
                        int nRead;
                        byte[] data = new byte[1024];
                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        return buffer.toByteArray();
                    }
                }
            } catch (IOException e) {
                // Continue to next URL
            }
        }
        return null;
    }
    
    private boolean isPathAllowed(URL url) {
        try {
            Path path = Path.of(url.toURI());
            // Verify that the path is within the allowed base path
            return path.normalize().startsWith(allowedBasePath.normalize());
        } catch (Exception e) {
            return false;
        }
    }
    
    private ProtectionDomain createRestrictedProtectionDomain() {
        Permissions permissions = new Permissions();
        // Add minimal necessary permissions
        // In a real implementation, you might want to add specific permissions
        // based on the skill's declared requirements
        
        CodeSource codeSource = new CodeSource(null, (Certificate[]) null);
        return new ProtectionDomain(codeSource, (PermissionCollection) null);
    }
}