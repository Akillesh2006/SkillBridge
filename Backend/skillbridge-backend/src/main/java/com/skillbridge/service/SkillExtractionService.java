package com.skillbridge.service;

import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import com.skillbridge.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SkillExtractionService {

    private final UserSkillRepository userSkillRepository;

    public SkillExtractionService(UserSkillRepository userSkillRepository) {
        this.userSkillRepository = userSkillRepository;
    }

    private static final Map<String, List<String>> SKILL_LIBRARY = Map.of(
            "Programming Languages",
            List.of(
                    "C",
                    "C++",
                    "Java",
                    "Python",
                    "Kotlin",
                    "JavaScript"
            ),

            "Web Technologies",
            List.of(
                    "HTML",
                    "CSS",
                    "JavaScript",
                    "React",
                    "Angular",
                    "Node.js"
            ),

            "Database",
            List.of(
                    "MySQL",
                    "PostgreSQL",
                    "SQLite",
                    "MongoDB"
            ),

            "Frameworks",
            List.of(
                    "Spring Boot",
                    "Django",
                    "Flask",
                    "Android"
            ),

            "Tools",
            List.of(
                    "Git",
                    "GitHub",
                    "Postman",
                    "VS Code"
            ),

            "Computer Science",
            List.of(
                    "Data Structures",
                    "Algorithms",
                    "OOP",
                    "DBMS",
                    "Operating Systems",
                    "OS",
                    "Computer Networks",
                    "REST APIs"
            ),

            "AI / Machine Learning",
            List.of(
                    "Machine Learning",
                    "Deep Learning",
                    "Artificial Intelligence",
                    "Pandas",
                    "NumPy",
                    "TensorFlow"
            )
    );

    @Transactional
    public List<UserSkill> extractAndSaveSkills(User user, String resumeText) {

        if (resumeText == null || resumeText.isBlank()) {
            return Collections.emptyList();
        }

        // Remove previous extracted skills for this user.
        userSkillRepository.deleteByUser(user);

        List<UserSkill> extractedSkills = new ArrayList<>();

        String normalizedText = resumeText.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, List<String>> categoryEntry : SKILL_LIBRARY.entrySet()) {

            String category = categoryEntry.getKey();

            for (String skill : categoryEntry.getValue()) {

                if (containsSkill(normalizedText, skill)) {

                    UserSkill userSkill = new UserSkill();

                    userSkill.setSkillName(skill);
                    userSkill.setCategory(category);
                    userSkill.setProficiency("Detected");
                    userSkill.setUser(user);

                    extractedSkills.add(userSkill);
                }
            }
        }

        return userSkillRepository.saveAll(extractedSkills);
    }

    private boolean containsSkill(String text, String skill) {

        String normalizedSkill = skill.toLowerCase(Locale.ROOT);

        if (normalizedSkill.equals("c") || normalizedSkill.equals("c++")) {
            return Pattern.compile(
                    "(^|[^a-z0-9+#])" +
                            Pattern.quote(normalizedSkill) +
                            "([^a-z0-9+#]|$)"
            ).matcher(text).find();
        }

        return text.contains(normalizedSkill);
    }
}