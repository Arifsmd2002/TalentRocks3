package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;

    public AdminController(UserService userService, ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalClients", userService.countByRole(Role.CLIENT));
        model.addAttribute("totalFreelancers", userService.countByRole(Role.FREELANCER));
        model.addAttribute("totalProjects", projectService.countTotalProjects());
        model.addAttribute("openProjects", projectService.countOpenProjects());
        model.addAttribute("recentProjects", projectService.findAllProjects().stream().limit(5).toList());
        model.addAttribute("recentUsers", userService.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Authentication auth, Model model) {
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.toggleUserStatus(id);
        redirectAttrs.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/projects")
    public String projects(Authentication auth, Model model) {
        model.addAttribute("user", userService.findByUsername(auth.getName()).orElseThrow());
        model.addAttribute("projects", projectService.findAllProjects());
        return "admin/projects";
    }
}
