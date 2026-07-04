package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.Notice;
import com.cloudmall.entity.SystemConfig;
import com.cloudmall.service.impl.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NoticeController {
    @Resource private ConfigService configService;

    /** 前台获取有效公告 */
    @GetMapping("/notice/active")
    public R activeNotices() {
        return R.ok(configService.activeNotices());
    }

    /** 后台公告列表 */
    @GetMapping("/admin/notice/list")
    public R listNotices() {
        return R.ok(configService.listNotices());
    }

    /** 后台保存公告 */
    @PostMapping("/admin/notice")
    public R saveNotice(@RequestBody Notice notice) {
        configService.saveNotice(notice);
        return R.ok();
    }

    /** 后台删除公告 */
    @DeleteMapping("/admin/notice/{id}")
    public R deleteNotice(@PathVariable Long id) {
        configService.deleteNotice(id);
        return R.ok();
    }

    /** 后台系统配置列表 */
    @GetMapping("/admin/config/list")
    public R allConfigs() {
        List<SystemConfig> list = configService.allConfigs();
        return R.ok(list);
    }

    /** 后台保存系统配置 */
    @PostMapping("/admin/config")
    public R saveConfig(@RequestBody SystemConfig config) {
        configService.saveConfig(config);
        return R.ok();
    }

    /** 根据key获取配置值 */
    @GetMapping("/config/{key}")
    public R getConfig(@PathVariable String key) {
        return R.ok(configService.getValue(key));
    }
}
