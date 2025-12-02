package com.example.buddyshare.ui.home.sendfiles.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddyshare.R
import com.example.buddyshare.databinding.ItemSelectedFileBinding
import com.example.buddyshare.ui.home.sendfiles.model.SelectedFilesModel

class SelectedFilesAdapter(private val files: List<SelectedFilesModel>) :
    RecyclerView.Adapter<SelectedFilesAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemSelectedFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemSelectedFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.binding.fileName.text = file.name

        // Use Glide to load the thumbnail
        Glide.with(holder.itemView.context)
            .load(file.uri) // Glide can load thumbnails directly from a Uri
            .placeholder(R.drawable.ic_launcher_foreground) // Generic fallback icon
            .error(R.drawable.ic_launcher_foreground)       // Icon for non-media files
            .into(holder.binding.fileThumbnail)
    }
}
