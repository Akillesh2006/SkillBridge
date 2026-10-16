package com.skillbridge.ai.data.remote.api

import com.skillbridge.ai.data.remote.dto.LoginRequest
import com.skillbridge.ai.data.remote.dto.LoginResponse
import com.skillbridge.ai.data.remote.dto.RoadmapResponse
import com.skillbridge.ai.data.remote.dto.ResumeResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("roadmap/generate")
    suspend fun generateRoadmap(
        @Query("roleName") roleName: String
    ): RoadmapResponse

    @GET("roadmap")
    suspend fun getRoadmaps(): List<RoadmapResponse>

    @Multipart
    @POST("resume/upload")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part
    ): ResumeResponse

    @GET("resume")
    suspend fun getResume(): ResumeResponse

    @POST("ai/chat")
    suspend fun chat(
        @Body request: com.skillbridge.ai.data.remote.dto.ChatRequest
    ): com.skillbridge.ai.data.remote.dto.ChatResponse

    @retrofit2.http.PATCH("roadmap/weeks/{weekNumber}/completion")
    suspend fun toggleWeekCompletion(
        @retrofit2.http.Path("weekNumber") weekNumber: Int,
        @Body request: Map<String, Boolean>
    ): List<RoadmapResponse>

    @retrofit2.http.PATCH("roadmap/tasks/{taskId}/completion")
    suspend fun toggleTaskCompletion(
        @retrofit2.http.Path("taskId") taskId: Long,
        @Body request: Map<String, Boolean>
    ): List<RoadmapResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: com.skillbridge.ai.data.remote.dto.RegisterRequest
    ): okhttp3.ResponseBody

    @retrofit2.http.POST("skill-test/start")
    suspend fun startAssessment(): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.GET("skill-test/{assessmentId}")
    suspend fun getAssessment(
        @retrofit2.http.Path("assessmentId") assessmentId: Long
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.POST("skill-test/{assessmentId}/submit")
    suspend fun submitAssessment(
        @retrofit2.http.Path("assessmentId") assessmentId: Long,
        @Body answers: List<com.skillbridge.ai.data.remote.dto.AnswerSubmissionDto>
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.GET("skill-test/{assessmentId}/result")
    suspend fun getAssessmentResult(
        @retrofit2.http.Path("assessmentId") assessmentId: Long
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.GET("skill-test/history")
    suspend fun getAssessmentHistory(): List<com.skillbridge.ai.data.remote.dto.AssessmentHistoryDto>

    @retrofit2.http.GET("skill-gap/analyze")
    suspend fun analyzeSkillGap(
        @retrofit2.http.Query("roleName") roleName: String
    ): com.skillbridge.ai.data.remote.dto.SkillGapResponse

    @GET("ai/copilot/context")
    suspend fun getCopilotContext(): com.skillbridge.ai.data.remote.dto.CopilotContextResponse

    @POST("ai/copilot/chat")
    suspend fun chatCopilot(
        @Body request: com.skillbridge.ai.data.remote.dto.ChatRequest
    ): com.skillbridge.ai.data.remote.dto.ChatResponse

    @GET("ai/daily-mission")
    suspend fun getDailyMission(): com.skillbridge.ai.data.remote.dto.DailyMissionResponse

    @GET("ai/career-readiness")
    suspend fun getCareerReadiness(): com.skillbridge.ai.data.remote.dto.CareerReadinessResponse

    @GET("ai/reminders/settings")
    suspend fun getReminderSettings(): com.skillbridge.ai.data.remote.dto.ReminderSettingsRequest

    @retrofit2.http.PUT("ai/reminders/settings")
    suspend fun updateReminderSettings(
        @Body request: com.skillbridge.ai.data.remote.dto.ReminderSettingsRequest
    ): com.skillbridge.ai.data.remote.dto.MessageResponse

    @POST("ai/mock-interview/start")
    suspend fun startInterview(): com.skillbridge.ai.data.remote.dto.InterviewStartResponse

    @POST("ai/mock-interview/{id}/answer")
    suspend fun submitInterviewAnswer(
        @retrofit2.http.Path("id") id: Long,
        @Body request: com.skillbridge.ai.data.remote.dto.AnswerSubmission
    ): com.skillbridge.ai.data.remote.dto.InterviewQuestionResponse

    @GET("ai/mock-interview/{id}/result")
    suspend fun getInterviewResult(
        @retrofit2.http.Path("id") id: Long
    ): com.skillbridge.ai.data.remote.dto.InterviewResultResponse

    @GET("ai/mock-interview/history")
    suspend fun getInterviewHistory(): List<com.skillbridge.ai.data.remote.dto.InterviewResultResponse>

    @POST("ai/challenge/generate")
    suspend fun generateDailyChallenge(): com.skillbridge.ai.data.remote.dto.ChallengeGenerateResponse

    @POST("ai/challenge/{id}/submit")
    suspend fun submitChallengeAnswer(
        @retrofit2.http.Path("id") id: Long,
        @Body request: com.skillbridge.ai.data.remote.dto.ChallengeSubmitRequest
    ): com.skillbridge.ai.data.remote.dto.ChallengeResultResponse

    @GET("ai/challenge/history")
    suspend fun getChallengeHistory(): List<com.skillbridge.ai.data.remote.dto.ChallengeResultResponse>

    @retrofit2.http.POST("mastery/tasks/{taskId}/start")
    suspend fun startMasteryCheck(
        @retrofit2.http.Path("taskId") taskId: Long
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.GET("mastery/{id}")
    suspend fun getMasteryCheck(
        @retrofit2.http.Path("id") id: Long
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.POST("mastery/{id}/submit")
    suspend fun submitMasteryCheck(
        @retrofit2.http.Path("id") id: Long,
        @Body answers: List<com.skillbridge.ai.data.remote.dto.AnswerSubmissionDto>
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.GET("mastery/{id}/result")
    suspend fun getMasteryCheckResult(
        @retrofit2.http.Path("id") id: Long
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @retrofit2.http.POST("mastery/{id}/retest")
    suspend fun retestMasteryCheck(
        @retrofit2.http.Path("id") id: Long
    ): com.skillbridge.ai.data.remote.dto.AssessmentResponse

    @GET("user/profile")
    suspend fun getUserProfile(): com.skillbridge.ai.data.remote.dto.ProfileResponseDto

    @retrofit2.http.PUT("user/profile")
    suspend fun updateUserProfile(
        @Body request: com.skillbridge.ai.data.remote.dto.UpdateProfileRequestDto
    ): com.skillbridge.ai.data.remote.dto.ProfileResponseDto

    @GET("ai/mission/active")
    suspend fun getActiveMission(): com.skillbridge.ai.data.remote.dto.CareerMissionResponse

    @POST("ai/mission/generate")
    suspend fun generateMission(): com.skillbridge.ai.data.remote.dto.CareerMissionResponse

    @POST("ai/mission/{id}/start")
    suspend fun startMission(
        @retrofit2.http.Path("id") id: Long
    ): com.skillbridge.ai.data.remote.dto.CareerMissionResponse

    @POST("ai/mission/{id}/submit")
    suspend fun submitMission(
        @retrofit2.http.Path("id") id: Long,
        @Body request: com.skillbridge.ai.data.remote.dto.MissionSubmitRequest
    ): com.skillbridge.ai.data.remote.dto.CareerMissionResponse

    @GET("ai/mission/history")
    suspend fun getMissionHistory(): List<com.skillbridge.ai.data.remote.dto.CareerMissionResponse>
}