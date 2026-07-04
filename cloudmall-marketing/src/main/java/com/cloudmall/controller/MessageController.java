package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.MessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Resource private MessageService messageService;

    @GetMapping("/list")
    public R list(@RequestParam(required = false) Integer isRead, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return R.ok(messageService.userMessages(userId, isRead));
    }

    @PutMapping("/read/{id}")
    public R read(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        messageService.markRead(id, userId);
        return R.ok();
    }

    @PutMapping("/readAll")
    public R readAll(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        messageService.markAllRead(userId);
        return R.ok();
    }

    @GetMapping("/unreadCount")
    public R unreadCount(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return R.ok(messageService.unreadCount(userId));
    }
}
