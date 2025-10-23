package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.database.PointHistoryTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PointController 통합 테스트")
class PointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("포인트 여러번 충전 후 조회 - 전체 플로우")
    void chargeMultipleTimesAndGetUserPoint_Flow() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount1 = 1000L;
        long chargeAmount2 = 3000L;

        // when: 포인트 2회 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(chargeAmount1)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(chargeAmount2)))
                .andExpect(status().isOk());

        // then: 조회로 최종 포인트 확인
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(chargeAmount1 + chargeAmount2));
    }

    @Test
    @DisplayName("포인트 충전 → 사용 → 조회 - 전체 플로우")
    void chargeUseAndGetPoint_Flow() throws Exception {
        // given
        long userId = 2L;
        long chargeAmount = 1000L;
        long useAmount = 300L;

        // when: 포인트 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk());

        // when: 포인트 사용
        mockMvc.perform(patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(useAmount)))
                .andExpect(status().isOk());

        // then: 포인트 조회로 잔액 확인
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @DisplayName("포인트 충전 → 사용 → 충전 → 히스토리 조회 - 복합 플로우")
    void complexFlow_IntegrationScenario() throws Exception {
        // given
        long userId = 3L;

        // when: 포인트 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("5000"))
                .andExpect(status().isOk());

        // when: 포인트 사용
        mockMvc.perform(patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("2000"))
                .andExpect(status().isOk());

        // when: 포인트 재충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("3000"))
                .andExpect(status().isOk());

        // then: 히스토리 조회로 전체 거래 확인
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].type").value("USE"))
                .andExpect(jsonPath("$[2].type").value("CHARGE"));
    }
}
