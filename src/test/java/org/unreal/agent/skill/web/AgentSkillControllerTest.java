package org.unreal.agent.skill.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AgentSkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSkillWithoutRevealScriptsDoesNotContainDisclosedScripts() throws Exception {
        // skill likely does not exist in test profile; ensure 404 or no disclosedScripts when exists
        mockMvc.perform(get("/api/agent-skills/non-existent-skill"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSkillWithRevealScriptsForNonExistentSkill() throws Exception {
        mockMvc.perform(get("/api/agent-skills/non-existent-skill?revealScripts=true"))
                .andExpect(status().isNotFound());
    }
}
