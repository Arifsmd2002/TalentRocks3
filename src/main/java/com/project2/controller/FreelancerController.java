package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/freelancer")
public class FreelancerController {

    private final UserService userService;
    private final ProjectService projectService;
    private final BidService bidService;
    private final MilestoneService milestoneService;

    public FreelancerController(UserService userService, ProjectService projectService, BidService bidService,
            MilestoneService milestoneService) {
        this.userService = userService;
        this.projectService = projectService;
        this.bidService = bidService;
        this.milestoneService = milestoneService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        List<Bid> myBids = bidService.findByFreelancer(freelancer);
        List<Project> activeProjects = projectService.findByFreelancer(freelancer);

        model.addAttribute("user", freelancer);
        model.addAttribute("myBids", myBids);
        model.addAttribute("activeProjects", activeProjects);
        model.addAttribute("pendingBids", myBids.stream().filter(b -> b.getStatus() == BidStatus.PENDING).count());
        model.addAttribute("acceptedBids", myBids.stream().filter(b -> b.getStatus() == BidStatus.ACCEPTED).count());
        return "freelancer/dashboard";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String keyword, Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        model.addAttribute("projects", projectService.searchProjects(keyword));
        model.addAttribute("keyword", keyword);
        return "freelancer/browse";
    }

    @GetMapping("/project/{id}")
    public String viewProject(@PathVariable Long id, Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        Project project = projectService.findById(id).orElseThrow();
        boolean alreadyBid = bidService.findByProject(project)
                .stream().anyMatch(b -> b.getFreelancer().getId().equals(freelancer.getId()));

        model.addAttribute("user", freelancer);
        model.addAttribute("project", project);
        model.addAttribute("alreadyBid", alreadyBid);
        model.addAttribute("bid", new Bid());
        return "freelancer/project-detail";
    }

    @PostMapping("/project/{id}/bid")
    public String submitBid(@PathVariable Long id, @ModelAttribute Bid bid,
            Authentication auth, RedirectAttributes redirectAttrs) {
        User freelancer = getCurrentUser(auth);
        Project project = projectService.findById(id).orElseThrow();
        bid.setFreelancer(freelancer);
        bid.setProject(project);
        try {
            bidService.submitBid(bid);
            redirectAttrs.addFlashAttribute("success", "Bid submitted successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/freelancer/browse";
    }

    @GetMapping("/active-project/{id}")
    public String activeProject(@PathVariable Long id, Authentication auth, Model model) {
        User freelancer = getCurrentUser(auth);
        Project project = projectService.findById(id).orElseThrow();
        model.addAttribute("user", freelancer);
        model.addAttribute("project", project);
        model.addAttribute("milestones", milestoneService.findByProject(project));
        return "freelancer/active-project";
    }

    @PostMapping("/milestone/{id}/submit")
    public String submitMilestone(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Milestone m = milestoneService.findById(id).orElseThrow();
        milestoneService.submitMilestone(id);
        redirectAttrs.addFlashAttribute("success", "Milestone submitted for review!");
        return "redirect:/freelancer/active-project/" + m.getProject().getId();
    }

    @GetMapping("/wallet")
    public String wallet(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        return "freelancer/wallet";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        return "freelancer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, Authentication auth,
            RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(auth);
        user.setFullName(updatedUser.getFullName());
        user.setBio(updatedUser.getBio());
        user.setSkills(updatedUser.getSkills());
        user.setLocation(updatedUser.getLocation());
        user.setPhone(updatedUser.getPhone());
        userService.save(user);
        redirectAttrs.addFlashAttribute("success", "Profile updated!");
        return "redirect:/freelancer/profile";
    }
}
