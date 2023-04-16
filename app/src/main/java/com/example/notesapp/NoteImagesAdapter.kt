package com.example.notesapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


class NoteImagesAdapter(
    private val context: Context,
    private var noteImageList: ArrayList<NoteImage>,
    private var onClickClose: (model: NoteImage, idx: Int) -> Unit
) :
    RecyclerView.Adapter<NoteImagesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.findViewById(R.id.note_image)
        val closeBtn: ImageButton = itemView.findViewById(R.id.image_remove_btn)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.note_image_list_item,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return noteImageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = noteImageList[position]
        val uri = Uri.parse(currentItem.uri)
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val imageStream = context.contentResolver.openInputStream(uri)
        val img = BitmapFactory.decodeStream(imageStream)
        holder.image.setImageBitmap(img)
        holder.closeBtn.setOnClickListener {
            onClickClose(currentItem, position)
        }
    }

    fun update(imageList: ArrayList<NoteImage>) {
        noteImageList = imageList
        notifyDataSetChanged()
    }

}