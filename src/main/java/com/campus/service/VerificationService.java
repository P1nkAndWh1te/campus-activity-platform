package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.common.UserContext;
import com.campus.dto.ReservationVO;
import com.campus.entity.Activity;
import com.campus.entity.ActivityReservation;
import com.campus.entity.VerificationRecord;
import com.campus.mapper.ActivityMapper;
import com.campus.mapper.ActivityReservationMapper;
import com.campus.mapper.VerificationRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final ActivityReservationMapper reservationMapper;
    private final VerificationRecordMapper verificationRecordMapper;
    private final ActivityMapper activityMapper;

    public VerificationService(ActivityReservationMapper reservationMapper,
                               VerificationRecordMapper verificationRecordMapper,
                               ActivityMapper activityMapper) {
        this.reservationMapper = reservationMapper;
        this.verificationRecordMapper = verificationRecordMapper;
        this.activityMapper = activityMapper;
    }

    /** 根据预约码查询预约 */
    public ReservationVO findByCode(String code) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        ActivityReservation reservation = reservationMapper.selectOne(
                new LambdaQueryWrapper<ActivityReservation>()
                        .eq(ActivityReservation::getReservationCode, code)
        );
        if (reservation == null) {
            throw new BusinessException("预约码无效，未找到对应预约");
        }
        ReservationVO vo = ReservationVO.from(reservation);
        Activity activity = activityMapper.selectById(reservation.getActivityId());
        if (activity != null) {
            vo.setActivityTitle(activity.getTitle());
        }
        return vo;
    }

    /** 核销预约 */
    @Transactional
    public void verify(String code) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }

        ActivityReservation reservation = reservationMapper.selectOne(
                new LambdaQueryWrapper<ActivityReservation>()
                        .eq(ActivityReservation::getReservationCode, code)
        );
        if (reservation == null) {
            throw new BusinessException("预约码无效");
        }
        if ("CANCELED".equals(reservation.getStatus())) {
            throw new BusinessException("该预约已取消，无法核销");
        }
        if ("VERIFIED".equals(reservation.getStatus())) {
            throw new BusinessException("该预约已核销，不可重复核销");
        }

        // 更新预约状态
        reservation.setStatus("VERIFIED");
        reservation.setVerifiedAt(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        // 写入核销记录
        VerificationRecord record = new VerificationRecord();
        record.setReservationId(reservation.getId());
        record.setReservationCode(reservation.getReservationCode());
        record.setActivityId(reservation.getActivityId());
        record.setUserId(reservation.getUserId());
        record.setOperatorId(UserContext.getUserId());
        record.setVerifiedAt(LocalDateTime.now());
        verificationRecordMapper.insert(record);
    }

    /** 查询核销记录列表 */
    public List<VerificationRecord> listVerifications() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        return verificationRecordMapper.selectList(
                new LambdaQueryWrapper<VerificationRecord>()
                        .orderByDesc(VerificationRecord::getCreatedAt)
        );
    }
}
