package com.example.notesapp

import android.app.Activity
import android.app.Notification.Style
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ActivityUpdateNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

class UpdateNoteActivity: AppCompatActivity() {
    private lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var updatedNote:Note
    private lateinit var oldNote: Note
    private lateinit var appDatabase: AppDatabase
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
    private val CHANNEL_ID = "notes_notifications"
    private lateinit var notificationManager: NotificationManager
    private val getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val noteImg = NoteImage(null, uri.toString(), oldNote.id)
            noteImages.add(noteImg)
            noteImagesAdapter.update(noteImages)
        }
    private val bodyTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(bodyText: Editable?) {
            submitButton.isEnabled =
                (bodyText?.isNotEmpty() == true && binding.updateNoteTitle.text.isNotEmpty())

        }
    }
    private val titleTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(titleText: Editable?) {
            submitButton.isEnabled =
                titleText?.isNotEmpty() == true && binding.updateNoteBody.text.isNotEmpty()
        }
    }


    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        appDatabase = AppDatabase.getDatabase(this)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        submitButton = binding.updateNoteSubmit

        try{
            oldNote =intent.getSerializableExtra("current_note") as Note
            val body = SpannableString(Html.fromHtml(oldNote.note, Html.FROM_HTML_MODE_COMPACT))
            binding.updateNoteTitle.setText(oldNote.title.toString())
            binding.updateNoteBody.setText(body)
            val fetchedNoteImages = oldNote.id?.let { appDatabase.noteImageDao().getAllForNote(it) }
            if (fetchedNoteImages != null) {
                if(fetchedNoteImages.isNotEmpty()) {
                    noteImages.addAll(fetchedNoteImages)
                }
            }
            isUpdate= true
            binding.addNoteToPush.isChecked = oldNote.isPush == true
        }catch ( e : Exception){
            e.printStackTrace()
        }
        binding.removeFormatButton.setOnClickListener {
            val str = SpannableStringBuilder(binding.updateNoteBody.text)
            val spans = str.getSpans(0, str.length, Objects::class.java)
            spans.forEach { span -> str.removeSpan(span) }
            binding.updateNoteBody.text = str
        }
        submitButton.setOnClickListener{
            val title= binding.updateNoteTitle.text.toString()
            val note_desc= binding.updateNoteBody.text.toString()
            var result = 0
            if (title.isNotEmpty() && note_desc.isNotEmpty()){
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
                val str = SpannableStringBuilder(note_desc)
                val ctx = this
                GlobalScope.launch(Dispatchers.IO) {
                    if(isUpdate){
                        updatedNote = Note(
                            oldNote.id, title,
                            Html.toHtml(str, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL),
                            formatter.format(Date()),
                            pushCheckBox.isChecked
                        )
                        result = appDatabase.noteDao().update(updatedNote)
                    }
                    if(result != 0) {
                        val intent= Intent()
                        intent.putExtra("note", updatedNote)
                        setResult(Activity.RESULT_OK, intent)

                        var imagesToBeAdded = noteImages.filter { img -> img.id == null }
                        if(imagesToBeAdded.isNotEmpty()) {
                            imagesToBeAdded.forEach{ img -> img.noteId = updatedNote.id }
                            appDatabase.noteImageDao().insert(imagesToBeAdded)
                        }
                        if(imageIdsToBeRemoved.isNotEmpty()) {
                            appDatabase.noteImageDao().deleteImages(imageIdsToBeRemoved)
                        }
                        val content = Html.fromHtml(updatedNote.note.toString(), Html.FROM_HTML_MODE_COMPACT)
                        val pendingIntent = Intent(ctx, UpdateNoteActivity::class.java)
                        pendingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        pendingIntent.putExtra("current_note", updatedNote)
                        if (pushCheckBox.isChecked) {
                            val bigTextStyle = NotificationCompat.BigTextStyle()
                                .setBigContentTitle(updatedNote.title)
                                .bigText(content)
                            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_note_notification) // change this to icon
                                .setContentTitle(updatedNote.title)
                                .setContentText(content)
                                .setStyle(bigTextStyle)
                                .setAutoCancel(true)
                                .setContentIntent(PendingIntent.getActivity(
                                    ctx, 0, pendingIntent, PendingIntent.FLAG_IMMUTABLE
                                ))
                            if (noteImages.isNotEmpty()) {
                                val img = noteImages[0]
                                val imageStream =
                                    ctx.contentResolver.openInputStream(Uri.parse(img.uri.toString()))
                                val imgBitmap = BitmapFactory.decodeStream(imageStream)
                                builder.setLargeIcon(imgBitmap)
                            }
                            val notification = builder.build()
                            // update the notification
                            updatedNote.id?.let { it1 -> notificationManager.notify(it1, notification) }
                        } else {
//                          remove the notification
                            updatedNote.id?.let { it1 -> notificationManager.cancel(it1) }
                        }

                    }

                    finish()

                }

            }else{
                Toast.makeText(this@UpdateNoteActivity, "Please enter some data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
        boldBtn = findViewById(R.id.bold_button)
        underlineBtn = findViewById(R.id.underline_button)
        italicBtn = findViewById(R.id.italic_button)
        centerAlignButton = findViewById(R.id.center_align_button)
        leftAlignButton = findViewById(R.id.left_align_button)
        rightAlignButton = findViewById(R.id.right_align_button)
        todoListButton = findViewById(R.id.checklist_button)
        todoCheckButton = findViewById(R.id.strike_button)
        pushCheckBox = findViewById(R.id.add_note_to_push)
        imageButton = findViewById(R.id.attach_image_button)
        binding.updateNoteBody.addTextChangedListener(bodyTextWatcher)
        binding.updateNoteTitle.addTextChangedListener(titleTextWatcher)
        imagesRecyclerView = findViewById(R.id.images_view)
        imagesRecyclerView.setHasFixedSize(true)
        val onClickImgClose: (NoteImage, Int) -> Unit = { noteImage: NoteImage, position: Int ->
            val imgId = noteImage.id
            if (imgId != null) {
                imageIdsToBeRemoved.add(imgId)
            }
            noteImages.removeAt(position)
            noteImagesAdapter.update(noteImages)
        }
        noteImagesAdapter = NoteImagesAdapter(this, noteImages, onClickImgClose)
        imagesRecyclerView.adapter = noteImagesAdapter
        imagesViewManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesRecyclerView.layoutManager = imagesViewManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        imageButton.setOnClickListener {
            getImageContent.launch("image/*")
        }
        boldBtn.setOnClickListener {
            val str = SpannableStringBuilder(binding.updateNoteBody.text)
            str.setSpan(StyleSpan(Typeface.BOLD), binding.updateNoteBody.selectionStart, binding.updateNoteBody.selectionEnd, 0)
            binding.updateNoteBody.text = str

        }
        italicBtn.setOnClickListener {
            val str = SpannableStringBuilder(binding.updateNoteBody.text)
            str.setSpan(StyleSpan(Typeface.ITALIC), binding.updateNoteBody.selectionStart, binding.updateNoteBody.selectionEnd, 0)
            binding.updateNoteBody.text = str

        }
        underlineBtn.setOnClickListener {
            val str = SpannableStringBuilder(binding.updateNoteBody.text)
            str.setSpan(UnderlineSpan(), binding.updateNoteBody.selectionStart, binding.updateNoteBody.selectionEnd, 0)
            binding.updateNoteBody.text = str

        }
        centerAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_CENTER
            if (binding.updateNoteBody.textAlignment == View.TEXT_ALIGNMENT_CENTER) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(binding.updateNoteBody.text)
            binding.updateNoteBody.textAlignment = textAlignment
            binding.updateNoteBody.text = spannableStr
        }
        leftAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            if (binding.updateNoteBody.textAlignment == View.TEXT_ALIGNMENT_TEXT_START) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(binding.updateNoteBody.text)
            binding.updateNoteBody.textAlignment = textAlignment
            binding.updateNoteBody.text = spannableStr
        }
        rightAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            if (binding.updateNoteBody.textAlignment == View.TEXT_ALIGNMENT_TEXT_END) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(binding.updateNoteBody.text)
            binding.updateNoteBody.textAlignment = textAlignment
            binding.updateNoteBody.text = spannableStr
        }

        todoCheckButton.setOnClickListener {
            var str = SpannableStringBuilder(binding.updateNoteBody.text)
            str.setSpan(
                StrikethroughSpan(),
                binding.updateNoteBody.selectionStart,
                binding.updateNoteBody.selectionEnd,
                0
            )
            str.setSpan(
                BackgroundColorSpan(Color.BLUE),
                binding.updateNoteBody.selectionStart,
                binding.updateNoteBody.selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.updateNoteBody.text = str
        }

        todoListButton.setOnClickListener{
            val startSelection: Int = binding.updateNoteBody.selectionStart
            val endSelection: Int = binding.updateNoteBody.selectionEnd
            val selectedText:String = binding.updateNoteBody.text.substring(startSelection, endSelection)
            val textBefore:String = binding.updateNoteBody.text.substring(0,startSelection)
            val textAfter:String = binding.updateNoteBody.text.substring(endSelection,
                binding.updateNoteBody.text.length)
            var str = SpannableStringBuilder(selectedText)
            var bulletHollow= "\u25CB "
            var result = str.contains(bulletHollow)
            if (result){
                val n = 2
                binding.updateNoteBody.setText(textBefore + str.substring(n) +textAfter)
            } else {
                binding.updateNoteBody.setText(textBefore + bulletHollow + str +"\u000A" +textAfter)
            }

        }

    }

}