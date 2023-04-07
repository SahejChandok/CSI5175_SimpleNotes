package com.example.notesapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Database
import androidx.viewbinding.ViewBinding
import com.example.notesapp.AppDatabase
import com.example.notesapp.Note
import com.example.notesapp.NoteViewModel

import com.example.notesapp.adapter.NotesAdapter
import com.example.notesapp.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() , NotesAdapter.NotesClickListener, PopupMenu.OnMenuItemClickListener{
    private lateinit var database: AppDatabase
    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: NoteViewModel
    lateinit var adapter: NotesAdapter
    lateinit var selectedNote: Note
    private val updateNote= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

        if (result.resultCode== Activity.RESULT_OK){
            val note= result.data?.getSerializableExtra("note") as? Note
            if(note!=null){
                viewModel.updateNote(note)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()

        viewModel = ViewModelProvider( this,
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoteViewModel::class.java)

        viewModel.allnotes.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        database = AppDatabase.getDatabase(this)
        /*val addButton = findViewById<FloatingActionButton>(R.id.add_notes)
        addButton.setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivity(intent)
        }*/

    }

    private fun initUI() {
      binding.noteList.setHasFixedSize(true)
        binding.noteList.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        adapter = NotesAdapter(this, this)
        binding.noteList.adapter = adapter

        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if (result.resultCode == Activity.RESULT_OK){

                val note= result.data?.getSerializableExtra("note") as? Note

                if (note!= null){
                    viewModel.insertNote(note)
                }
                }
        }

        binding.addNotes.setOnClickListener{
            val intent = Intent(this, CreateNoteActivity::class.java)
            getContent.launch(intent)

        }

        binding.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!=null){
                    adapter.filterList(newText)
                }

                return true
            }

        })
    }

    override fun onItemClicked(note: Note) {
        val intent = Intent(this@MainActivity, CreateNoteActivity::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }
    private fun popUpDisplay(cardView: CardView){
        val popup = PopupMenu(this, cardView)
        popup.setOnMenuItemClickListener(this@MainActivity)
        popup.inflate(R.menu.pop_up_menu)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
       if (item?.itemId == R.id.delete_note){
           viewModel.deleteNote(selectedNote)
           return true
       }
        return false
    }


}

