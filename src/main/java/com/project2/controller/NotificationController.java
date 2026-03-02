package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // -----------------------------------------------
    // GET UNREAD COUNT (for bell badge polling)
    // -----------------------------------------------
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unreadCount(Authentication auth) {
        if (auth == null)
            return ResponseEntity.ok(Map.of("count", 0));
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null)
            return ResponseEntity.ok(Map.of("count", 0));
        long count = notificationService.countUnread(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // -----------------------------------------------
    // GET ALL NOTIFICATIONS (dropdown JSON)
    // -----------------------------------------------
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> list(Authentication auth) {
        if (auth == null)
            return ResponseEntity.ok(Collections.emptyList());
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null)
            return ResponseEntity.ok(Collections.emptyList());

        List<Notification> notifs = notificationService.getForUser(user);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification n : notifs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", n.getId());
            m.put("title", n.getTitle());
            m.put("message", n.getMessage());
            m.put("type", n.getType());
            m.put("link", n.getLinkUrl() != null ? n.getLinkUrl() : "#");
            m.put("isRead", n.getIsRead());
            m.put("time", n.getCreatedAt() != null ? n.getCreatedAt().format(fmt) : "");
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // -----------------------------------------------
    // MARK ALL READ
    // -----------------------------------------------
    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllRead(Authentication auth) {
        if (auth != null) {
            userService.findByUsername(auth.getName())
                    .ifPresent(notificationService::markAllRead);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    // -----------------------------------------------
    // MARK SINGLE READ
    // -----------------------------------------------
    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id, Authentication auth) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
