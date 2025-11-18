package com.example.wardrobe.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.wardrobe.WardrobeApp
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.MemberViewModel

@Composable
fun Home(
    repo: WardrobeRepository,
    memberViewModel : MemberViewModel
){

    /*val memberVmFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MemberViewModel(repo) as T
        }
    }*/
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    if (selectedMemberId == null) {
        // This is the root screen, handle app exit here
        var lastBackTime by remember { mutableLongStateOf(0L) }
        val context = LocalContext.current
        val activity = (context as? Activity)
        BackHandler(enabled = true) {
            val now = System.currentTimeMillis()
            if (now - lastBackTime < 2000) {
                activity?.finish() // Exit the app
            } else {
                lastBackTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }

        // val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
        MemberSelectionScreen(
            vm = memberViewModel,
            onMemberSelected = { memberId ->
                selectedMemberId = memberId
            }
        )
    } else {
        WardrobeApp(memberId = selectedMemberId!!) {
            selectedMemberId = null // On back pressed from app, go back to member selection
            memberViewModel.setCurrentMember(null)
        }
    }
}