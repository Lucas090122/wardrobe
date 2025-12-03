package com.example.wardrobe

import com.example.wardrobe.data.Member
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.MemberViewModel
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
class MemberViewModelTest {

    private lateinit var repository: WardrobeRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        every { repository.getAllMembers() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `members flow should expose data from repository`() = runTest {
        // Given
        val fakeMembers = listOf(
            Member(memberId = 1L, name = "Alice", gender = "Female", age = 20, birthDate = null),
            Member(memberId = 2L, name = "Bob", gender = "Male", age = 25, birthDate = null)
        )
        every { repository.getAllMembers() } returns flowOf(fakeMembers)

        // When
        val viewModel = MemberViewModel(repository)
        val result = viewModel.members.first { it.isNotEmpty() }

        // Then
        assertEquals(fakeMembers, result)
    }

    @Test
    fun `createMember should delegate to repository`() = runTest {
        // Given
        val name = "Charlie"
        val gender = "Male"
        val age = 30
        val birthDate: Long? = 123456789L

        val viewModel = MemberViewModel(repository)

        // When
        viewModel.createMember(name, gender, age, birthDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.createMember(name, gender, age, birthDate) }
    }

    @Test
    fun `setCurrentMember should update currentMemberId and currentMemberName`() = runTest {
        // Given
        val fakeMembers = listOf(
            Member(memberId = 1L, name = "Alice", gender = "Female", age = 20, birthDate = null),
            Member(memberId = 2L, name = "Bob", gender = "Male", age = 25, birthDate = null)
        )
        every { repository.getAllMembers() } returns flowOf(fakeMembers)

        val viewModel = MemberViewModel(repository)

        // When
        viewModel.setCurrentMember(2L)

        assertEquals(2L, viewModel.currentMemberId.value)

        val name = viewModel.currentMemberName.first { it.isNotEmpty() }

        // Then
        assertEquals("Bob", name)
    }

    @Test
    fun `outdatedCounts should be calculated for each member`() = runTest {
        // Given
        val fakeMembers = listOf(
            Member(memberId = 1L, name = "Alice", gender = "Female", age = 20, birthDate = null),
            Member(memberId = 2L, name = "Bob", gender = "Male", age = 25, birthDate = null)
        )
        every { repository.getAllMembers() } returns flowOf(fakeMembers)

        coEvery { repository.countOutdatedItems(1L) } returns 3
        coEvery { repository.countOutdatedItems(2L) } returns 5

        // When
        val viewModel = MemberViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle() // 等待 init 里那段 collect 走完

        val result = viewModel.outdatedCounts.value

        // Then
        assertEquals(mapOf(1L to 3, 2L to 5), result)
        coVerify { repository.countOutdatedItems(1L) }
        coVerify { repository.countOutdatedItems(2L) }
    }

    @Test
    fun `clearCurrentMember should set currentMemberId to null`() = runTest {
        // Given
        val viewModel = MemberViewModel(repository)

        viewModel.setCurrentMember(1L)
        assertEquals(1L, viewModel.currentMemberId.value)

        // When
        viewModel.clearCurrentMember()

        // Then
        assertEquals(null, viewModel.currentMemberId.value)
    }
}