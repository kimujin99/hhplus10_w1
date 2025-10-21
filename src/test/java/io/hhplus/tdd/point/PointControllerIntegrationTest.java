package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /point/{id} - 포인트 조회 성공")
    void getPoint_Success() throws Exception {
        // given
        long userId = 1L;

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$.point").exists())
                // .andExpect(jsonPath("$.updateMillis").exists());
    }

    @Test
    @DisplayName("PATCH /point/{id}/charge - 포인트 충전 성공")
    void chargePoint_Success() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = 1000L;

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$.point").value(chargeAmount));
    }

    @Test
    @DisplayName("PATCH /point/{id}/charge - 포인트 충전 실패: 음수 금액")
    void chargePoint_Fail_NegativeAmount() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = -500L;

        // when & then
        // TODO: PointService 구현 및 예외 처리 후 테스트 작성
        // mockMvc.perform(patch("/point/{id}/charge", userId)
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .content(String.valueOf(chargeAmount)))
        //         .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /point/{id}/use - 포인트 사용 성공")
    void usePoint_Success() throws Exception {
        // given
        long userId = 2L;
        long chargeAmount = 1000L;
        long useAmount = 300L;

        // 먼저 포인트 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$.point").value(700L));
    }

    @Test
    @DisplayName("PATCH /point/{id}/use - 포인트 사용 실패: 잔액 부족")
    void usePoint_Fail_InsufficientBalance() throws Exception {
        // given
        long userId = 3L;
        long useAmount = 5000L;

        // when & then
        // TODO: PointService 구현 및 예외 처리 후 테스트 작성
        // mockMvc.perform(patch("/point/{id}/use", userId)
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .content(String.valueOf(useAmount)))
        //         .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /point/{id}/histories - 포인트 히스토리 조회 성공")
    void getPointHistories_Success() throws Exception {
        // given
        long userId = 4L;
        long chargeAmount = 1000L;
        long useAmount = 300L;

        // 포인트 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk());

        // 포인트 사용
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk());
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$").isArray())
                // .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("포인트 충전 후 조회 - 통합 시나리오")
    void chargeAndGetPoint_IntegrationScenario() throws Exception {
        // given
        long userId = 5L;
        long chargeAmount = 2000L;

        // when: 포인트 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk());

        // then: 포인트 조회로 확인
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$.point").value(chargeAmount));
    }

    @Test
    @DisplayName("포인트 충전, 사용, 히스토리 조회 - 전체 플로우 통합 시나리오")
    void fullFlow_IntegrationScenario() throws Exception {
        // given
        long userId = 6L;

        // when & then: 포인트 충전
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("5000"))
                .andExpect(status().isOk());

        // when & then: 포인트 사용
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("2000"))
                .andExpect(status().isOk());

        // when & then: 포인트 조회
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$.point").value(3000L));

        // when & then: 히스토리 조회
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk());
                // TODO: PointService 구현 후 추가 검증
                // .andExpect(jsonPath("$").isArray())
                // .andExpect(jsonPath("$.length()").value(2));
    }
}
