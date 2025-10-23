package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // 포인트 정책 단위 설정
    private static final int CHARGE_UNIT = 1000;
    private static final int USE_UNIT = 100;

    /**
     * 포인트 조회
     */
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 포인트 충전/이용 내역 조회
     */
    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 포인트 충전
     */
    public UserPoint chargePoint (long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야합니다.");
        }
        if(amount % CHARGE_UNIT != 0) {
            throw new IllegalArgumentException(String.format("충전은 %s원 단위로만 가능합니다.", CHARGE_UNIT));
        }

        // 1. 포인트 조회
        UserPoint current = userPointTable.selectById(userId);
        // 2. 포인트 충전
        UserPoint updated = userPointTable.insertOrUpdate(userId, current.point() + amount);
        // 3. 포인트 히스토리 등록
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updated.updateMillis());

        return updated;
    }

    /**
     * 포인트 사용
     */
    public UserPoint usePoint (String userId, long amount) {
        if(amount % USE_UNIT != 0) {
            throw new IllegalArgumentException(String.format("사용은 %s원 단위로만 가능합니다.", USE_UNIT));
        }
        return null;
    }
}
