package com.campus.dto;

import com.campus.entity.ActivityReservation;

import java.time.LocalDateTime;

public class ReservationVO {

    private Long id;
    private Long userId;
    private Long activityId;
    private String activityTitle;
    private String reservationCode;
    private String status;
    private LocalDateTime reservedAt;
    private LocalDateTime canceledAt;

    public static ReservationVO from(ActivityReservation r) {
        ReservationVO vo = new ReservationVO();
        vo.id = r.getId();
        vo.userId = r.getUserId();
        vo.activityId = r.getActivityId();
        vo.reservationCode = r.getReservationCode();
        vo.status = r.getStatus();
        vo.reservedAt = r.getReservedAt();
        vo.canceledAt = r.getCanceledAt();
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
    public String getReservationCode() { return reservationCode; }
    public void setReservationCode(String reservationCode) { this.reservationCode = reservationCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(LocalDateTime canceledAt) { this.canceledAt = canceledAt; }
}
