package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.common.BusinessException;
import com.campus.common.UserContext;
import com.campus.config.RabbitMQConfig;
import com.campus.dto.ReservationMessage;
import com.campus.dto.ReservationVO;
import com.campus.entity.Activity;
import com.campus.entity.ActivityReservation;
import com.campus.mapper.ActivityMapper;
import com.campus.mapper.ActivityReservationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ActivityReservationMapper reservationMapper;
    private final ActivityMapper activityMapper;
    private final RabbitTemplate rabbitTemplate;

    public ReservationService(ActivityReservationMapper reservationMapper,
                              ActivityMapper activityMapper,
                              RabbitTemplate rabbitTemplate) {
        this.reservationMapper = reservationMapper;
        this.activityMapper = activityMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    /** 预约活动 */
    @Transactional
    public ReservationVO reserve(Long activityId) {
        Long userId = UserContext.getUserId();

        // 检查活动是否存在且上架
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("活动不存在或已下架");
        }

        // 条件扣减库存：available_quota > 0 才扣
        UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", activityId)
                .gt("available_quota", 0)
                .setSql("available_quota = available_quota - 1");
        int rows = activityMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("库存不足，预约失败");
        }

        // 生成预约码
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        // 插入预约记录
        ActivityReservation reservation = new ActivityReservation();
        reservation.setUserId(userId);
        reservation.setActivityId(activityId);
        reservation.setReservationCode(code);
        reservation.setStatus("RESERVED");
        reservation.setReservedAt(LocalDateTime.now());
        try {
            reservationMapper.insert(reservation);
        } catch (Exception e) {
            throw new BusinessException("您已预约过该活动，不可重复预约");
        }

        // 事务提交后异步发送通知消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ReservationMessage msg = new ReservationMessage();
                msg.setUserId(userId);
                msg.setActivityId(activityId);
                msg.setReservationId(reservation.getId());
                msg.setReservationCode(code);
                msg.setActivityTitle(activity.getTitle());
                msg.setReservedAt(LocalDateTime.now());

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.ROUTING_KEY,
                        msg
                );
                log.info("预约成功消息已发送: {}", code);
            }
        });

        ReservationVO vo = ReservationVO.from(reservation);
        vo.setActivityTitle(activity.getTitle());
        return vo;
    }

    /** 取消预约 */
    @Transactional
    public void cancel(Long reservationId) {
        Long userId = UserContext.getUserId();

        ActivityReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }

        // 更新预约状态
        reservation.setStatus("CANCELED");
        reservation.setCanceledAt(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        // 回滚库存
        UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", reservation.getActivityId())
                .setSql("available_quota = available_quota + 1");
        activityMapper.update(null, updateWrapper);
    }

    /** 查询我的预约列表 */
    public List<ReservationVO> myReservations() {
        Long userId = UserContext.getUserId();
        List<ActivityReservation> list = reservationMapper.selectList(
                new LambdaQueryWrapper<ActivityReservation>()
                        .eq(ActivityReservation::getUserId, userId)
                        .orderByDesc(ActivityReservation::getCreatedAt)
        );
        return list.stream().map(r -> {
            ReservationVO vo = ReservationVO.from(r);
            Activity activity = activityMapper.selectById(r.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }
            return vo;
        }).toList();
    }

    /** 查询预约详情 */
    public ReservationVO detail(Long reservationId) {
        ActivityReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        ReservationVO vo = ReservationVO.from(reservation);
        Activity activity = activityMapper.selectById(reservation.getActivityId());
        if (activity != null) {
            vo.setActivityTitle(activity.getTitle());
        }
        return vo;
    }
}
