package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;

    public HomeController(UserService userService, ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("openProjects", projectService.countOpenProjects());
        model.addAttribute("totalProjects", projectService.countTotalProjects());
        model.addAttribute("totalFreelancers", userService.countByRole(Role.FREELANCER));
        return "home";
    }

    @GetMapping("/hire/{category}")
    public String hireCategory(@PathVariable String category, Model model) {
        // Replace dashes with spaces for better matching if needed
        String displayCategory = category.replace("-", " ");
        List<User> freelancers = userService.findFreelancersByCategory(displayCategory);

        model.addAttribute("category", displayCategory);
        model.addAttribute("freelancers", freelancers);
        return "hire-category";
    }

    @GetMapping("/profile/{id}")
    public String publicProfile(@PathVariable Long id, Model model) {
        User freelancer = userService.findById(id).orElseThrow(() -> new RuntimeException("Profile not found"));
        model.addAttribute("freelancer", freelancer);
        return "public-profile";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null)
            return "redirect:/login";

        model.addAttribute("user", user);
        if (user.getRole() == Role.ADMIN)
            return "redirect:/admin/dashboard";
        if (user.getRole() == Role.CLIENT)
            return "redirect:/client/dashboard";
        if (user.getRole() == Role.FREELANCER)
            return "redirect:/freelancer/dashboard";
        return "redirect:/";
    }
}
