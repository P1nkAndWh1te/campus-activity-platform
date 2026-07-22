package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.ReservationVO;
import com.campus.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** 预约活动 */
    @PostMapping("/api/activities/{id}/reservations")
    public Result<ReservationVO> reserve(@PathVariable Long id) {
        return Result.ok(reservationService.reserve(id));
    }

    /** 取消预约 */
    @DeleteMapping("/api/reservations/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return Result.ok();
    }

    /** 我的预约列表 */
    @GetMapping("/api/users/me/reservations")
    public Result<List<ReservationVO>> myReservations() {
        return Result.ok(reservationService.myReservations());
    }

    /** 预约详情 */
    @GetMapping("/api/reservations/{id}")
    public Result<ReservationVO> detail(@PathVariable Long id) {
        return Result.ok(reservationService.detail(id));
    }
}
