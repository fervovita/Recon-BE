package com.project.recon.domain.order.entity;

public enum OrderStatus {
    PENDING,    // 결제 대기
    CONFIRMED,  // 결제 완료
    CANCELLED,  // 주문 취소
}
