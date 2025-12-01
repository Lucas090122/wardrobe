package com.example.wardrobe

import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.ClothingWithTags
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.WardrobeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WardrobeViewModelTest {

    private lateinit var viewModel: WardrobeViewModel
    private lateinit var repository: WardrobeRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        // Assuming memberId 1L for tests
        viewModel = WardrobeViewModel(repository, 1L)
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

        // Mock the repository call to get the item's original owner
        coEvery { repository.observeItem(itemId) } returns flowOf(clothingWithTags)

        // When
        viewModel.transferItem(itemId, newOwnerId)
        testDispatcher.scheduler.advanceUntilIdle() // Execute the coroutine

        // Then
        // Verify that the repository's transferItem method was called
        coVerify { repository.transferItem(itemId, newOwnerId) }

        // Verify that the repository's recordTransferHistory method was called
        coVerify { repository.recordTransferHistory(any()) }
    }
}
