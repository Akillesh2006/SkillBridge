package com.skillbridge.ai.domain.usecase

/**
 * Abstract base class for all use cases.
 *
 * Each use case encapsulates a single business operation and exposes it via
 * [invoke] so callers can use the clean `useCase(params)` syntax.
 *
 * ## Usage
 * ```kotlin
 * class GetUserProfileUseCase @Inject constructor(
 *     private val repository: UserRepository,
 * ) : BaseUseCase<String, Flow<Resource<UserProfile>>>() {
 *
 *     override suspend fun invoke(params: String): Flow<Resource<UserProfile>> {
 *         return repository.getUserProfile(params)
 *     }
 * }
 * ```
 *
 * @param Params The input parameter type for the use case.
 * @param Result The output type returned by the use case.
 */
abstract class BaseUseCase<in Params, out Result> {

    /**
     * Executes the use case with the given [params].
     *
     * @param params Input required by the use case.
     * @return The result of the business operation.
     */
    abstract suspend operator fun invoke(params: Params): Result
}
