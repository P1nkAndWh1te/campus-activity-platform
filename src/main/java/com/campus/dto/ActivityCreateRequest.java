package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ActivityCreateRequest {

    @NotNull(message = "活动分类不能为空")
    private Long categoryId;

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "活动地点不能为空")
    private String location;

    @NotNull(message = "总名额不能为空")
    private Integer totalQuota;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "预约开始时间不能为空")
    private LocalDateTime reservationStartTime;

    @NotNull(message = "预约结束时间不能为空")
    private LocalDateTime reservationEndTime;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getTotalQuota() { return totalQuota; }
    public void setTotalQuota(Integer totalQuota) { this.totalQuota = totalQuota; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getReservationStartTime() { return reservationStartTime; }
    public void setReservationStartTime(LocalDateTime reservationStartTime) { this.reservationStartTime = reservationStartTime; }
    public LocalDateTime getReservationEndTime() { return reservationEndTime; }
    public void setReservationEndTime(LocalDateTime reservationEndTime) { this.reservationEndTime = reservationEndTime; }
}
