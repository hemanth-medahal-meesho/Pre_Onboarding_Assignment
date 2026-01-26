package com.example.instalgam.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.instalgam.apiClient.LikeBody
import com.example.instalgam.apiClient.RetrofitApiClient
import com.example.instalgam.model.Post
import com.example.instalgam.repository.PostRepository
import com.example.instalgam.repository.PreferencesRepository
import com.example.instalgam.room.DatabasePost
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: PostRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent
    private val _toastEvent = MutableLiveData<ToastEvent?>()
    val toastEvent: LiveData<ToastEvent?> = _toastEvent
    private var hasDisconnected = false
//    private var hasFetched = false

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
        preferencesRepository.signOutUser()
    }

    fun onReelsButtonClick() {
        _navigationEvent.value = NavigationEvent.Reels
    }

    fun navigationComplete() {
        _navigationEvent.value = null
    }

//    fun fetchOfflinePosts() {
//        viewModelScope.launch {
//        }
//    }

    fun onClickLike(
        postID: String,
        position: Int,
    ) {
        viewModelScope.launch {
            val likeStatus = repository.getLikeStatus(postID)

            if (likeStatus) {
                val status = repository.dislikePost(postID)
                if (!status) {
                    val existingPendingLike = repository.getPendingLike(postID)
                    if (existingPendingLike == null) {
                        repository.addPendingLike(postID, true)
                    } else {
                        repository.removePendingLike(postID)
                    }
                    _toastEvent.value = ToastEvent.FailedToDislikePost
                }
            } else {
                val status = repository.likePost(postID)

                if (!status) {
                    val existingPendingLike = repository.getPendingLike(postID)
                    if (existingPendingLike == null) {
                        repository.addPendingLike(postID, false)
                    } else {
                        repository.removePendingLike(postID)
                    }
                    _toastEvent.value = ToastEvent.FailedToLikePost
                }
            }
        }
    }

    fun deleteAllPendingLikes() {
        viewModelScope.launch {
            repository.removeAllLikes()
        }
    }

    fun fetchOnlinePosts() {
        viewModelScope.launch {
            val status = repository.fetchPostsOnline()
            if (status.first == 1) {
                _toastEvent.value = ToastEvent.FailedToLoadPosts
            } else if (status.first == 2) {
                _toastEvent.value = ToastEvent.ErrorOccured(status.second!!)
            }
        }
    }

    fun onNetworkStatusChanged(isConnected: Boolean) {
        if (!hasDisconnected && !isConnected) {
            _toastEvent.value = ToastEvent.NoConnection
            hasDisconnected = true
            return
        }

        if (isConnected) {
            likeUnsyncedPosts()
        }

//        if (hasDisconnected && isConnected) {
//        }
//        if (!isConnected) {
//            hasDisconnected = true
//            return
//        }
//
//        if (hasDisconnected) {
//            likeUnsyncedPosts()
//            hasDisconnected = false
//        }
    }

    fun likeUnsyncedPosts() {
        viewModelScope.launch {
            val pendingLikes = repository.getAllPendingLikes()
            Log.d("pendingLikes", "Size of pending likes: ${pendingLikes.size}")

            if (pendingLikes.isEmpty()) {
                Log.d("apiStatus", "No pending likes to sync")
                return@launch
            }

            viewModelScope.launch {
                for ((index, pending) in pendingLikes.withIndex()) {
//                if (index > 0) {
//                    kotlinx.coroutines.delay(100)
//                }

                    try {
                        val response =
                            if (pending.liked) {
                                Log.d("apiStatus", "Liking post: ${pending.postId}")
                                RetrofitApiClient.postsApiService.likePost(
                                    LikeBody(true, pending.postId),
                                )
                            } else {
                                Log.d("apiStatus", "Disliking post: ${pending.postId}")
                                // Note: dislikePost() doesn't take postId parameter in current API
                                // This might need to be fixed if the API requires it
                                RetrofitApiClient.postsApiService.dislikePost()
                            }

                        if (response.isSuccessful) {
                            repository.removePendingLike(pending.postId)
                            Log.d("apiStatus", "Successfully synced like for post: ${pending.postId}")
                        } else {
                            Log.e("apiStatus", "Sync failed for post ${pending.postId}: HTTP ${response.code()}, ${response.message()}")
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        Log.e("apiStatus", "Timeout while syncing post ${pending.postId}", e)
                        // Continue with next post instead of stopping
                    } catch (e: Exception) {
                        Log.e("apiStatus", "Sync failed for post ${pending.postId}", e)
                        // Continue with next post instead of stopping
                    }
                }
            }
        }
    }
}

sealed class NavigationEvent {
    object SignOut : NavigationEvent()

    object Reels : NavigationEvent()
}

sealed class ToastEvent {
    object FailedToLoadPosts : ToastEvent()

    data class ErrorOccured(
        val errorMessage: String,
    ) : ToastEvent()

    object FailedToLikePost : ToastEvent()

    object FailedToDislikePost : ToastEvent()

    object NoConnection : ToastEvent()
}
