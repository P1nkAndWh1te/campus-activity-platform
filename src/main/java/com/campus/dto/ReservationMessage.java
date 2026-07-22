package com.campus.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReservationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long activityId;
    private Long reservationId;
    private String reservationCode;
    private String activityTitle;
    private LocalDateTime reservedAt;

    public ReservationMessage() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getReservationCode() { return reservationCode; }
    public void setReservationCode(String reservationCode) { this.reservationCode = reservationCode; }

    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
}
