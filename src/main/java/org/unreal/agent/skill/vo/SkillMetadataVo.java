package org.unreal.agent.skill.vo;

import java.util.Map;

/**
 * Skill 元数据 Value Object
 */
public class SkillMetadataVo {
    
    private final String name;
    private final String discoveryInfo;
    private final Map<String, Object> fullMetadata;
    private final String instructions;

    public SkillMetadataVo(String discoveryInfo, Map<String, Object> fullMetadata, String instructions) {
        this.discoveryInfo = discoveryInfo;
        this.fullMetadata = fullMetadata;
        this.instructions = instructions;
        this.name = (String) fullMetadata.get("name");
    }

    public String getName() {
        return name;
    }

    public String getDiscoveryInfo() {
        return discoveryInfo;
    }

    public Map<String, Object> getFullMetadata() {
        return fullMetadata;
    }

    public String getInstructions() {
        return instructions;
    }
}