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

    // 포인트 단위 설정(정책)
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
        if(amount % CHARGE_UNIT != 0) {
            throw new IllegalArgumentException(String.format("충전은 %s원 단위로만 가능합니다.", CHARGE_UNIT));
        }
        return null;
    }

    /**
     * 포인트 이용
     */
    public UserPoint usePoint (String userId, long amount) {
        if(amount % USE_UNIT != 0) {
            throw new IllegalArgumentException(String.format("사용은 %s원 단위로만 가능합니다.", USE_UNIT));
        }
        return null;
    }
}
