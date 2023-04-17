package com.example.notesapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.adapter.NotesAdapter
import com.example.notesapp.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(),
    NotesAdapter.NotesClickListener, PopupMenu.OnMenuItemClickListener {
    lateinit var appDatabase: AppDatabase
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 101
    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: NoteViewModel
    lateinit var adapter: NotesAdapter
    lateinit var selectedNote: Note

    private var flagSwitch:Boolean = false

    private val updateNote =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val note = result.data?.getSerializableExtra("note") as? Note
                if (note != null) {
                    viewModel.updateNote(note)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        appDatabase = AppDatabase.getDatabase(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
                )
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    102
                )
            }
        }
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        viewModel.allnotes.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                    findViewById(R.id.main_activity_content),
                    "Read External Storage Required to Add Images",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initUI() {
        binding.noteList.setHasFixedSize(true)
        binding.noteList.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        adapter = NotesAdapter(this, this)
        binding.noteList.adapter = adapter
        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val note = result.data?.getSerializableExtra("note") as? Note

                    if (note != null) {
//                        viewModel.insertNote(note)
                        var updatedList = arrayListOf<Note>(note)
                        var noteList = viewModel.allnotes.value as ArrayList<Note>
                        updatedList.addAll(noteList)
                        adapter.updateList(updatedList)
                    }
                }
            }

        binding.addNotes.setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            getContent.launch(intent)

        }

        binding.shareNotes.setOnClickListener {
            binding.shareNotes.animate().rotation(180f)
            if(flagSwitch){
                flagSwitch = false
                binding.noteList.setHasFixedSize(true)
                binding.noteList.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
                adapter = NotesAdapter(this, this)
                binding.noteList.adapter = adapter
            }else{
                flagSwitch = true
                binding.noteList.setHasFixedSize(true)
                binding.noteList.layoutManager = LinearLayoutManager(this)
                adapter = NotesAdapter(this, this)
                binding.noteList.adapter = adapter
            }
            viewModel.allnotes.observe(this) { list ->
                list?.let {
                    adapter.updateList(list)
                }
            }
        }

        binding.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapter.filterList(newText)
                }

                return true
            }

        })
    }

    override fun onItemClicked(note: Note) {
        val intent = Intent(this@MainActivity, UpdateNoteActivity::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }

    private fun popUpDisplay(cardView: CardView) {
        val popup = PopupMenu(this, cardView)
        popup.setOnMenuItemClickListener(this@MainActivity)
        popup.inflate(R.menu.pop_up_menu)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.delete_note) {
            viewModel.deleteNote(selectedNote)
            return true
        }
        if (item?.itemId == R.id.share_note) {
            try {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, "${selectedNote.title}\n${selectedNote.date} \ncontent: ${  SpannableString(Html.fromHtml(selectedNote.note,
                    Html.FROM_HTML_MODE_COMPACT))
                }")
                intent.type = "text/plain"
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Press and long hold to select a note", Toast.LENGTH_SHORT).show()
            }
            return true
        }

        return false
    }
}
