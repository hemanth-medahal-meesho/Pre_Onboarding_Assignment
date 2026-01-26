package com.example.instalgam.repository

import android.util.Log
import com.example.instalgam.apiClient.LikeBody
import com.example.instalgam.apiClient.RetrofitApiClient
import com.example.instalgam.model.Post
import com.example.instalgam.room.DatabasePost
import com.example.instalgam.room.PendingLike
import com.example.instalgam.room.PendingLikesDao
import com.example.instalgam.room.PostDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PostRepository(
    val postDao: PostDao,
    val pendingLikesDao: PendingLikesDao,
) {
    fun getPosts(): Flow<List<DatabasePost>> = postDao.fetchLivePosts()

    suspend fun getPostsAtFirst(): List<DatabasePost> = postDao.fetchAll()

    suspend fun fetchPostsOffline(): List<Post> {
        val dbPosts = getPostsAtFirst()
        Log.d("dbStatus", "Loaded ${dbPosts.size} posts from database")

        val posts =
            dbPosts.map {
                Post(
                    it.postId,
                    it.userName,
                    it.profilePicture,
                    it.postImage,
                    it.likeCount,
                    it.likedByUser,
                )
            }
//        postAdapter.submitList(posts)
        return posts
    }

    suspend fun fetchPostsOnline(): Pair<Int, String?> {
        try {
            val response = RetrofitApiClient.postsApiService.fetchPosts()
            if (response.isSuccessful) {
                val apiPosts = response.body()?.posts?.filterNotNull() ?: emptyList()
//                postAdapter.submitList(apiPosts)
                val dbPosts =
                    apiPosts.map {
                        DatabasePost(
                            it.postId,
                            it.userName,
                            it.profilePicture,
                            it.postImage,
                            it.likeCount,
                            it.likedByUser,
                        )
                    }
                Log.d("dbStatus", "Pushed ${dbPosts.size} posts into database")
//                dbHelper.savePosts(dbPosts)
                savePosts(dbPosts)
                return Pair(0, null)
            } else {
//                Toast.makeText(this@PostFeedActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
//                        fetchPostsOffline()
                return Pair(1, null)
            }
        } catch (e: Exception) {
//            Toast.makeText(this@PostFeedActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
//                    fetchPostsOffline()

            Log.e("apiStatus", e.message.toString())
            return Pair(2, e.message)
        }
    }

    suspend fun savePosts(posts: List<DatabasePost>) {
        postDao.deleteAll()
        postDao.insertAll(posts)
    }

    suspend fun likePost(postID: String): Boolean =
        withContext(Dispatchers.IO) {
            postDao.like(postID)

            Log.d("dbStatus", "$postID liked")
            try {
                val response =
                    RetrofitApiClient.postsApiService
                        .likePost(LikeBody(true, postID))

                if (response.isSuccessful) {
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

    suspend fun getLikeStatus(postID: String): Boolean = postDao.likeStatus(postID)

    suspend fun getLikeCount(postID: String): Int = postDao.likeCount(postID)

    suspend fun getPendingLike(postId: String): PendingLike? =
        withContext(Dispatchers.IO) {
            pendingLikesDao.getByPostId(postId)
        }

    suspend fun addPendingLike(
        postId: String,
        liked: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            pendingLikesDao.addLike(PendingLike(postId, liked))
        }
    }

    suspend fun dislikePost(postID: String): Boolean =

        withContext(Dispatchers.IO) {
            postDao.dislike(postID)
            Log.d("dbStatus", "$postID disliked")
            try {
                val response = RetrofitApiClient.postsApiService.dislikePost()

                if (response.isSuccessful) {
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

    suspend fun getAllPendingLikes(): List<PendingLike> =
        withContext(Dispatchers.IO) {
            pendingLikesDao.fetchAll()
        }

    suspend fun removePendingLike(postId: String) {
        withContext(Dispatchers.IO) {
            pendingLikesDao.removeLike(postId)
        }
    }

    suspend fun removeAllLikes() {
        withContext(Dispatchers.IO) {
            pendingLikesDao.flush()
        }
    }
}
