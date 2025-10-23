package io.hhplus.tdd.point;


/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
public enum TransactionType {
    CHARGE("충전"),
    USE("사용");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}