package com.example.instalgam

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instalgam.adapter.PostAdapter
import com.example.instalgam.apiClient.RetrofitApiClient
import com.example.instalgam.connectivity.NetworkObserver
import com.example.instalgam.model.Post
import com.example.instalgam.repository.PostRepository
import com.example.instalgam.room.DatabasePost
import com.example.instalgam.room.PendingLikeDatabase
import com.example.instalgam.room.PendingLikeDatabaseHelper
import com.example.instalgam.room.PostDatabase
import com.example.instalgam.room.PostDatabaseHelper
import com.example.instalgam.viewmodel.NavigationEvent
import com.example.instalgam.viewmodel.PostViewModel
import kotlinx.coroutines.launch

class PostFeedActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var signOutButton: Button
    private lateinit var postAdapter: PostAdapter
    private lateinit var reelsButton: Button
    private lateinit var dbHelper: PostDatabaseHelper
    private lateinit var pendingLikeDbHelper: PendingLikeDatabaseHelper
    private lateinit var networkObserver: NetworkObserver
    private val viewModel: PostViewModel by viewModels {
        val sp = getSharedPreferences("loginStatus", Context.MODE_PRIVATE)
        val repository = PostRepository(sp, PostDatabase.getInstance(applicationContext).postDao())
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PostViewModel(repository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.post_feed)

        val data = intent.extras
        if (data != null) {
            val username: String? = data.getString("USER_USERNAME")
            Toast.makeText(this, "$username has logged in!", Toast.LENGTH_SHORT).show()
        }

//        val db = PostDatabase.getInstance(applicationContext)
//        dbHelper = PostDatabaseHelper(db.postDao())

        val pendingLikeDb = PendingLikeDatabase.getInstance(applicationContext)
        pendingLikeDbHelper = PendingLikeDatabaseHelper(pendingLikeDb.pendingLikesDao())

        recyclerView = findViewById(R.id.recyclerView)
        postAdapter = PostAdapter(this, pendingLikeDbHelper)
        recyclerView.adapter = postAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        signOutButton = findViewById(R.id.signOutButton)
        signOutButton.setOnClickListener {
            viewModel.onSignOutButtonClick()
//            val sp =
//                getSharedPreferences(
//                    getString(R.string.shared_preferences_file_name),
//                    Context.MODE_PRIVATE,
//                )
//
//            val username = sp.getString(getString(R.string.logged_in_user), null)
//
//            sp.edit {
//                putString(getString(R.string.logged_in_user), null)
//            }

//            Toast.makeText(this, "Signing out of $username", Toast.LENGTH_SHORT).show()
        }

        reelsButton = findViewById(R.id.reels)
        reelsButton.setOnClickListener {
            viewModel.onReelsButtonClick()
//            val intent = Intent(this@PostFeedActivity, ReelsFeedActivity::class.java)
//            startActivity(intent)
        }

        viewModel.navigationEvent.observe(this) { navigation ->
            navigation?.let {
                when (it) {
                    is NavigationEvent.SignOut -> {
                        startActivity(Intent(this, MainActivity::class.java))
                    }

                    is NavigationEvent.Reels -> {
                        val intent = Intent(this@PostFeedActivity, ReelsFeedActivity::class.java)
                        startActivity(intent)
                    }
                }

                viewModel.navigationComplete()
            }
        }
        viewModel.posts.observe(this) { posts ->
            posts?.let {
                postAdapter.submitList(it)
            }
        }
        // Always fetch posts from the DB first
//        lifecycleScope.launch {
//            viewModel.fetchOfflinePosts()
//        }
//        checkConnectivityStatus()
        networkObserver = NetworkObserver(applicationContext)
        observeNetworkStatus()
    }

    fun observeNetworkStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkObserver.observe().collect { isConnected ->
                    viewModel.onNetworkStatusChanged(isConnected)
                }
            }
        }
    }
//    private fun observeNetworkStatus() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                var hasDisconnected = false
//                networkObserver.observe().collect { status ->
//                    if (!status) {
//                        hasDisconnected = true
//                    }
//                    if (status && hasDisconnected) {
//                        postAdapter.likeUnsyncedPosts()
//                        Toast
//                            .makeText(
//                                this@PostFeedActivity,
//                                "Network is available, syncing posts with database",
//                                Toast.LENGTH_SHORT,
//                            ).show()
//                        Log.d("networkStatus", "Network reconnected, syncing")
//                    }
//                }
//            }
//        }
//    }

//    private fun checkConnectivityStatus() {
//        val connectivityManager = getSystemService(ConnectivityManager::class.java)
//        if (connectivityManager.activeNetwork == null) {
//            Log.d("networkStatus", "Network is not available on startup")
//            Toast
//                .makeText(
//                    this@PostFeedActivity,
//                    "Device is not connected to a network. Loading posts from Room database",
//                    Toast.LENGTH_SHORT,
//                ).show()
// //            fetchPostsOffline()
//        } else {
//            Log.d("networkStatus", "Network is available")
//            lifecycleScope.launch {
//                fetchPostsOnline()
//            }
//        }
//    }

//    private suspend fun fetchPostsOnline() {
//        try {
//            val response = RetrofitApiClient.postsApiService.fetchPosts()
//            if (response.isSuccessful) {
//                val apiPosts = response.body()?.posts?.filterNotNull() ?: emptyList()
//                postAdapter.submitList(apiPosts)
//                val dbPosts =
//                    apiPosts.map {
//                        DatabasePost(
//                            it.postId,
//                            it.userName,
//                            it.profilePicture,
//                            it.postImage,
//                            it.likeCount,
//                            it.likedByUser,
//                        )
//                    }
//                Log.d("dbStatus", "Pushed ${dbPosts.size} posts into database")
//                dbHelper.savePosts(dbPosts)
//            } else {
//                Toast.makeText(this@PostFeedActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
// //                        fetchPostsOffline()
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this@PostFeedActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
// //                    fetchPostsOffline()
//            Log.e("apiStatus", e.message.toString())
//        }
//    }

//    private suspend fun fetchPostsOffline() {
//        val dbPosts = dbHelper.getPosts()
//        Log.d("dbStatus", "Loaded ${dbPosts.size} posts from database")
//
//        val posts =
//            dbPosts.map {
//                Post(
//                    it.postId,
//                    it.userName,
//                    it.profilePicture,
//                    it.postImage,
//                    it.likeCount,
//                    it.likedByUser,
//                )
//            }
//        postAdapter.submitList(posts)
//    }
}
