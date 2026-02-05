package org.unreal.agent.skill.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Skill 验证结果
 */
public class SkillValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;

    public SkillValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}