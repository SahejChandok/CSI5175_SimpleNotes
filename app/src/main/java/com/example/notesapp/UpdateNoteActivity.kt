package com.example.notesapp

import android.app.Activity
import android.app.Notification.Style
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.toSpannable
import com.example.notesapp.databinding.ActivityCreateNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

class UpdateNoteActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCreateNoteBinding
    private lateinit var note:Note
    private lateinit var old_note: Note
    var isUpdate = false
    private lateinit var submitButton: Button


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        submitButton = findViewById(R.id.create_note_submit)

        try{
            old_note=intent.getSerializableExtra("current_note") as Note
            binding.createNoteTitle.setText(old_note.title.toString())
            binding.createNoteBody.setText(old_note.note.toString())

            isUpdate= true
        }catch ( e : Exception){
            e.printStackTrace()
        }
        submitButton.setOnClickListener{
            val title= binding.createNoteTitle.text.toString()
            val note_desc= binding.createNoteBody.text.toString()
            if (title.isNotEmpty() || note_desc.isNotEmpty()){
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
                if(isUpdate){
                    note = Note(
                        old_note.id, title, note_desc, formatter.format(Date())
                    )
                }else{
                    note = Note(
                        null, title, note_desc, formatter.format(Date())
                    )
                }
                val intent= Intent()
                intent.putExtra("note", note)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }else{
                Toast.makeText(this@UpdateNoteActivity, "Please enter some data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

    }
    /*private lateinit var appDatabase: AppDatabase
    private lateinit var title: TextView
    private lateinit var body: TextView
    private lateinit var submitButton: Button
    private lateinit var boldBtn: ImageButton
    private lateinit var italicBtn: ImageButton
    private lateinit var underlineBtn: ImageButton
    private lateinit var centerAlignButton: ImageButton
    private lateinit var leftAlignButton: ImageButton
    private lateinit var rightAlignButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)
        appDatabase = AppDatabase.getDatabase(this)
        title = findViewById(R.id.create_note_title)
        body = findViewById(R.id.create_note_body)
        submitButton = findViewById(R.id.create_note_submit)
        boldBtn = findViewById(R.id.bold_button)
        underlineBtn = findViewById(R.id.underline_button)
        italicBtn = findViewById(R.id.italic_button)
        centerAlignButton = findViewById(R.id.center_align_button)
        leftAlignButton = findViewById(R.id.left_align_button)
        rightAlignButton = findViewById(R.id.right_align_button)
        boldBtn.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            str.setSpan(StyleSpan(Typeface.BOLD), body.selectionStart, body.selectionEnd, 0)
            body.text = str

        }
        italicBtn.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            str.setSpan(StyleSpan(Typeface.ITALIC), body.selectionStart, body.selectionEnd, 0)
            body.text = str

        }
        italicBtn.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            str.setSpan(UnderlineSpan(), body.selectionStart, body.selectionEnd, 0)
            body.text = str

        }
        centerAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_CENTER
            if(body.textAlignment == View.TEXT_ALIGNMENT_CENTER) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(body.text)
            body.textAlignment = textAlignment
            body.text = spannableStr
        }
        leftAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            if(body.textAlignment == View.TEXT_ALIGNMENT_TEXT_START) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(body.text)
            body.textAlignment = textAlignment
            body.text = spannableStr
        }
        rightAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            if(body.textAlignment == View.TEXT_ALIGNMENT_TEXT_END) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(body.text)
            body.textAlignment = textAlignment
            body.text = spannableStr
        }
        submitButton.setOnClickListener {
            createNote()
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        createNote()
        super.onDestroy()
    }

    private fun createNote() {
        if(body.text.toString().isNotEmpty() && title.text.toString().isNotEmpty())  {
            val str = SpannableStringBuilder(body.text)
            val note = Note(null, title.text.toString(), Html.toHtml(str, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE))
            GlobalScope.launch(Dispatchers.IO) {
                appDatabase.getNoteDao().insert(note)
            }
        }
    }*/

}