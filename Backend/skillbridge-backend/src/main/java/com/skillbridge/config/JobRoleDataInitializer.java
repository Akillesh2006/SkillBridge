package com.skillbridge.config;

import com.skillbridge.entity.JobRole;
import com.skillbridge.repository.JobRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobRoleDataInitializer {

    @Bean
    CommandLineRunner initializeJobRoles(JobRoleRepository repository) {

        return args -> {

            createRole(
                    repository,
                    "Java Backend Developer",
                    "Java,Spring Boot,SQL,REST APIs,Git,JPA,Hibernate"
            );

            createRole(
                    repository,
                    "Android Developer",
                    "Android,Kotlin,Java,Jetpack Compose,Room,Coroutines,Retrofit,MVVM"
            );

            createRole(
                    repository,
                    "Frontend Developer",
                    "HTML,CSS,JavaScript,React,Git,REST APIs"
            );

            createRole(
                    repository,
                    "Full Stack Developer",
                    "HTML,CSS,JavaScript,React,Node.js,Java,SQL,REST APIs,Git"
            );

            createRole(
                    repository,
                    "Data Analyst",
                    "Python,Pandas,NumPy,SQL,Excel,Statistics,Data Visualization"
            );

            createRole(
                    repository,
                    "Data Scientist",
                    "Python,Pandas,NumPy,SQL,Statistics,Machine Learning,Data Visualization,Scikit-learn"
            );

            createRole(
                    repository,
                    "Python Developer",
                    "Python,Flask,Django,SQL,REST APIs,Git"
            );

            createRole(
                    repository,
                    "Machine Learning Engineer",
                    "Python,Machine Learning,NumPy,Pandas,TensorFlow,SQL,Git"
            );
        };
    }

    private void createRole(
            JobRoleRepository repository,
            String roleName,
            String requiredSkills
    ) {

        if (repository.findByRoleNameIgnoreCase(roleName).isEmpty()) {

            JobRole role = new JobRole();

            role.setRoleName(roleName);
            role.setRequiredSkills(requiredSkills);

            repository.save(role);
        }
    }
}