package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.domain.model.*
import com.skillbridge.ai.domain.repository.AssessmentRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssessmentRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AssessmentRepository {

    private var activeAssessment: Assessment? = null
    private val historyList = mutableListOf<AssessmentHistoryItem>()

    // Mock Questions Database
    private val mockQuestions = listOf(
        AssessmentQuestion(
            id = 1L,
            skillName = "Kotlin",
            difficulty = "EASY",
            questionText = "Which of the following is used to create a read-only variable in Kotlin?",
            options = listOf("val", "var", "const", "let"),
            correctAnswer = 0,
            explanation = "val declares a read-only variable, whereas var is for mutable variables."
        ),
        AssessmentQuestion(
            id = 2L,
            skillName = "Java",
            difficulty = "MEDIUM",
            questionText = "Which keyword is used to inherit a class in Java?",
            options = listOf("implements", "extends", "inherits", "super"),
            correctAnswer = 1,
            explanation = "extends is used for class inheritance, while implements is for interface implementation."
        ),
        AssessmentQuestion(
            id = 3L,
            skillName = "Spring Boot",
            difficulty = "MEDIUM",
            questionText = "Which annotation is used to mark a class as a REST controller in Spring Boot?",
            options = listOf("@Controller", "@RestController", "@ResponseBody", "@Component"),
            correctAnswer = 1,
            explanation = "@RestController combines @Controller and @ResponseBody."
        ),
        AssessmentQuestion(
            id = 4L,
            skillName = "SQL",
            difficulty = "EASY",
            questionText = "Which SQL clause is used to filter records in a SELECT statement?",
            options = listOf("GROUP BY", "ORDER BY", "WHERE", "HAVING"),
            correctAnswer = 2,
            explanation = "WHERE is used to filter rows before grouping, while HAVING filters after grouping."
        ),
        AssessmentQuestion(
            id = 5L,
            skillName = "Git",
            difficulty = "HARD",
            questionText = "Which command is used to combine multiple commits into a single commit?",
            options = listOf("git rebase -i", "git merge", "git commit --amend", "git checkout"),
            correctAnswer = 0,
            explanation = "Interactive rebase (git rebase -i) allows squash or fixup options to combine commits."
        ),
        AssessmentQuestion(
            id = 6L,
            skillName = "Data Structures",
            difficulty = "MEDIUM",
            questionText = "What is the time complexity of searching an element in a balanced binary search tree (BST)?",
            options = listOf("O(1)", "O(n)", "O(log n)", "O(n log n)"),
            correctAnswer = 2,
            explanation = "Balanced BSTs divide the search space by half at each step, giving logarithmic time complexity."
        ),
        AssessmentQuestion(
            id = 7L,
            skillName = "Jetpack Compose",
            difficulty = "MEDIUM",
            questionText = "How do you observe a StateFlow object as a Compose State in Compose?",
            options = listOf("collectAsState()", "observeAsState()", "rememberState()", "state()"),
            correctAnswer = 0,
            explanation = "collectAsState() is the extension function to collect values from a Flow and represent it as a Compose State."
        ),
        AssessmentQuestion(
            id = 8L,
            skillName = "REST APIs",
            difficulty = "MEDIUM",
            questionText = "Which HTTP method is designed to be idempotent and used to update resources?",
            options = listOf("POST", "PUT", "GET", "DELETE"),
            correctAnswer = 1,
            explanation = "PUT is idempotent, meaning multiple identical requests will produce the same result."
        ),
        AssessmentQuestion(
            id = 9L,
            skillName = "Docker",
            difficulty = "EASY",
            questionText = "What is the name of the file used to define the instructions to build a Docker image?",
            options = listOf("DockerConfig", "DockerCompose", "Dockerfile", "DockerImage"),
            correctAnswer = 2,
            explanation = "A Dockerfile is a text document that contains all the commands a user could call on the command line to assemble an image."
        ),
        AssessmentQuestion(
            id = 10L,
            skillName = "Design Patterns",
            difficulty = "HARD",
            questionText = "Which design pattern ensures a class has only one instance and provides a global point of access to it?",
            options = listOf("Singleton", "Factory", "Observer", "Builder"),
            correctAnswer = 0,
            explanation = "The Singleton pattern restricts instantiation of a class to a single object."
        )
    )

    override suspend fun startAssessment(): Result<Assessment> {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        // Clean questions to remove correctAnswer and explanation to emulate API security
        val sanitizedQuestions = mockQuestions.map {
            it.copy(correctAnswer = null, explanation = null)
        }

        val assessment = Assessment(
            id = System.currentTimeMillis(),
            score = 0,
            createdDate = now,
            questions = sanitizedQuestions
        )
        activeAssessment = assessment
        return Result.success(assessment)
    }

    override suspend fun getAssessment(assessmentId: Long): Result<Assessment> {
        activeAssessment?.let {
            if (it.id == assessmentId) {
                return Result.success(it)
            }
        }
        return Result.failure(Exception("Assessment not found"))
    }

    override suspend fun submitAssessment(
        assessmentId: Long,
        answers: List<Pair<Long, Int>>
    ): Result<Assessment> {
        val current = activeAssessment ?: return Result.failure(Exception("No active assessment"))
        if (current.id != assessmentId) {
            return Result.failure(Exception("Assessment ID mismatch"))
        }

        var correctCount = 0
        val gradedQuestions = mockQuestions.map { mockQ ->
            val studentAnswer = answers.find { it.first == mockQ.id }?.second
            val isCorrect = studentAnswer == mockQ.correctAnswer
            if (isCorrect) correctCount++
            
            mockQ.copy(
                studentAnswer = studentAnswer,
                isCorrect = isCorrect
            )
        }

        val scorePercentage = (correctCount * 100) / mockQuestions.size
        
        // Group by skill to generate breakdown
        val breakdown = gradedQuestions.map { q ->
            val status = if (q.isCorrect == true) "VERIFIED" else "NEEDS_IMPROVEMENT"
            SkillVerificationDetail(
                skillName = q.skillName,
                totalQuestions = 1,
                correctQuestions = if (q.isCorrect == true) 1 else 0,
                percentage = if (q.isCorrect == true) 100 else 0,
                status = status
            )
        }

        val gradedAssessment = Assessment(
            id = assessmentId,
            score = scorePercentage,
            createdDate = current.createdDate,
            questions = gradedQuestions,
            skillBreakdown = breakdown
        )

        activeAssessment = gradedAssessment
        
        // Add to history list
        val historyItem = AssessmentHistoryItem(
            id = assessmentId,
            score = scorePercentage,
            createdDate = current.createdDate
        )
        historyList.removeAll { it.id == assessmentId }
        historyList.add(0, historyItem)

        return Result.success(gradedAssessment)
    }

    override suspend fun getAssessmentResult(assessmentId: Long): Result<Assessment> {
        activeAssessment?.let {
            if (it.id == assessmentId) {
                return Result.success(it)
            }
        }
        return Result.failure(Exception("Result details not found"))
    }

    override suspend fun getAssessmentHistory(): Result<List<AssessmentHistoryItem>> {
        return Result.success(historyList)
    }

    private fun mapResponseToDomain(dto: com.skillbridge.ai.data.remote.dto.AssessmentResponse): Assessment {
        return Assessment(
            id = dto.id,
            score = dto.score,
            createdDate = dto.createdDate,
            questions = dto.questions.map { q ->
                AssessmentQuestion(
                    id = q.id,
                    skillName = q.skillName,
                    difficulty = q.difficulty,
                    questionText = q.questionText,
                    options = q.options,
                    correctAnswer = q.correctAnswer,
                    studentAnswer = q.studentAnswer,
                    isCorrect = q.isCorrect,
                    explanation = q.explanation,
                    concept = q.concept,
                    questionType = q.questionType
                )
            },
            skillBreakdown = dto.skillBreakdown.map { sb ->
                SkillVerificationDetail(
                    skillName = sb.skillName,
                    totalQuestions = sb.totalQuestions,
                    correctQuestions = sb.correctQuestions,
                    percentage = sb.percentage,
                    status = sb.status
                )
            },
            assessmentType = dto.assessmentType,
            masteryLevel = dto.masteryLevel,
            strengths = dto.strengths,
            weakConcepts = dto.weakConcepts,
            aiFeedback = dto.aiFeedback,
            recommendedPractice = dto.recommendedPractice,
            conceptualScore = dto.conceptualScore,
            applicationScore = dto.applicationScore,
            problemSolvingScore = dto.problemSolvingScore,
            debuggingScore = dto.debuggingScore
        )
    }

    override suspend fun startMasteryCheck(taskId: Long): Result<Assessment> {
        return try {
            val response = apiService.startMasteryCheck(taskId)
            Result.success(mapResponseToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMasteryCheck(assessmentId: Long): Result<Assessment> {
        return try {
            val response = apiService.getMasteryCheck(assessmentId)
            Result.success(mapResponseToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitMasteryCheck(
        assessmentId: Long,
        answers: List<Pair<Long, Int>>
    ): Result<Assessment> {
        return try {
            val dtoList = answers.map {
                com.skillbridge.ai.data.remote.dto.AnswerSubmissionDto(
                    questionId = it.first,
                    studentAnswer = it.second
                )
            }
            val response = apiService.submitMasteryCheck(assessmentId, dtoList)
            Result.success(mapResponseToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMasteryCheckResult(assessmentId: Long): Result<Assessment> {
        return try {
            val response = apiService.getMasteryCheckResult(assessmentId)
            Result.success(mapResponseToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retestMasteryCheck(assessmentId: Long): Result<Assessment> {
        return try {
            val response = apiService.retestMasteryCheck(assessmentId)
            Result.success(mapResponseToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
