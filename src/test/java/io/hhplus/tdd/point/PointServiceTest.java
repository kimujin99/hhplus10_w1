package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    /*
    * 유저 포인트 테스트
    * */
    @ParameterizedTest
    @CsvSource({
            "1, 1000",
            "2, 2000"
    })
    @DisplayName("포인트 조회 - 성공")
    void getUserPoint_Success(long userId, long amount) {
        // given
        UserPoint expected = new UserPoint(userId, amount, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(expected);

        // when
        UserPoint result = pointService.getUserPoint(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(amount);
    }

    /*
    * 포인트 히스토리 테스트
    * */
    @Test
    @DisplayName("포인트 히스토리 조회 - 성공")
    void getPointHistory_Success() {
        // given
        long userId = 1L;
        List<PointHistory> expected = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(1L, userId, 300L, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expected);

        // when
        List<PointHistory> histories = pointService.getPointHistory(userId);

        // then
        assertThat(histories).isNotNull();
        assertThat(histories).hasSize(2);
        // 어디까지 검증해야 하는지?
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(histories.get(0).amount()).isEqualTo(1000L);
        assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(histories.get(1).amount()).isEqualTo(300L);
    }

    @Test
    @DisplayName("포인트 충전 - 성공")
    void chargePoint_Success() {
        // given
        long userId = 1L;
        long chargeAmount = 1000L;
        long currentPoint = 1000L;
        long newPoint = chargeAmount + currentPoint;

        UserPoint current = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        UserPoint charged = new UserPoint(userId, newPoint, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(current);
        when(userPointTable.insertOrUpdate(userId, newPoint)).thenReturn(charged);

        // when
        UserPoint result = pointService.chargePoint(userId, chargeAmount);

        // then
        // 1. 반환값 검증
        assertThat(result).isNotNull();
        assertThat(result.point()).isEqualTo(newPoint);

        // 2. 포인트 조회 검증
        verify(userPointTable).selectById(userId);

        // 3. 포인트 충전 검증
        verify(userPointTable).insertOrUpdate(userId, newPoint);

        // 4. 포인트 내역 등록 검증
        verify(pointHistoryTable).insert(
                eq(userId),
                eq(chargeAmount),
                eq(TransactionType.CHARGE),
                anyLong()
        );
    }

    @Test
    @DisplayName("포인트 충전 - 실패: 음수 금액")
    void chargePoint_Fail_NegativeAmount() {
        // given
        long userId = 1L;
        long chargeAmount = -1000L;

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("충전 금액은 0보다 커야합니다.");
    }

    @Test
    @DisplayName("포인트 충전 - 실패: 단위 틀림(1000원)")
    void chargePoint_Fail_InvalidUnit() {
        // given
        long userId = 1L;
        long chargeAmount = 100L;

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("충전은 1000원 단위로만 가능합니다.");
    }

    @Test
    @DisplayName("포인트 사용 - 성공")
    void usePoint_Success() {
        // given
        long userId = 1L;
        long useAmount = 300L;
        long currentPoint = 1000L;
        long newPoint = currentPoint - useAmount;

        UserPoint current = new UserPoint(userId, currentPoint, System.currentTimeMillis());
        UserPoint used = new UserPoint(userId, newPoint, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(current);
        when(userPointTable.insertOrUpdate(userId, newPoint)).thenReturn(used);

        // when
        UserPoint result = pointService.usePoint(userId, useAmount);

        // then
        // 1. 반환값 검증
        assertThat(result).isNotNull();
        assertThat(result.point()).isEqualTo(700L);

        // 2. 포인트 조회 검증
        verify(userPointTable).selectById(userId);

        // 3. 포인트 사용 검증
        verify(userPointTable).insertOrUpdate(userId, newPoint);

        // 4. 포인트 내역 등록 검증
        verify(pointHistoryTable).insert(
                eq(userId),
                eq(useAmount),
                eq(TransactionType.USE),
                anyLong()
        );
    }

    @Test
    @DisplayName("포인트 사용 - 실패: 잔액 부족")
    void usePoint_Fail_InsufficientBalance() {
        // given
        long userId = 1L;
        long useAmount = 2000L;
        long currentPoint = 1000L;

        // when & then
        // TODO: PointService 구현 후 예외 테스트 작성
        // assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
        //     .isInstanceOf(IllegalArgumentException.class)
        //     .hasMessage("포인트 잔액이 부족합니다.");
    }

    @Test
    @DisplayName("포인트 사용 - 실패: 음수 금액")
    void usePoint_Fail_NegativeAmount() {
        // given
        long userId = 1L;
        long useAmount = -500L;

        // when & then
        // TODO: PointService 구현 후 예외 테스트 작성
        // assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
        //     .isInstanceOf(IllegalArgumentException.class)
        //     .hasMessage("사용 금액은 0보다 커야 합니다.");
    }
}
