package com.example.instalgam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.instalgam.model.Post
import com.example.instalgam.repository.PostRepository
import com.example.instalgam.room.DatabasePost
import kotlinx.coroutines.flow.map
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

    val posts: LiveData<List<Post>> =
        repository
            .getPosts()
            .map { dbPosts: List<DatabasePost> ->
                dbPosts.map { dbPost ->
                    Post(
                        dbPost.postId,
                        dbPost.userName,
                        dbPost.profilePicture,
                        dbPost.postImage,
                        dbPost.likeCount,
                        dbPost.likedByUser,
                    )
                }
            }.asLiveData()

    init {
//        fetchOfflinePosts()
        fetchOnlinePosts()
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

    fun fetchOfflinePosts() {
        // Posts are now automatically observed from Room database Flow
        // This method can be removed or kept for initial loading if needed
        viewModelScope.launch {
            // Initial posts are loaded from database via Flow
            // This is just for ensuring we have data on first load
        }
    }

    fun onClickLike(
        postID: String,
        position: Int,
    ) {
        viewModelScope.launch {
            val likeStatus = repository.getLikeStatus(postID)

            // Update database - the Flow will automatically emit new values and update the UI
            if (likeStatus) {
                repository.dislikePost(postID)
            } else {
                repository.likePost(postID)
            }
        }
    }

    fun fetchOnlinePosts() {
        viewModelScope.launch {
            // Fetch from API and save to database
            // The Flow will automatically emit the updated posts
            repository.fetchPostsOnline()
        }
    }

    fun onNetworkStatusChanged(isConnected: Boolean) {
        if (!isConnected) {
            hasDisconnected = true
            return
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
