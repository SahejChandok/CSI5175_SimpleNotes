package com.example.notesapp

import android.app.Activity
import android.app.Notification.Style
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.*
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ActivityCreateNoteBinding
import com.example.notesapp.databinding.ActivityUpdateNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

class UpdateNoteActivity: AppCompatActivity() {
    private lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var note:Note
    private lateinit var body: TextView
    private lateinit var old_note: Note
    var isUpdate = false
    private lateinit var submitButton: Button
    private lateinit var boldBtn: ImageButton
    private lateinit var italicBtn: ImageButton
    private lateinit var underlineBtn: ImageButton
    private lateinit var centerAlignButton: ImageButton
    private lateinit var leftAlignButton: ImageButton
    private lateinit var rightAlignButton: ImageButton
    private lateinit var todoListButton: ImageButton
    private lateinit var todoCheckButton: ImageButton
    private lateinit var pushCheckBox: CheckBox
    private lateinit var imageButton: ImageButton
    private var noteImages = arrayListOf<NoteImage>()
    private var imageIdsToBeRemoved = arrayListOf<Int>()
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var imagesViewManager: LinearLayoutManager
    private lateinit var noteImagesAdapter: NoteImagesAdapter
    private val getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val noteImg = NoteImage(null, uri.toString(), null)
            noteImages.add(noteImg)
            noteImagesAdapter.update(noteImages)
        }


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        body=binding.createNoteBody

        val onClickImgClose: (NoteImage, Int) -> Unit = { noteImage: NoteImage, position: Int ->
            val imgId = noteImage.id
            if (imgId != null) {
                imageIdsToBeRemoved.add(imgId)
            }
            noteImages.removeAt(position)
            noteImagesAdapter.update(noteImages)
        }

        try{
            old_note=intent.getSerializableExtra("current_note") as Note
            binding.createNoteTitle.setText(old_note.title.toString())
            val body = SpannableString(Html.fromHtml(old_note.note, Html.FROM_HTML_MODE_LEGACY))
            binding.createNoteBody.setText(body)
            isUpdate= true
        }catch ( e : Exception){
            e.printStackTrace()
        }
        submitButton = findViewById(R.id.create_note_submit)
        boldBtn = findViewById(R.id.bold_button)
        underlineBtn = findViewById(R.id.underline_button)
        italicBtn = findViewById(R.id.italic_button)
        centerAlignButton = findViewById(R.id.center_align_button)
        leftAlignButton = findViewById(R.id.left_align_button)
        rightAlignButton = findViewById(R.id.right_align_button)
        todoListButton = findViewById(R.id.checklist_button)
        todoCheckButton = findViewById(R.id.strike_button)
        imageButton = findViewById(R.id.attach_image_button)
        pushCheckBox = findViewById(R.id.add_note_to_push)
        imagesRecyclerView = findViewById(R.id.images_view)
        imagesRecyclerView.setHasFixedSize(true)
        noteImagesAdapter = NoteImagesAdapter(this, noteImages, onClickImgClose)
        imagesRecyclerView.adapter = noteImagesAdapter
        imagesViewManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesRecyclerView.layoutManager = imagesViewManager
        imageButton.setOnClickListener {
            getImageContent.launch("image/*")
        }
        boldBtn.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            str.setSpan(StyleSpan(Typeface.BOLD), body.selectionStart, body.selectionEnd, 0)
            body.text = str
            /* val str = SpannableStringBuilder(body.text)
            var tempStr = body.text.toString()
            var subStr = tempStr.subSequence(body.selectionStart, body.selectionEnd).toString()
            tempStr.replace(subStr, "<strong>" + subStr + "</strong>")
            str.setSpan(StyleSpan(Typeface.BOLD), body.selectionStart, body.selectionEnd, 0)
            body.text = tempStr */

        }
        italicBtn.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            str.setSpan(StyleSpan(Typeface.ITALIC), body.selectionStart, body.selectionEnd, 0)
            body.text = str

        }
        underlineBtn.setOnClickListener {
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
        todoCheckButton.setOnClickListener {
            var str = SpannableStringBuilder(body.text)
            str.setSpan(
                StrikethroughSpan(),
                body.selectionStart,
                body.selectionEnd,
                0
            )
            str.setSpan(
                BackgroundColorSpan(Color.BLUE),
                body.selectionStart,
                body.selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            body.text = str
        }

        todoListButton.setOnClickListener {
            var bulletStr = SpannableStringBuilder(body.text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bulletStr.setSpan(
                    BulletSpan(40, Color.BLUE, 20),
                    body.selectionStart,
                    body.selectionEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            body.text = bulletStr
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
    /*

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