package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.common.UserContext;
import com.campus.dto.ActivityCreateRequest;
import com.campus.dto.ActivityVO;
import com.campus.entity.Activity;
import com.campus.entity.ActivityCategory;
import com.campus.mapper.ActivityCategoryMapper;
import com.campus.mapper.ActivityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);
    private static final String CACHE_PREFIX = "campus:activity:";
    private static final String NULL_PREFIX = "campus:activity:null:";
    private static final Duration BASE_TTL = Duration.ofMinutes(30);
    private static final int MAX_JITTER_SECONDS = 5 * 60;
    private static final Duration NULL_TTL = Duration.ofMinutes(5);

    private final ActivityMapper activityMapper;
    private final ActivityCategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ActivityService(ActivityMapper activityMapper,
                           ActivityCategoryMapper categoryMapper,
                           RedisTemplate<String, Object> redisTemplate) {
        this.activityMapper = activityMapper;
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }

    /** 分页查询活动列表（仅上架的） */
    public Page<ActivityVO> pageActive(int page, int size) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 1)
                .orderByDesc(Activity::getCreatedAt);

        long total = activityMapper.selectCount(wrapper);

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + "," + size);
        var records = activityMapper.selectList(wrapper).stream()
                .map(this::toVO).toList();

        Page<ActivityVO> voPage = new Page<>(page, size, total);
        voPage.setRecords(records);
        return voPage;
    }

    /** 查询活动详情（Redis Cache-Aside） */
    public ActivityVO detail(Long id) {
        String cacheKey = CACHE_PREFIX + id;
        String nullKey = NULL_PREFIX + id;

        // 1. 先查空值缓存 → 防穿透
        Boolean hasNull = redisTemplate.hasKey(nullKey);
        if (Boolean.TRUE.equals(hasNull)) {
            log.info("Redis 空值缓存命中: {}", nullKey);
            throw new BusinessException("活动不存在");
        }

        // 2. 查正常缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof ActivityVO) {
            log.info("Redis 缓存命中: {}", cacheKey);
            return (ActivityVO) cached;
        }

        // 3. 缓存未命中 → 查 MySQL
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            // 写入空值缓存，防穿透
            log.info("写入 Redis 空值缓存: {}", nullKey);
            redisTemplate.opsForValue().set(nullKey, "", NULL_TTL);
            throw new BusinessException("活动不存在");
        }

        // 4. 写入缓存，TTL = 基础 30min + 随机 0~5min
        ActivityVO vo = toVO(activity);
        Duration ttl = BASE_TTL.plusSeconds(
                ThreadLocalRandom.current().nextInt(MAX_JITTER_SECONDS));
        redisTemplate.opsForValue().set(cacheKey, vo, ttl);
        log.info("写入 Redis 缓存: {}, TTL={}s", cacheKey, ttl.getSeconds());
        return vo;
    }

    /** 管理员创建活动 */
    public ActivityVO create(ActivityCreateRequest req) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        Activity activity = new Activity();
        activity.setCategoryId(req.getCategoryId());
        activity.setTitle(req.getTitle());
        activity.setDescription(req.getDescription());
        activity.setLocation(req.getLocation());
        activity.setTotalQuota(req.getTotalQuota());
        activity.setAvailableQuota(req.getTotalQuota());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setReservationStartTime(req.getReservationStartTime());
        activity.setReservationEndTime(req.getReservationEndTime());
        activity.setStatus(1);
        activityMapper.insert(activity);
        return toVO(activity);
    }

    /** 管理员修改活动 */
    public ActivityVO update(Long id, ActivityCreateRequest req) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activity.setCategoryId(req.getCategoryId());
        activity.setTitle(req.getTitle());
        activity.setDescription(req.getDescription());
        activity.setLocation(req.getLocation());
        activity.setTotalQuota(req.getTotalQuota());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setReservationStartTime(req.getReservationStartTime());
        activity.setReservationEndTime(req.getReservationEndTime());
        activityMapper.updateById(activity);

        evictCache(id);
        return toVO(activity);
    }

    /** 管理员上下架活动 */
    public void updateStatus(Long id, Integer status) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activity.setStatus(status);
        activityMapper.updateById(activity);

        evictCache(id);
    }

    /** 清除缓存 */
    private void evictCache(Long activityId) {
        redisTemplate.delete(CACHE_PREFIX + activityId);
        redisTemplate.delete(NULL_PREFIX + activityId);
        log.info("清除 Redis 缓存: activity:{}", activityId);
    }

    private ActivityVO toVO(Activity a) {
        ActivityVO vo = ActivityVO.from(a);
        ActivityCategory category = categoryMapper.selectById(a.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }
}
