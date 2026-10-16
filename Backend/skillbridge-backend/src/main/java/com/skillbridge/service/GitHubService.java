package com.skillbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubService {

    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("^(?:https?:\\/\\/)?(?:www\\.)?github\\.com\\/([a-zA-Z0-9_\\-\\.]+)\\/([a-zA-Z0-9_\\-\\.]+)(?:\\/.*)?$");

    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public static class RepoDetails {
        public final String owner;
        public final String repo;

        public RepoDetails(String owner, String repo) {
            this.owner = owner;
            this.repo = repo.endsWith(".git") ? repo.substring(0, repo.length() - 4) : repo;
        }
    }

    public static class GitHubProjectAnalysisResult {
        public String owner;
        public String repo;
        public String defaultBranch;
        public String description;
        public String primaryLanguage;
        public boolean hasReadme;
        public String readmeContent;
        public List<String> fileTree = new ArrayList<>();
        public Map<String, String> keySourceSnippets = new LinkedHashMap<>();
        public List<String> detectedTechnologies = new ArrayList<>();
        public boolean hasTests;
        public int totalFilesCount;
    }

    public RepoDetails parseGitHubUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("GitHub repository URL cannot be empty.");
        }
        String clean = url.trim();
        Matcher matcher = GITHUB_URL_PATTERN.matcher(clean);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid GitHub repository URL format. Example format: https://github.com/username/repository");
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);
        return new RepoDetails(owner, repo);
    }

    public GitHubProjectAnalysisResult analyzePublicRepository(String githubUrl) {
        RepoDetails details = parseGitHubUrl(githubUrl);
        GitHubProjectAnalysisResult result = new GitHubProjectAnalysisResult();
        result.owner = details.owner;
        result.repo = details.repo;

        try {
            // 1. Fetch Repository Metadata
            String repoApiUrl = "https://api.github.com/repos/" + details.owner + "/" + details.repo;
            HttpRequest repoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(repoApiUrl))
                    .header("User-Agent", "SkillBridge-AI-Project-Evaluator")
                    .header("Accept", "application/vnd.github.v3+json")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> repoResponse = httpClient.send(repoRequest, HttpResponse.BodyHandlers.ofString());

            if (repoResponse.statusCode() == 404 || repoResponse.statusCode() == 403) {
                throw new IllegalArgumentException("This repository is private or inaccessible. Please verify the URL and ensure the repository is public.");
            }

            if (repoResponse.statusCode() != 200) {
                throw new IllegalArgumentException("Unable to access GitHub repository (HTTP " + repoResponse.statusCode() + "). Please ensure the repository is public and accessible.");
            }

            JsonNode repoJson = mapper.readTree(repoResponse.body());
            result.defaultBranch = repoJson.has("default_branch") ? repoJson.get("default_branch").asText("main") : "main";
            result.description = repoJson.has("description") && !repoJson.get("description").isNull() ? repoJson.get("description").asText("") : "";
            result.primaryLanguage = repoJson.has("language") && !repoJson.get("language").isNull() ? repoJson.get("language").asText("Unknown") : "Unknown";

            // 2. Fetch README Content
            fetchReadme(details, result);

            // 3. Fetch File Tree
            fetchFileTree(details, result);

            // 4. Sample Key Source Files
            sampleKeySourceFiles(details, result);

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error fetching GitHub repository: " + e.getMessage());
            throw new RuntimeException("Failed to analyze GitHub repository: " + e.getMessage(), e);
        }
    }

    private void fetchReadme(RepoDetails details, GitHubProjectAnalysisResult result) {
        String[] branches = new String[]{result.defaultBranch, "main", "master"};
        String[] readmeNames = new String[]{"README.md", "readme.md", "README", "Readme.md"};

        for (String branch : branches) {
            if (branch == null || branch.isBlank()) continue;
            for (String name : readmeNames) {
                try {
                    String rawUrl = "https://raw.githubusercontent.com/" + details.owner + "/" + details.repo + "/" + branch + "/" + name;
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(rawUrl))
                            .header("User-Agent", "SkillBridge-AI-Project-Evaluator")
                            .timeout(Duration.ofSeconds(4))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 && !response.body().isBlank()) {
                        result.hasReadme = true;
                        String body = response.body();
                        result.readmeContent = body.length() > 2500 ? body.substring(0, 2500) + "\n...[truncated]" : body;
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        result.hasReadme = false;
        result.readmeContent = "No README file found in public root.";
    }

    private void fetchFileTree(RepoDetails details, GitHubProjectAnalysisResult result) {
        try {
            String treeUrl = "https://api.github.com/repos/" + details.owner + "/" + details.repo + "/git/trees/" + result.defaultBranch + "?recursive=1";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(treeUrl))
                    .header("User-Agent", "SkillBridge-AI-Project-Evaluator")
                    .header("Accept", "application/vnd.github.v3+json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                JsonNode tree = root.get("tree");
                if (tree != null && tree.isArray()) {
                    result.totalFilesCount = tree.size();
                    Set<String> techs = new LinkedHashSet<>();
                    boolean testFound = false;

                    for (int i = 0; i < tree.size(); i++) {
                        String path = tree.get(i).has("path") ? tree.get(i).get("path").asText() : "";
                        if (!path.isBlank()) {
                            if (result.fileTree.size() < 60) {
                                result.fileTree.add(path);
                            }
                            String lower = path.toLowerCase(Locale.ROOT);
                            if (lower.contains("test") || lower.contains("spec") || lower.contains("__tests__")) {
                                testFound = true;
                            }
                            if (lower.endsWith(".kt") || lower.endsWith(".kts")) techs.add("Kotlin");
                            if (lower.endsWith(".java")) techs.add("Java");
                            if (lower.endsWith(".py")) techs.add("Python");
                            if (lower.endsWith(".js") || lower.endsWith(".jsx")) techs.add("JavaScript");
                            if (lower.endsWith(".ts") || lower.endsWith(".tsx")) techs.add("TypeScript");
                            if (lower.contains("pom.xml") || lower.contains("build.gradle")) techs.add("Maven/Gradle");
                            if (lower.contains("dockerfile") || lower.contains("docker-compose")) techs.add("Docker");
                            if (lower.contains("compose") || lower.contains("ui/theme")) techs.add("Jetpack Compose");
                            if (lower.contains("room") || lower.contains("entity") || lower.contains("dao")) techs.add("Database/Room");
                            if (lower.contains("retrofit") || lower.contains("api")) techs.add("REST API/Retrofit");
                            if (lower.contains("viewmodel")) techs.add("MVVM Architecture");
                        }
                    }
                    result.hasTests = testFound;
                    result.detectedTechnologies = new ArrayList<>(techs);
                }
            }
        } catch (Exception e) {
            System.err.println("Tree fetch note: " + e.getMessage());
        }
    }

    private void sampleKeySourceFiles(RepoDetails details, GitHubProjectAnalysisResult result) {
        // Find 3-4 representative source/config files from tree
        List<String> candidatePaths = result.fileTree.stream()
                .filter(p -> p.endsWith(".kt") || p.endsWith(".java") || p.endsWith(".py") || p.endsWith(".js") || p.endsWith(".tsx") || p.endsWith("pom.xml") || p.endsWith("build.gradle.kts") || p.endsWith("package.json"))
                .filter(p -> !p.toLowerCase(Locale.ROOT).contains("gradlew") && !p.toLowerCase(Locale.ROOT).contains("settings.gradle"))
                .limit(4)
                .toList();

        for (String path : candidatePaths) {
            try {
                String rawUrl = "https://raw.githubusercontent.com/" + details.owner + "/" + details.repo + "/" + result.defaultBranch + "/" + path;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(rawUrl))
                        .header("User-Agent", "SkillBridge-AI-Project-Evaluator")
                        .timeout(Duration.ofSeconds(4))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && !response.body().isBlank()) {
                    String snippet = response.body();
                    if (snippet.length() > 1500) {
                        snippet = snippet.substring(0, 1500) + "\n...[truncated]";
                    }
                    result.keySourceSnippets.put(path, snippet);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
