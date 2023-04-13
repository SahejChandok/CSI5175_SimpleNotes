package com.example.notesapp.adapter

import android.content.Context
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.Note
import com.example.notesapp.R
import javax.xml.transform.ErrorListener
import kotlin.random.Random

class NotesAdapter(private val context: Context, val listener: NotesClickListener) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>(){
    private val NotesList = ArrayList<Note>()
    private val fullList = ArrayList<Note>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(context).inflate(R.layout.activity_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
       return NotesList.size
    }
    fun randomColor() : Int{
        val list = ArrayList<Int>()
        list.add(R.color.NoteColor1)
        list.add(R.color.NoteColor2)
        list.add(R.color.NoteColor3)
        list.add(R.color.NoteColor4)
        list.add(R.color.NoteColor5)
        list.add(R.color.NoteColor6)
        val seed= System.currentTimeMillis().toInt()
        val randomIndex= Random(seed).nextInt(list.size)
        return list[randomIndex]
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote= NotesList[position]
        holder.title.text= currentNote.title
        holder.title.isSelected= true
        holder.nnote.text= SpannableString(Html.fromHtml(currentNote.note,
        Html.FROM_HTML_MODE_LEGACY))
        holder.date.text= currentNote.date
        holder.date.isSelected = true
        holder.notes_layout.setCardBackgroundColor(holder.itemView.resources.getColor(randomColor(), null))

        holder.notes_layout.setOnClickListener{
            listener.onItemClicked(NotesList[holder.adapterPosition])
        }

        holder.notes_layout.setOnLongClickListener{
            listener.onLongItemClicked(NotesList[holder.adapterPosition], holder.notes_layout)
            true
        }


    }

    fun updateList(newList : List<Note>){

        fullList.clear()
        fullList.addAll(newList)
        NotesList.clear()
        NotesList.addAll(fullList)

        notifyDataSetChanged()



    }

    fun filterList(search : String){
        NotesList.clear()

        for(item in fullList){
            if(item.title?.lowercase()?.contains(search.lowercase()) == true ||
                    item.note?.lowercase()?.contains(search.lowercase()) == true){
                NotesList.add(item)
            }
        }

        notifyDataSetChanged()
    }



    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val notes_layout = itemView.findViewById<CardView>(R.id.card_layout)
        val title= itemView.findViewById<TextView>(R.id.n_title)
        val nnote =itemView.findViewById<TextView>(R.id.n_note)
        val date = itemView.findViewById<TextView>(R.id.n_date)
    }

    interface NotesClickListener{
        fun onItemClicked(note: Note)
        fun onLongItemClicked(note: Note, cardView: CardView)
    }

}


