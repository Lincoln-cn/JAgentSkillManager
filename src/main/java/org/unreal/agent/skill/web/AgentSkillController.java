package org.unreal.agent.skill.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.AgentSkillResult;
import org.unreal.agent.skill.folder.DescriptorAgentSkill;
import org.unreal.agent.skill.folder.SkillDescriptor;

import java.util.*;

/**
 * REST controller for agent skills.
 * Provides endpoints for skill discovery, execution, and querying.
 */
@RestController
@RequestMapping("/api/agent-skills")
public class AgentSkillController {

    @Autowired
    private AgentSkillManager skillManager;

    /**
     * Get all registered skill names.
     *
     * @return List of skill names
     */
    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllSkillNames() {
        List<String> skillNames = skillManager.getAllSkills().stream()
                .map(AgentSkill::getName)
                .toList();
        return ResponseEntity.ok(skillNames);
    }

    /**
     * Get all skills with basic information.
     *
     * @return List of skills
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSkills() {
        List<Map<String, Object>> skills = skillManager.getAllSkills().stream()
                .map(skill -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", skill.getName());
                    map.put("description", skill.getDescription());
                    map.put("version", skill.getVersion());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of("skills", skills));
    }

    /**
     * Get detailed information about a specific skill.
     *
     * @param skillName the name of the skill
     * @return Skill details
     */
    @GetMapping("/{skillName}")
    public ResponseEntity<Map<String, Object>> getSkillDetails(@PathVariable String skillName,
                                                                @RequestParam(name = "revealScripts", defaultValue = "false") boolean revealScripts) {
        var skill = skillManager.getSkill(skillName);
        if (skill == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> skillDetails = new LinkedHashMap<>();
        skillDetails.put("name", skill.getName());
        skillDetails.put("description", skill.getDescription());
        skillDetails.put("version", skill.getVersion());
        skillDetails.put("requiredParameters", skill.getRequiredParameters());
        skillDetails.put("optionalParameters", skill.getOptionalParameters());
        skillDetails.put("instructions", skill.getInstructions());

        // Optionally reveal disclosed script contents kept in descriptor.extraMetadata.disclosedScripts
        if (revealScripts) {
            if (skill instanceof DescriptorAgentSkill) {
                SkillDescriptor desc = ((DescriptorAgentSkill) skill).getDescriptor();
                if (desc != null && desc.getExtraMetadata() != null && desc.getExtraMetadata().containsKey("disclosedScripts")) {
                    skillDetails.put("disclosedScripts", desc.getExtraMetadata().get("disclosedScripts"));
                }
            }
        }

        return ResponseEntity.ok(skillDetails);
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
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", Map.of());

            AgentSkillResult result = skillManager.executeSkill(skillName, "", parameters);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Skill execution failed: " + e.getMessage()));
        }
    }

    /**
     * Find a skill that can handle the given request.
     *
     * @param request the request to find a skill for
     * @return skill name if found
     */
    @PostMapping("/find")
    public ResponseEntity<Map<String, Object>> findSkill(@RequestBody Map<String, String> request) {
        String requestText = request.getOrDefault("request", "");
        AgentSkill skill = skillManager.findSkillForRequest(requestText);

        if (skill == null) {
            return ResponseEntity.ok(Map.of("found", false));
        }

        return ResponseEntity.ok(Map.of(
                "found", true,
                "skillName", skill.getName(),
                "description", skill.getDescription()
        ));
    }
}
