package com.example.instalgam.viewmodel

import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instalgam.model.Post
import com.example.instalgam.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: PostRepository,
) : ViewModel() {
    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent
    private val _postEvent = MutableLiveData<PostEvent?>()
    val postEvent: LiveData<PostEvent?> = _postEvent

    private var hasDisconnected = false
    private var hasFetched = false
    private val _posts = MutableLiveData<List<Post>?>()
    val posts: LiveData<List<Post>?> = _posts

    init {
        fetchOfflinePosts()
    }

    fun onSignOutButtonClick() {
        _navigationEvent.value = NavigationEvent.SignOut
        repository.signOutUser()
    }

    fun onReelsButtonClick() {
        _navigationEvent.value = NavigationEvent.Reels
    }

    fun navigationComplete() {
        _navigationEvent.value = null
    }

//    fun checkConnectivityStatus(): Boolean {
//        if (connectivityManager.activeNetwork == null) {
//            Log.d("networkStatus", "Network is not available on startup")
//            return false
// //            Toast
// //                .makeText(
// //                    this@PostFeedActivity,
// //                    "Device is not connected to a network. Loading posts from Room database",
// //                    Toast.LENGTH_SHORT,
// //                ).show()
// //            fetchPostsOffline()
//        } else {
//            Log.d("networkStatus", "Network is available")
//            return true
// //            lifecycleScope.launch {
// //                fetchPostsOnline()
// //            }
//        }
//    }

    fun fetchOfflinePosts() {
        viewModelScope.launch {
            _posts.value = repository.fetchPostsOffline()
        }
    }

    fun fetchOnlinePosts() {
        viewModelScope.launch {
            _posts.value = repository.fetchPostsOnline()
        }
    }

    fun onNetworkStatusChanged(isConnected: Boolean) {
        if (!isConnected) {
            hasDisconnected = true
            return
        }

        if (!hasFetched) {
            hasFetched = true
            fetchOnlinePosts()
        }

        if (hasDisconnected) {
//            syncUnsyncedLikes()
            hasDisconnected = false
        }
    }
}

sealed class NavigationEvent {
    object SignOut : NavigationEvent()

    object Reels : NavigationEvent()
}

sealed class PostEvent {
    object Like : PostEvent()

    object PostsUpdate : PostEvent()
}

sealed class NetworkStatus {
    object Connected : NetworkStatus()

    object NotConnected : NetworkStatus()
}
