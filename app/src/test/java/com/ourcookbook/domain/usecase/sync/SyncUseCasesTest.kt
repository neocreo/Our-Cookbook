package com.ourcookbook.domain.usecase.sync

import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.ConflictStatus
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.model.VersionVector
import com.ourcookbook.domain.repository.SyncConflictRepository
import com.ourcookbook.domain.usecase.recipe.GetRecipeById
import com.ourcookbook.domain.usecase.recipe.UpdateRecipe
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for Sync Use Cases
 * Tests the business logic of sync-related use cases
 */
class SyncUseCasesTest {

    private lateinit var repository: SyncConflictRepository
    private lateinit var createConflict: CreateConflict
    private lateinit var updateConflict: UpdateConflict
    private lateinit var deleteConflict: DeleteConflict
    private lateinit var getConflictById: GetConflictById
    private lateinit var getConflictsByStatus: GetConflictsByStatus
    private lateinit var resolveSyncConflict: ResolveSyncConflict

    private val localVersion = VersionVector("device1", 1, Instant.now())
    private val remoteVersion = VersionVector("device2", 1, Instant.now())
    
    private val testConflict = SyncConflict(
        id = UUID.randomUUID().toString(),
        localRecipeId = UUID.randomUUID().toString(),
        remoteRecipeId = UUID.randomUUID().toString(),
        localChecksum = "local-checksum",
        remoteChecksum = "remote-checksum",
        localVersion = localVersion,
        remoteVersion = remoteVersion,
        detectedAt = Instant.now(),
        status = ConflictStatus.PENDING
    )

    @Before
    fun setup() {
        repository = mock()
        createConflict = CreateConflict(repository)
        updateConflict = UpdateConflict(repository)
        deleteConflict = DeleteConflict(repository)
        getConflictById = GetConflictById(repository)
        getConflictsByStatus = GetConflictsByStatus(repository)
        
        val getConflictByIdUseCase = GetConflictById(repository)
        resolveSyncConflict = ResolveSyncConflict(repository, getConflictByIdUseCase, updateConflict)
    }

    @Test
    fun `CreateConflict with valid conflict returns success`() = runTest {
        // Given
        val expectedId = UUID.randomUUID().toString()
        whenever(repository.createConflict(any())).thenReturn(expectedId)

        // When
        val result = createConflict(testConflict)

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == expectedId)
        verify(repository).createConflict(eq(testConflict))
    }

    @Test
    fun `CreateConflict with invalid conflict returns failure`() = runTest {
        // Given
        val invalidConflict = testConflict.copy(localRecipeId = "") // Empty recipe ID

        // When
        val result = createConflict(invalidConflict)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `UpdateConflict returns success`() = runTest {
        // Given
        whenever(repository.updateConflict(any())).then { /* do nothing */ }

        // When
        val result = updateConflict(testConflict)

        // Then
        assert(result.isSuccess)
        verify(repository).updateConflict(eq(testConflict))
    }

    @Test
    fun `DeleteConflict returns success`() = runTest {
        // Given
        val conflictId = UUID.randomUUID().toString()
        whenever(repository.deleteConflict(any())).then { /* do nothing */ }

        // When
        val result = deleteConflict(conflictId)

        // Then
        assert(result.isSuccess)
        verify(repository).deleteConflict(eq(conflictId))
    }

    @Test
    fun `GetConflictById returns conflict when found`() = runTest {
        // Given
        val conflictId = testConflict.id
        whenever(repository.getConflictById(eq(conflictId))).thenReturn(testConflict)

        // When
        val result = getConflictById(conflictId)

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == testConflict)
        verify(repository).getConflictById(eq(conflictId))
    }

    @Test
    fun `GetConflictById returns null when not found`() = runTest {
        // Given
        val conflictId = UUID.randomUUID().toString()
        whenever(repository.getConflictById(eq(conflictId))).thenReturn(null)

        // When
        val result = getConflictById(conflictId)

        // Then
        assert(result.isSuccess)
        assert(result.getOrThrow() == null)
    }

    @Test
    fun `ResolveSyncConflict with KeepLocal resolution returns success`() = runTest {
        // Given
        val conflictId = testConflict.id
        whenever(repository.getConflictById(eq(conflictId))).thenReturn(testConflict)
        whenever(repository.updateConflict(any())).then { /* do nothing */ }

        // When
        val result = resolveSyncConflict(conflictId, ConflictResolution.KeepLocal)

        // Then
        assert(result.isSuccess)
        verify(repository).getConflictById(eq(conflictId))
        verify(repository).updateConflict(any())
    }

    @Test
    fun `ResolveSyncConflict returns failure when conflict not found`() = runTest {
        // Given
        val conflictId = UUID.randomUUID().toString()
        whenever(repository.getConflictById(eq(conflictId))).thenReturn(null)

        // When
        val result = resolveSyncConflict(conflictId, ConflictResolution.KeepLocal)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `GetConflictsByStatus returns flow of conflicts`() = runTest {
        // Given
        val conflicts = listOf(testConflict)
        whenever(repository.getConflictsByStatus(any())).thenReturn(kotlinx.coroutines.flow.flowOf(conflicts))

        // When
        val result = getConflictsByStatus(ConflictStatus.PENDING)

        // Then
        // Flow test - compilation check
        verify(repository).getConflictsByStatus(eq(ConflictStatus.PENDING))
    }
}
