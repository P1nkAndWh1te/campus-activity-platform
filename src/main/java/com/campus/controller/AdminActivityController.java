package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.ActivityCreateRequest;
import com.campus.dto.ActivityVO;
import com.campus.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final ActivityService activityService;

    public AdminActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public Result<ActivityVO> create(@Valid @RequestBody ActivityCreateRequest req) {
        return Result.ok(activityService.create(req));
    }

    @PutMapping("/{id}")
    public Result<ActivityVO> update(@PathVariable Long id, @Valid @RequestBody ActivityCreateRequest req) {
        return Result.ok(activityService.update(id, req));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail("status 必须为 0 或 1");
        }
        activityService.updateStatus(id, status);
        return Result.ok();
    }
}
