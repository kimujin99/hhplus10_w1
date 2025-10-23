package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointService 동시성 테스트")
class PointConcurrencyTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("동시에 여러 스레드가 포인트 충전 - 모든 충전이 반영되어야 함")
    void chargePoint_ConcurrentRequests() throws InterruptedException {
        // given
        long userId = 1L;
        int threadCount = 10;
        long chargeAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        // 10개 스레드 동시 작업 실행
        for(int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 완료될 때까지 대기
        latch.await();

        // then
        // 최종 충전된 포인트 검증
        UserPoint result = pointService.getUserPoint(userId);
        assertThat(result.point()).isEqualTo(10000L);

        executorService.shutdown();
    }

    @Test
    @DisplayName("동시에 충전과 사용이 발생 - 최종 잔액이 정확해야 함")
    void chargeAndUsePoint_ConcurrentRequests() throws InterruptedException {
        // given
        long userId = 1L;
        long initialPoint = 10000L;
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // 초기 포인트 설정
        userPointTable.insertOrUpdate(userId, initialPoint);

        // when
        // 10개 스레드는 충전(+1000), 10개 스레드는 사용(-500)
        for(int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    if (index < 10) {
                        pointService.chargePoint(userId, 1000L);
                    } else {
                        pointService.usePoint(userId, 500L);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // 최종 포인트 = 1000 + (100 * 10) - (50 * 10) = 1500
        UserPoint result = pointService.getUserPoint(userId);
        assertThat(result.point()).isEqualTo(15000L);

        executorService.shutdown();
    }

    @Test
    @DisplayName("동시에 여러 사용자의 포인트 충전 - 각 사용자별로 정확해야 함")
    void chargePoint_MultipleUsers_ConcurrentRequests() throws InterruptedException {
        // given
        int userCount = 5;
        int threadPerUser = 10;
        long chargeAmount = 100L;
        ExecutorService executorService = Executors.newFixedThreadPool(userCount * threadPerUser);
        CountDownLatch latch = new CountDownLatch(userCount * threadPerUser);

        // when
        // TODO: PointService 구현 후 동시성 테스트 작성
        // for (long userId = 1; userId <= userCount; userId++) {
        //     final long currentUserId = userId;
        //     for (int i = 0; i < threadPerUser; i++) {
        //         executorService.submit(() -> {
        //             try {
        //                 pointService.chargePoint(currentUserId, chargeAmount);
        //             } finally {
        //                 latch.countDown();
        //             }
        //         });
        //     }
        // }
        // latch.await();

        // then
        // TODO: 각 사용자의 최종 포인트가 1000 (100 * 10)이어야 함
        // for (long userId = 1; userId <= userCount; userId++) {
        //     UserPoint result = pointService.getUserPoint(userId);
        //     assertThat(result.point()).isEqualTo(1000L);
        // }

        executorService.shutdown();
    }
}
