package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
