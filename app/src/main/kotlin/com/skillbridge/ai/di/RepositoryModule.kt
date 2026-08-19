package com.skillbridge.ai.di

import com.skillbridge.ai.data.repository.AuthRepositoryImpl
import com.skillbridge.ai.data.repository.DashboardRepositoryImpl
import com.skillbridge.ai.data.repository.ResumeRepositoryImpl
import com.skillbridge.ai.data.repository.JobRepositoryImpl
import com.skillbridge.ai.data.repository.SkillGapRepositoryImpl
import com.skillbridge.ai.data.repository.RoadmapRepositoryImpl
import com.skillbridge.ai.data.repository.ProgressRepositoryImpl
import com.skillbridge.ai.data.repository.ProfileRepositoryImpl

import com.skillbridge.ai.domain.repository.AuthRepository
import com.skillbridge.ai.domain.repository.DashboardRepository
import com.skillbridge.ai.domain.repository.ResumeRepository
import com.skillbridge.ai.domain.repository.JobRepository
import com.skillbridge.ai.domain.repository.SkillGapRepository
import com.skillbridge.ai.domain.repository.RoadmapRepository
import com.skillbridge.ai.domain.repository.ProgressRepository
import com.skillbridge.ai.domain.repository.ProfileRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindResumeRepository(
        impl: ResumeRepositoryImpl
    ): ResumeRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(
        impl: JobRepositoryImpl
    ): JobRepository

    @Binds
    @Singleton
    abstract fun bindSkillGapRepository(
        impl: SkillGapRepositoryImpl
    ): SkillGapRepository

    @Binds
    @Singleton
    abstract fun bindRoadmapRepository(
        impl: RoadmapRepositoryImpl
    ): RoadmapRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: com.skillbridge.ai.data.repository.ChatRepositoryImpl
    ): com.skillbridge.ai.domain.repository.ChatRepository

    @Binds
    @Singleton
    abstract fun bindAssessmentRepository(
        impl: com.skillbridge.ai.data.repository.AssessmentRepositoryImpl
    ): com.skillbridge.ai.domain.repository.AssessmentRepository

    @Binds
    @Singleton
    abstract fun bindCopilotRepository(
        impl: com.skillbridge.ai.data.repository.CopilotRepositoryImpl
    ): com.skillbridge.ai.domain.repository.CopilotRepository

    @Binds
    @Singleton
    abstract fun bindInterviewRepository(
        impl: com.skillbridge.ai.data.repository.InterviewRepositoryImpl
    ): com.skillbridge.ai.domain.repository.InterviewRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        impl: com.skillbridge.ai.data.repository.ChallengeRepositoryImpl
    ): com.skillbridge.ai.domain.repository.ChallengeRepository
}