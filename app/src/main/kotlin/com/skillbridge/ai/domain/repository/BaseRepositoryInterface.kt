package com.skillbridge.ai.domain.repository

/**
 * Marker interface for all domain-layer repository contracts.
 *
 * Repository interfaces in `domain/repository/` should extend this interface.
 * Concrete implementations live in `data/repository/` and are bound via Hilt
 * in `di/RepositoryModule.kt`.
 *
 * This pattern:
 * - Keeps the domain layer free of framework dependencies.
 * - Allows easy substitution of implementations (e.g. fake repositories in tests).
 * - Enforces separation of concerns between domain and data layers.
 */
interface BaseRepositoryInterface
