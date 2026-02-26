package com.project2.util;

import com.project2.model.Role;
import com.project2.model.User;
import com.project2.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 45) {
            return; // Already fully seeded
        }

        // Create Admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@talentrock.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setFullName("Platform Admin");
            userRepository.save(admin);
        }

        // Create Sample Client if not exists
        if (!userRepository.existsByUsername("client")) {
            User client = new User();
            client.setUsername("client");
            client.setEmail("client@gmail.com");
            client.setPassword(passwordEncoder.encode("password123"));
            client.setRole(Role.CLIENT);
            client.setFullName("John Client");
            client.setWalletBalance(new BigDecimal("100000"));
            userRepository.save(client);
        }

        List<String> categories = Arrays.asList(
                "Website Design", "Mobile Apps", "Android Apps", "iPhone Apps",
                "Software Architecture", "Graphic Design", "Logo Design",
                "UI/UX Design", "Data Science & ML", "Digital Marketing",
                "Content Writing", "Video Editing", "SEO & Analytics", "Backend Development");

        List<String> firstNames = Arrays.asList("Arjun", "Neha", "Rahul", "Priya", "Amit", "Sonal", "Vikram", "Anjali",
                "Karan", "Meera");
        List<String> lastNames = Arrays.asList("Sharma", "Verma", "Gupta", "Malhotra", "Singh", "Joshi", "Mehta",
                "Reddy", "Patel", "Nair");

        Random random = new Random();

        for (int i = 1; i <= 40; i++) {
            User freelancer = new User();
            String firstName = firstNames.get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));
            String username = (firstName + lastName + i).toLowerCase();

            freelancer.setUsername(username);
            freelancer.setEmail(username + "@talentrock.dev");
            freelancer.setPassword(passwordEncoder.encode("pass123"));
            freelancer.setRole(Role.FREELANCER);
            freelancer.setFullName(firstName + " " + lastName);

            String cat = categories.get(random.nextInt(categories.size()));
            freelancer.setCategory(cat);
            freelancer.setSkills(cat + ", Freelancing, Expert");
            freelancer.setLocation("India");
            freelancer.setBio("I am a professional freelancer specializing in " + cat
                    + ". With over 5 years of experience, I deliver high-quality results.");
            freelancer.setHourlyRate(new BigDecimal(15 + random.nextInt(85))); // $15 - $100
            freelancer.setPerformanceScore(4.0 + (random.nextDouble() * 1.0)); // 4.0 - 5.0
            freelancer.setCompletedProjects(5 + random.nextInt(50));
            freelancer.setIsVerified(random.nextBoolean());

            userRepository.save(freelancer);
        }

        System.out.println(">>> 40+ Sample Freelancers Seeded Successfully!");
    }
}
