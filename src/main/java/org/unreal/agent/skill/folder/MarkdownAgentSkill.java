package org.unreal.agent.skill.folder;

import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AgentSkill for SKILL.md based skills.
 * Primarily provides instructions and metadata to the agent.
 */
public class MarkdownAgentSkill implements AgentSkill {

    private final SkillDescriptor descriptor;

    public MarkdownAgentSkill(SkillDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public String getDescription() {
        return descriptor.getDescription();
    }

    @Override
    public String getVersion() {
        return descriptor.getVersion();
    }

    @Override
    public boolean canHandle(String request) {
        // Markdown skills are usually context-based, 
        // they don't necessarily "handle" specific requests in the traditional way
        // but can be activated if the description matches.
        return true; 
    }

    @Override
    public AgentSkillResult execute(String request, Map<String, Object> parameters) {
        // Executing a markdown skill might mean returning its instructions or
        // performing a default action.
        return AgentSkillResult.success()
                .message("Markdown skill metadata retrieved")
                .data(descriptor.getInstructions())
                .skillName(getName())
                .build();
    }

    @Override
    public Map<String, String> getRequiredParameters() {
        if (descriptor.getParameters() != null && descriptor.getParameters().getRequired() != null) {
            return descriptor.getParameters().getRequired();
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getOptionalParameters() {
        if (descriptor.getParameters() != null && descriptor.getParameters().getOptional() != null) {
            return descriptor.getParameters().getOptional();
        }
        return Collections.emptyMap();
    }

    @Override
    public String getInstructions() {
        return descriptor.getInstructions();
    }
}