package br.com.instagram.profile.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.instagram.R
import br.com.instagram.common.model.Post
import com.bumptech.glide.Glide

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    var items: List<Post> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_grid, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(items[position].photoUrl)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(photoUrl: String?) {
            Glide.with(itemView.context).load(photoUrl)
                .into(itemView.findViewById(R.id.item_profile_img_grid))
        }
    }

}