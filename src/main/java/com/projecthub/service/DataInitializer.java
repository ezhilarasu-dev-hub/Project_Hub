package com.projecthub.service;

import com.projecthub.entity.*;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Seeding initial institutional users...");

            User faculty = new User(
                    "Dr. Robert Vance (Faculty)",
                    "faculty@institution.edu",
                    passwordEncoder.encode("faculty123"),
                    Role.FACULTY
            );
            userRepository.save(faculty);

            User student1 = new User(
                    "Alex Mercer (Student)",
                    "student@institution.edu",
                    passwordEncoder.encode("student123"),
                    Role.STUDENT
            );
            userRepository.save(student1);

            User student2 = new User(
                    "Sophia Lin (Student)",
                    "student2@institution.edu",
                    passwordEncoder.encode("student123"),
                    Role.STUDENT
            );
            userRepository.save(student2);

            // Seed initial student projects
            Project p1 = new Project();
            p1.setTitle("Automated Lab Equipment Booking");
            p1.setDescription("A web platform for students and faculty to schedule, monitor, and manage laboratory equipment reservations across departments.");
            p1.setStudent(student1);
            p1.setDeployedUrl("https://lab-booking.institution.edu");
            p1.setSourceCodeVisibility(ProjectVisibility.ALL_STUDENTS);
            p1.setDeployedLinkVisibility(ProjectVisibility.ALL_STUDENTS);
            p1.setStatus(ProjectStatus.APPROVED);
            p1.setCreatedAt(LocalDateTime.now().minusDays(10));
            p1.setUpdatedAt(LocalDateTime.now().minusDays(10));
            projectRepository.save(p1);

            Project p2 = new Project();
            p2.setTitle("Autonomous Drone Navigation in Indoor Arenas");
            p2.setDescription("Computer vision and sensor fusion based navigation system for small quadcopters operating in GPS-denied campus environments.");
            p2.setStudent(student1);
            p2.setSourceCodeVisibility(ProjectVisibility.FACULTY_ONLY);
            p2.setDeployedLinkVisibility(ProjectVisibility.FACULTY_ONLY);
            p2.setStatus(ProjectStatus.SUBMITTED);
            p2.setCreatedAt(LocalDateTime.now().minusDays(2));
            p2.setUpdatedAt(LocalDateTime.now().minusDays(2));
            projectRepository.save(p2);

            Project p3 = new Project();
            p3.setTitle("Smart Campus Attendance via BLE Beacons");
            p3.setDescription("Low-energy Bluetooth beacon mobile tracking client for automated lecture hall attendance logging.");
            p3.setStudent(student2);
            p3.setDeployedUrl("https://attendance.institution.edu");
            p3.setSourceCodeVisibility(ProjectVisibility.FACULTY_ONLY);
            p3.setDeployedLinkVisibility(ProjectVisibility.ALL_STUDENTS);
            p3.setStatus(ProjectStatus.SUBMITTED);
            p3.setCreatedAt(LocalDateTime.now().minusDays(1));
            p3.setUpdatedAt(LocalDateTime.now().minusDays(1));
            projectRepository.save(p3);

            logger.info("Initialized default accounts: faculty@institution.edu, student@institution.edu (password: faculty123 / student123)");
        }
    }
}
