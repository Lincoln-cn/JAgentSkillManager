package org.unreal.agent.skill.folder;

import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillResult;

import java.util.Collections;
import java.util.Map;

/**
 * A lightweight AgentSkill implementation backed by a SkillDescriptor.
 * Used when a descriptor exists but no executable implementation (class/JAR/script) is available.
 */
public class DescriptorAgentSkill implements AgentSkill {

    private final SkillDescriptor descriptor;

    public DescriptorAgentSkill(SkillDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Expose underlying descriptor for management/API usage (read-only).
     */
    public SkillDescriptor getDescriptor() {
        return descriptor;
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
        // Descriptor-backed skills are discoverable but do not claim handling responsibility
        // aggressively. Return true to make them discoverable by name/metadata.
        return false;
    }

    @Override
    public AgentSkillResult execute(String request, Map<String, Object> parameters) {
        // Execution for descriptor-only skill returns its instructions/metadata
        String instructions = descriptor.getInstructions();
        Object payload = instructions != null && !instructions.isBlank() ? instructions : descriptor;

        return AgentSkillResult.success()
                .message("Descriptor-only skill: returning metadata/instructions")
                .data(payload)
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
