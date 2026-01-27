package com.example.instalgam

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.example.instalgam.connectivity.NetworkObserver
import com.example.instalgam.repository.PostRepository
import com.example.instalgam.repository.PreferencesRepository
import com.example.instalgam.room.PendingLikeDatabase
import com.example.instalgam.room.PostDatabase
import com.example.instalgam.viewmodel.NavigationEvent
import com.example.instalgam.viewmodel.PostViewModel
import com.example.instalgam.viewmodel.ToastEvent
import kotlinx.coroutines.launch

class PostFeedActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var signOutButton: Button
    private lateinit var postAdapter: PostAdapter
    private lateinit var reelsButton: Button
    private lateinit var networkObserver: NetworkObserver
    private val viewModel: PostViewModel by viewModels {
        val sp = getSharedPreferences("loginStatus", Context.MODE_PRIVATE)
        val postRepository =
            PostRepository(
                PostDatabase.getInstance(applicationContext).postDao(),
                PendingLikeDatabase.getInstance(applicationContext).pendingLikesDao(),
            )
        val preferencesRepository = PreferencesRepository(sp)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PostViewModel(postRepository, preferencesRepository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.post_feed)

//        val data = intent.extras
//        if (data != null) {
//            val username: String? = data.getString("USER_USERNAME")
//            Toast.makeText(this, "$username has logged in!", Toast.LENGTH_SHORT).show()
//        }

        recyclerView = findViewById(R.id.recyclerView)
        postAdapter = PostAdapter(this, viewModel::onClickLike)
        recyclerView.adapter = postAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        signOutButton = findViewById(R.id.signOutButton)
        signOutButton.setOnClickListener {
            viewModel.onSignOutButtonClick()
        }

        reelsButton = findViewById(R.id.reels)
        reelsButton.setOnClickListener {
            viewModel.onReelsButtonClick()
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

        viewModel.posts.observe(this) {
            postAdapter.submitList(it)
        }

        viewModel.toastEvent.observe(this) { message ->
            message?.let {
                when (it) {
                    is ToastEvent.FailedToLoadPosts -> {
                        Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
                    }

                    is ToastEvent.ErrorOccured -> {
                        Toast.makeText(this, "An error occurred: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                    }

                    is ToastEvent.FailedToLikePost -> {
                        Toast.makeText(this, "Failed to like post", Toast.LENGTH_SHORT).show()
                    }

                    is ToastEvent.FailedToDislikePost -> {
                        Toast.makeText(this, "Failed to dislike post", Toast.LENGTH_SHORT).show()
                    }

                    is ToastEvent.NoConnection -> {
                        Toast.makeText(this, "No internet connection, loading posts from the Room database", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        networkObserver = NetworkObserver(applicationContext)
        observeNetworkStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.deleteAllPendingLikes()
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
}
