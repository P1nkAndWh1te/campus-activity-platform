package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.ActivityCategory;
import com.campus.mapper.ActivityCategoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-categories")
public class ActivityCategoryController {

    private final ActivityCategoryMapper categoryMapper;

    public ActivityCategoryController(ActivityCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public Result<List<ActivityCategory>> list() {
        List<ActivityCategory> list = categoryMapper.selectList(null);
        return Result.ok(list);
    }
}
