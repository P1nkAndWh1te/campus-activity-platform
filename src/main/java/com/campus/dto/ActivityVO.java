package com.campus.dto;

import com.campus.entity.Activity;

import java.time.LocalDateTime;

public class ActivityVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String location;
    private Integer totalQuota;
    private Integer availableQuota;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime reservationStartTime;
    private LocalDateTime reservationEndTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivityVO from(Activity a) {
        ActivityVO vo = new ActivityVO();
        vo.setId(a.getId());
        vo.setCategoryId(a.getCategoryId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setLocation(a.getLocation());
        vo.setTotalQuota(a.getTotalQuota());
        vo.setAvailableQuota(a.getAvailableQuota());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());
        vo.setReservationStartTime(a.getReservationStartTime());
        vo.setReservationEndTime(a.getReservationEndTime());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getTotalQuota() { return totalQuota; }
    public void setTotalQuota(Integer totalQuota) { this.totalQuota = totalQuota; }
    public Integer getAvailableQuota() { return availableQuota; }
    public void setAvailableQuota(Integer availableQuota) { this.availableQuota = availableQuota; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getReservationStartTime() { return reservationStartTime; }
    public void setReservationStartTime(LocalDateTime reservationStartTime) { this.reservationStartTime = reservationStartTime; }
    public LocalDateTime getReservationEndTime() { return reservationEndTime; }
    public void setReservationEndTime(LocalDateTime reservationEndTime) { this.reservationEndTime = reservationEndTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
