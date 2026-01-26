package com.example.instalgam.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.example.instalgam.R
import com.example.instalgam.model.Post
import com.example.instalgam.room.PendingLikeDatabaseHelper

class PostAdapter(
    val context: Context,
    private val onClickLike: (String, Int) -> Unit,
) : RecyclerView.Adapter<PostAdapter.Holder>() {
    private val diffUtil =
        object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean = oldItem.postId == newItem.postId

            override fun areContentsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean = oldItem == newItem
        }

    private val differ = AsyncListDiffer(this, diffUtil)

    fun submitList(data: List<Post>) {
        differ.submitList(data)
    }

    val currentList: List<Post>
        get() = differ.currentList

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): Holder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.post_layout, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int,
    ) {
        val post = differ.currentList[position]
        holder.likeCount.text = post.likeCount.toString()

        if (post.likedByUser) {
            holder.likeButton.setImageResource(R.drawable.liked_heart)
        } else {
            holder.likeButton.setImageResource(R.drawable.unliked_heart)
        }

        holder.username.text = post.userName

        holder.pfpImage.load(post.profilePicture) {
            transformations(CircleCropTransformation())
        }
        holder.postImage.load(post.postImage)
        holder.shareButton.setImageResource(R.drawable.share)
        holder.commentButton.setImageResource(R.drawable.comment)

        holder.likeButton.setOnClickListener {
            onClickLike(post.postId, position)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

//    fun likeUnsyncedPosts() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val pendingLikes = pendingLikeDbHelper.getAllPendingLikes()
//            Log.d("pendingLikes", "Size of pending likes: ${pendingLikes.size}")
//
//            for (pending in pendingLikes) {
//                Log.d("apiStatus", "Trying like for: ${pending.postId}")
//                try {
//                    val response =
//                        if (pending.liked) {
//                            RetrofitApiClient.postsApiService.likePost(
//                                LikeBody(true, pending.postId),
//                            )
//                        } else {
//                            RetrofitApiClient.postsApiService.dislikePost()
//                        }
//
//                    if (response.isSuccessful) {
//                        pendingLikeDbHelper.removePendingLike(pending.postId)
//                        Log.d("apiStatus", "Synced like: ${pending.postId}")
//                    }
//                } catch (e: Exception) {
//                    Log.e("apiStatus", "Sync failed", e)
//                }
//            }
//        }
//    }

    inner class Holder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.usernameText)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val likeCount: TextView = view.findViewById(R.id.likeCountText)
        val postImage: ImageView = view.findViewById(R.id.postPicture)
        val pfpImage: ImageView = view.findViewById(R.id.profilePictureImage)
        val commentButton: ImageView = view.findViewById(R.id.comment)
        val shareButton: ImageView = view.findViewById(R.id.share)
    }
}
