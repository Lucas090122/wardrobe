package com.example.wardrobe

import com.example.wardrobe.data.CategoryCount
import com.example.wardrobe.data.NameCount
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.SeasonCount
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.StatisticsViewModel
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
class StatisticsViewModelTest {

    private lateinit var repository: WardrobeRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `countByMember flow should expose data from repository`() = runTest {
        // Given
        val fakeData = listOf(NameCount("Alice", 5), NameCount("Bob", 3))
        every { repository.getCountByMember() } returns flowOf(fakeData)
        every { repository.getCountBySeason() } returns flowOf(emptyList())
        every { repository.getCountByCategory() } returns flowOf(emptyList())

        // When
        val viewModel = StatisticsViewModel(repository)
        val result = viewModel.countByMember.first { it.isNotEmpty() }

        // Then
        assertEquals(fakeData, result)
    }

    @Test
    fun `countBySeason flow should expose data from repository`() = runTest {
        // Given
        val fakeData = listOf(
            SeasonCount(Season.SUMMER, 10),
            SeasonCount(Season.WINTER, 8)
        )
        every { repository.getCountByMember() } returns flowOf(emptyList())
        every { repository.getCountBySeason() } returns flowOf(fakeData)
        every { repository.getCountByCategory() } returns flowOf(emptyList())

        // When
        val viewModel = StatisticsViewModel(repository)
        val result = viewModel.countBySeason.first { it.isNotEmpty() }

        // Then
        assertEquals(fakeData, result)
    }

    @Test
    fun `countByCategory flow should expose data from repository`() = runTest {
        // Given
        val fakeData = listOf(
            CategoryCount("TOP", 12),
            CategoryCount("PANTS", 6)
        )
        every { repository.getCountByMember() } returns flowOf(emptyList())
        every { repository.getCountBySeason() } returns flowOf(emptyList())
        every { repository.getCountByCategory() } returns flowOf(fakeData)

        // When
        val viewModel = StatisticsViewModel(repository)
        val result = viewModel.countByCategory.first { it.isNotEmpty() }

        // Then
        assertEquals(fakeData, result)
    }
}
