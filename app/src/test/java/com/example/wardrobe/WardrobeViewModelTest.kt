package com.example.wardrobe

import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.ClothingWithTags
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.TransferHistoryDetails
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.WardrobeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WardrobeViewModelTest {

    private lateinit var repository: WardrobeRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `transferItem should call repository methods`() = runTest {
        // Given
        val itemId = 10L
        val originalOwnerId = 1L
        val newOwnerId = 2L
        val clothingItem = ClothingItem(
            itemId = itemId,
            ownerMemberId = originalOwnerId,
            description = "Test Item",
            imageUri = null,
            category = "TOP",
            warmthLevel = 3,
            occasions = "CASUAL",
            isWaterproof = false,
            color = "#FFFFFF",
            isFavorite = false,
            season = Season.SPRING_AUTUMN,
            lastWornAt = 0L,
            sizeLabel = null
        )
        val clothingWithTags = ClothingWithTags(item = clothingItem, tags = emptyList())
        coEvery { repository.observeItem(itemId) } returns flowOf(clothingWithTags)

        // When
        val viewModel = WardrobeViewModel(repository, originalOwnerId)
        viewModel.transferItem(itemId, newOwnerId)
        testDispatcher.scheduler.advanceUntilIdle() // Execute the coroutine

        // Then
        coVerify { repository.transferItem(itemId, newOwnerId) }
        coVerify { repository.recordTransferHistory(any()) }
    }

    @Test
    fun `transferHistory flow should expose data from repository`() = runTest {
        // Given
        val fakeHistory = listOf(
            TransferHistoryDetails(System.currentTimeMillis(), "Alice", "Bob", "T-Shirt"),
            TransferHistoryDetails(System.currentTimeMillis() - 10000, "Bob", "Charlie", "Jeans")
        )
        every { repository.getAllTransferHistoryDetails() } returns flowOf(fakeHistory)

        // When
        val viewModel = WardrobeViewModel(repository, 1L)
        // The `stateIn` with WhileSubscribed needs a collector to be active.
        // We collect the flow here and wait for the first non-empty list to be emitted.
        val result = viewModel.transferHistory.first { it.isNotEmpty() }

        // Then
        assertEquals(fakeHistory, result)
    }
}
