package org.unreal.agent.skill.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.AgentSkillResult;
import org.unreal.agent.skill.folder.DescriptorAgentSkill;
import org.unreal.agent.skill.folder.SkillDescriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AgentSkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentSkillManager skillManager;

    @BeforeEach
    void setUp() {
        // Default setup - can be overridden in specific tests
    }

    @Test
    void getSkillWithoutRevealScriptsDoesNotContainDisclosedScripts() throws Exception {
        mockMvc.perform(get("/api/agent-skills/non-existent-skill"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSkillWithRevealScriptsForNonExistentSkill() throws Exception {
        mockMvc.perform(get("/api/agent-skills/non-existent-skill?revealScripts=true"))
                .andExpect(status().isNotFound());
    }

    // Discovery API Tests
    @Test
    void getAllSkillNamesReturnsListOfSkillNames() throws Exception {
        AgentSkill skill1 = createMockSkill("test-skill-1", "Test Skill 1", "1.0.0");
        AgentSkill skill2 = createMockSkill("test-skill-2", "Test Skill 2", "1.0.0");

        when(skillManager.getAllSkills()).thenReturn(List.of(skill1, skill2));

        mockMvc.perform(get("/api/agent-skills/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("test-skill-1"))
                .andExpect(jsonPath("$[1]").value("test-skill-2"));
    }

    @Test
    void getAllSkillNamesReturnsEmptyListWhenNoSkills() throws Exception {
        when(skillManager.getAllSkills()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/agent-skills/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // All Skills API Tests
    @Test
    void getAllSkillsReturnsAllSkillsWithBasicInfo() throws Exception {
        AgentSkill skill1 = createMockSkill("test-skill-1", "First test skill", "1.0.0");
        AgentSkill skill2 = createMockSkill("test-skill-2", "Second test skill", "2.0.0");

        when(skillManager.getAllSkills()).thenReturn(List.of(skill1, skill2));

        mockMvc.perform(get("/api/agent-skills/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills.length()").value(2));
    }

    @Test
    void getAllSkillsIncludesNameDescriptionVersion() throws Exception {
        AgentSkill skill = createMockSkill("my-skill", "My awesome skill", "1.5.0");

        when(skillManager.getAllSkills()).thenReturn(List.of(skill));

        mockMvc.perform(get("/api/agent-skills/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills[0].name").value("my-skill"))
                .andExpect(jsonPath("$.skills[0].description").value("My awesome skill"))
                .andExpect(jsonPath("$.skills[0].version").value("1.5.0"));
    }

    // Skill Details API Tests
    @Test
    void getSkillDetailsReturns200WithSkillInfoForExistingSkill() throws Exception {
        AgentSkill skill = createMockSkillWithParameters(
                "existing-skill",
                "An existing skill",
                "1.0.0",
                Map.of("param1", "Required parameter"),
                Map.of("param2", "Optional parameter")
        );

        when(skillManager.getSkill("existing-skill")).thenReturn(skill);

        mockMvc.perform(get("/api/agent-skills/existing-skill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("existing-skill"))
                .andExpect(jsonPath("$.description").value("An existing skill"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void getSkillDetailsIncludesAllExpectedFields() throws Exception {
        AgentSkill skill = createMockSkillWithParameters(
                "complete-skill",
                "A complete skill with all fields",
                "2.0.0",
                Map.of("requiredParam", "A required parameter"),
                Map.of("optionalParam", "An optional parameter")
        );

        when(skillManager.getSkill("complete-skill")).thenReturn(skill);

        mockMvc.perform(get("/api/agent-skills/complete-skill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.requiredParameters").exists())
                .andExpect(jsonPath("$.optionalParameters").exists());
    }

    @Test
    void getSkillDetailsWithRevealScriptsTrueIncludesDisclosedScripts() throws Exception {
        SkillDescriptor descriptor = new SkillDescriptor();
        descriptor.setName("script-skill");
        descriptor.setDescription("Skill with scripts");
        descriptor.setVersion("1.0.0");
        descriptor.setInstructions("Some instructions");

        Map<String, Object> disclosedScripts = new HashMap<String, Object>();
        disclosedScripts.put("script1", "console.log('hello');");
        disclosedScripts.put("script2", "print('world');");
        descriptor.addExtraMetadata("disclosedScripts", disclosedScripts);

        DescriptorAgentSkill skill = new DescriptorAgentSkill(descriptor);

        when(skillManager.getSkill("script-skill")).thenReturn(skill);

        mockMvc.perform(get("/api/agent-skills/script-skill?revealScripts=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disclosedScripts").exists())
                .andExpect(jsonPath("$.disclosedScripts.script1").value("console.log('hello');"))
                .andExpect(jsonPath("$.disclosedScripts.script2").value("print('world');"));
    }

    @Test
    void getSkillDetailsWithRevealScriptsFalseDoesNotIncludeDisclosedScripts() throws Exception {
        SkillDescriptor descriptor = new SkillDescriptor();
        descriptor.setName("script-skill");
        descriptor.setDescription("Skill with scripts");
        descriptor.setVersion("1.0.0");
        descriptor.setInstructions("Some instructions");

        Map<String, Object> disclosedScripts = new HashMap<String, Object>();
        disclosedScripts.put("script1", "console.log('hello');");
        descriptor.addExtraMetadata("disclosedScripts", disclosedScripts);

        DescriptorAgentSkill skill = new DescriptorAgentSkill(descriptor);

        when(skillManager.getSkill("script-skill")).thenReturn(skill);

        mockMvc.perform(get("/api/agent-skills/script-skill?revealScripts=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disclosedScripts").doesNotExist());
    }

    // Skill Execution API Tests
    @Test
    void executeSkillReturns200AndResultForValidSkill() throws Exception {
        AgentSkillResult result = AgentSkillResult.success()
                .message("Skill executed successfully")
                .data(Map.of("result", "success"))
                .skillName("valid-skill")
                .build();

        when(skillManager.executeSkill(anyString(), anyString(), any())).thenReturn(result);

        String requestBody = "{\"parameters\": {\"key\": \"value\"}}";

        mockMvc.perform(post("/api/agent-skills/execute/valid-skill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Skill executed successfully"))
                .andExpect(jsonPath("$.skillName").value("valid-skill"));
    }

    @Test
    void executeSkillReturns400ForExecutionFailure() throws Exception {
        when(skillManager.executeSkill(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Execution failed"));

        String requestBody = "{\"parameters\": {}}";

        mockMvc.perform(post("/api/agent-skills/execute/failing-skill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Skill execution failed: Execution failed"));
    }

    @Test
    void executeSkillHandlesMissingParametersGracefully() throws Exception {
        AgentSkillResult result = AgentSkillResult.success()
                .message("Skill executed with default parameters")
                .skillName("skill-with-defaults")
                .build();

        when(skillManager.executeSkill(anyString(), anyString(), any())).thenReturn(result);

        String requestBody = "{}";

        mockMvc.perform(post("/api/agent-skills/execute/skill-with-defaults")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // Find Skill API Tests
    @Test
    void findSkillReturnsFoundTrueWithSkillInfoWhenMatchExists() throws Exception {
        AgentSkill skill = createMockSkill("matching-skill", "A skill that matches", "1.0.0");

        when(skillManager.findSkillForRequest("process this request")).thenReturn(skill);

        String requestBody = "{\"request\": \"process this request\"}";

        mockMvc.perform(post("/api/agent-skills/find")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.skillName").value("matching-skill"))
                .andExpect(jsonPath("$.description").value("A skill that matches"));
    }

    @Test
    void findSkillReturnsFoundFalseWhenNoMatch() throws Exception {
        when(skillManager.findSkillForRequest("unknown request")).thenReturn(null);

        String requestBody = "{\"request\": \"unknown request\"}";

        mockMvc.perform(post("/api/agent-skills/find")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.skillName").doesNotExist());
    }

    // Helper methods to create mock skills
    private AgentSkill createMockSkill(String name, String description, String version) {
        AgentSkill skill = mock(AgentSkill.class);
        when(skill.getName()).thenReturn(name);
        when(skill.getDescription()).thenReturn(description);
        when(skill.getVersion()).thenReturn(version);
        when(skill.getRequiredParameters()).thenReturn(Collections.emptyMap());
        when(skill.getOptionalParameters()).thenReturn(Collections.emptyMap());
        when(skill.getInstructions()).thenReturn(null);
        return skill;
    }

    private AgentSkill createMockSkillWithParameters(
            String name,
            String description,
            String version,
            Map<String, String> requiredParams,
            Map<String, String> optionalParams) {
        AgentSkill skill = mock(AgentSkill.class);
        when(skill.getName()).thenReturn(name);
        when(skill.getDescription()).thenReturn(description);
        when(skill.getVersion()).thenReturn(version);
        when(skill.getRequiredParameters()).thenReturn(requiredParams);
        when(skill.getOptionalParameters()).thenReturn(optionalParams);
        when(skill.getInstructions()).thenReturn(null);
        return skill;
    }
}
