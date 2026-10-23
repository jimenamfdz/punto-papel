package com.puntoypapel.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SaludControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SaludController())
            .build();

    @Test
    void respondeSaludo() throws Exception {
        mockMvc.perform(get("/api/saludo"))
                .andExpect(status().isOk())
                .andExpect(content().string("Punto & Papel API funcionando"));
    }
}
