package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.ReservationVO;
import com.campus.entity.VerificationRecord;
import com.campus.service.VerificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminVerificationController {

    private final VerificationService verificationService;

    public AdminVerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /** 根据预约码查询预约 */
    @GetMapping("/reservations/by-code/{code}")
    public Result<ReservationVO> findByCode(@PathVariable String code) {
        return Result.ok(verificationService.findByCode(code));
    }

    /** 核销预约 */
    @PostMapping("/verifications")
    public Result<?> verify(@RequestBody Map<String, String> body) {
        String code = body.get("reservationCode");
        if (code == null || code.isBlank()) {
            return Result.fail("reservationCode 不能为空");
        }
        verificationService.verify(code);
        return Result.ok();
    }

    /** 核销记录列表 */
    @GetMapping("/verifications")
    public Result<List<VerificationRecord>> listVerifications() {
        return Result.ok(verificationService.listVerifications());
    }
}
