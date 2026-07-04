package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.Violation;
import com.cloudmall.mapper.ViolationMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/violation")
public class ViolationController {

    @Resource
    private ViolationMapper violationMapper;

    /** 违规列表 */
    @GetMapping("/list")
    public R<List<Violation>> list() {
        return R.ok(violationMapper.selectList(
            new LambdaQueryWrapper<Violation>().orderByDesc(Violation::getCreateTime)));
    }

    /** 新增违规处罚 */
    @PostMapping
    public R<Void> create(@RequestBody Map<String, Object> body) {
        Violation v = new Violation();
        v.setMerchantId(Long.valueOf(body.get("merchantId").toString()));
        v.setType((Integer) body.get("type"));
        v.setReason((String) body.get("reason"));
        v.setPenaltyType((Integer) body.get("penaltyType"));
        v.setPenaltyAmount(body.get("penaltyAmount") != null ?
                (Integer) body.get("penaltyAmount") : 0);
        v.setStatus(0);
        v.setCreateTime(LocalDateTime.now());
        violationMapper.insert(v);
        return R.ok();
    }

    /** 更新处罚状态 */
    @PutMapping("/{id}")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Violation v = violationMapper.selectById(id);
        if (v != null) {
            v.setStatus((Integer) body.get("status"));
            violationMapper.updateById(v);
        }
        return R.ok();
    }
}
