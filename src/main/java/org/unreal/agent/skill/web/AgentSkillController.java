package org.unreal.agent.skill.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.springai.SpringAIAgentSkillAdapter;

import java.util.List;
import java.util.Map;

/**
 * REST controller for exposing agent skills to third-party Spring AI services.
 * Provides endpoints for skill discovery, activation, and execution according to agentskills.io specification.
 */
@RestController
@RequestMapping("/api/agent-skills")
public class AgentSkillController {

    @Autowired
    private AgentSkillManager skillManager;

    @Autowired
    private SpringAIAgentSkillAdapter springAIAgentSkillAdapter;

    /**
     * Get all registered skills for discovery phase (tier 1 of progressive disclosure).
     * 
     * @return List of skill discovery information
     */
    @GetMapping("/discovery")
    public ResponseEntity<List<String>> getSkillDiscoveryInfo() {
        List<String> discoveryInfo = springAIAgentSkillAdapter.getSkillDiscoveryInfo();
        return ResponseEntity.ok(discoveryInfo);
    }

    /**
     * Get all skills information formatted for agentskills.io integration.
     * 
     * @return Complete skill information organized by tier
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSkills() {
        Map<String, Object> allSkillsInfo = springAIAgentSkillAdapter.getAllSkillsForAgentskillsIo();
        return ResponseEntity.ok(allSkillsInfo);
    }

    /**
     * Execute a skill with the given parameters.
     * 
     * @param skillName the name of the skill to execute
     * @param request the skill execution request
     * @return execution result
     */
    @PostMapping("/execute/{skillName}")
    public ResponseEntity<Object> executeSkill(@PathVariable String skillName, 
                                             @RequestBody Map<String, Object> request) {
        try {
            // Extract the actual request text from the payload
            String requestText = (String) request.getOrDefault("request", "");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", Map.of());
            
            // Execute the skill
            Object result = springAIAgentSkillAdapter.executeFunction(skillName, parameters);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Skill execution failed: " + e.getMessage()));
        }
    }

    /**
     * Get function definitions for Spring AI integration.
     * 
     * @return List of function definitions compatible with Spring AI
     */
    @GetMapping("/spring-ai-functions")
    public ResponseEntity<List<Map<String, Object>>> getSpringAIFunctions() {
        List<Map<String, Object>> functionDefinitions = springAIAgentSkillAdapter.getFunctionDefinitions();
        return ResponseEntity.ok(functionDefinitions);
    }

    /**
     * Get all registered skill names.
     * 
     * @return List of skill names
     */
    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllSkillNames() {
        List<String> skillNames = skillManager.getAllSkills().stream()
                .map(skill -> skill.getName())
                .toList();
        return ResponseEntity.ok(skillNames);
    }

    /**
     * Get detailed information about a specific skill.
     * 
     * @param skillName the name of the skill
     * @return Skill details
     */
    @GetMapping("/{skillName}")
    public ResponseEntity<Map<String, Object>> getSkillDetails(@PathVariable String skillName) {
        var skill = skillManager.getSkill(skillName);
        if (skill == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> skillDetails = Map.of(
                "name", skill.getName(),
                "description", skill.getDescription(),
                "version", skill.getVersion(),
                "requiredParameters", skill.getRequiredParameters(),
                "optionalParameters", skill.getOptionalParameters(),
                "instructions", skill.getInstructions()
        );

        return ResponseEntity.ok(skillDetails);
    }
}