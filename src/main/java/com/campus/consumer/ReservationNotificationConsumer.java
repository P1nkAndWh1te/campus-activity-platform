package com.campus.consumer;

import com.campus.dto.ReservationMessage;
import com.campus.entity.Notification;
import com.campus.mapper.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationNotificationConsumer.class);

    private final NotificationMapper notificationMapper;

    public ReservationNotificationConsumer(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @RabbitListener(queues = "campus.reservation.notify")
    public void handleReservationSuccess(ReservationMessage msg) {
        log.info("收到预约成功消息: userId={}, activity={}, code={}",
                msg.getUserId(), msg.getActivityTitle(), msg.getReservationCode());

        // 模拟发送通知：写入通知记录
        Notification notification = new Notification();
        notification.setUserId(msg.getUserId());
        notification.setReservationId(msg.getReservationId());
        notification.setActivityTitle(msg.getActivityTitle());
        notification.setMessage("您已成功预约活动「" + msg.getActivityTitle() + "」，预约码：" + msg.getReservationCode());
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);

        log.info("通知记录已写入: userId={}, reservationId={}", msg.getUserId(), msg.getReservationId());
    }
}
