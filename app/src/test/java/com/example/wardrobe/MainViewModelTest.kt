package com.example.wardrobe

import com.example.wardrobe.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startBindLocationMode should switch to BindLocation and clear pendingTag`() {
        val vm = MainViewModel()

        vm.pendingTagIdForBinding.value = "OLD_TAG"

        vm.startBindLocationMode()

        assertEquals(MainViewModel.NfcMode.BindLocation, vm.nfcMode.value)
        assertNull(vm.pendingTagIdForBinding.value)
    }

    @Test
    fun `cancelBindLocationMode should switch to Idle and clear pendingTag`() {
        val vm = MainViewModel()

        vm.startBindLocationMode()
        vm.pendingTagIdForBinding.value = "TAG_123"

        vm.cancelBindLocationMode()

        assertEquals(MainViewModel.NfcMode.Idle, vm.nfcMode.value)
        assertNull(vm.pendingTagIdForBinding.value)
    }

    @Test
    fun `onTagScanned in BindLocation mode should only set pendingTagId`() = runTest {
        val vm = MainViewModel()
        vm.startBindLocationMode()

        vm.onTagScanned("TAG_ABC") { _ ->
            42L
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MainViewModel.NfcMode.BindLocation, vm.nfcMode.value)
        assertEquals("TAG_ABC", vm.pendingTagIdForBinding.value)
        assertNull(vm.navigateToLocationId.value)
    }

    @Test
    fun `onTagScanned in Idle mode should resolve location and set navigateToLocationId`() = runTest {
        val vm = MainViewModel()
        vm.cancelBindLocationMode()

        vm.onTagScanned("TAG_XYZ") { tagId ->
            if (tagId == "TAG_XYZ") 100L else null
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MainViewModel.NfcMode.Idle, vm.nfcMode.value)
        assertEquals(100L, vm.navigateToLocationId.value)
    }

    @Test
    fun `clearNavigationRequest should set navigateToLocationId to null`() {
        val vm = MainViewModel()
        vm.navigateToLocationId.value = 200L

        vm.clearNavigationRequest()

        assertNull(vm.navigateToLocationId.value)
    }

    @Test
    fun `onLocationBound should exit BindLocation mode`() {
        val vm = MainViewModel()
        vm.startBindLocationMode()

        vm.onLocationBound()

        assertEquals(MainViewModel.NfcMode.Idle, vm.nfcMode.value)
        assertNull(vm.pendingTagIdForBinding.value)
    }

    @Test
    fun `setCurrentScreen should update currentScreen`() {
        val vm = MainViewModel()

        vm.setCurrentScreen(Screen.DrawerScreen.Settings)

        assertEquals(Screen.DrawerScreen.Settings, vm.currentScreen.value)
    }
}