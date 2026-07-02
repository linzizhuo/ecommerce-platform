package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.Venue;
import com.cloudmall.mapper.VenueMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Resource private VenueMapper venueMapper;

    /** 会场列表 */
    @GetMapping
    public R<List<Venue>> list() {
        return R.ok(venueMapper.selectList(
            new LambdaQueryWrapper<Venue>().orderByDesc(Venue::getCreateTime)));
    }

    /** C端：查看已发布的会场 */
    @GetMapping("/published")
    public R<List<Venue>> published() {
        return R.ok(venueMapper.selectList(
            new LambdaQueryWrapper<Venue>()
                .eq(Venue::getStatus, 1)
                .orderByDesc(Venue::getCreateTime)));
    }

    /** 会场详情 */
    @GetMapping("/{id}")
    public R<Venue> detail(@PathVariable Long id) {
        return R.ok(venueMapper.selectById(id));
    }

    /** 创建会场 */
    @PostMapping
    public R<Void> create(@RequestBody Venue venue) {
        venue.setStatus(0);
        venue.setCreateTime(LocalDateTime.now());
        venueMapper.insert(venue);
        return R.ok();
    }

    /** 更新会场 */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Venue venue) {
        venue.setId(id);
        venueMapper.updateById(venue);
        return R.ok();
    }

    /** 发布/下线 */
    @PutMapping("/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Venue v = venueMapper.selectById(id);
        if (v != null) {
            v.setStatus((Integer) body.get("status"));
            venueMapper.updateById(v);
        }
        return R.ok();
    }

    /** 删除会场 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        venueMapper.deleteById(id);
        return R.ok();
    }
}
