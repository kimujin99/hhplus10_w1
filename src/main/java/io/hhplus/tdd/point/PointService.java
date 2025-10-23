package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

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
        validateAmount(amount, PointPolicy.CHARGE_UNIT, TransactionType.CHARGE.label());

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
    public UserPoint usePoint (long userId, long amount) {
        validateAmount(amount, PointPolicy.USE_UNIT, TransactionType.USE.label());

        // 1. 포인트 조회
        UserPoint current = userPointTable.selectById(userId);
        long newPoint = current.point() - amount;
        // 2. 잔액 부족 검증
        if(newPoint < 0) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다.");
        }
        // 3. 포인트 사용
        UserPoint updated = userPointTable.insertOrUpdate(userId, newPoint);
        // 4. 포인트 히스토리 등록
        pointHistoryTable.insert(userId, amount, TransactionType.USE, updated.updateMillis());

        return updated;
    }

    // 공통 검증 로직
    private void validateAmount(long amount, int unit, String type) {
        if (amount <= 0) {
            throw new IllegalArgumentException(type + " 금액은 0보다 커야합니다.");
        }
        if (amount % unit != 0) {
            throw new IllegalArgumentException(String.format("%s은 %d원 단위로만 가능합니다.", type, unit));
        }
    }
}
