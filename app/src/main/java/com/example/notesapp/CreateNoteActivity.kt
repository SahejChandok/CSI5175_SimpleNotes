package com.example.notesapp


import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ActivityCreateNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteActivity : AppCompatActivity() {
    private lateinit var appDatabase: AppDatabase
    private lateinit var binding: ActivityCreateNoteBinding
    private lateinit var old_note: Note
    var isUpdate = false
    var changesSaved = false
    private lateinit var title: TextView
    private lateinit var body: TextView
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
    private lateinit var removeFormatButton: ImageButton
    private val CHANNEL_ID = "notes_notifications"
    private lateinit var notificationManager: NotificationManager
    private val getImageContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data == null) return@registerForActivityResult
                val uri = result.data!!.data!!
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val noteImg = NoteImage(null, uri.toString(), null)
                noteImages.add(noteImg)
                noteImagesAdapter.update(noteImages)
                if(noteImages.size == 1) {
                    imagesRecyclerView.visibility = View.VISIBLE
                }
            }

        }
    private val bodyTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(bodyText: Editable?) {
            submitButton.isEnabled =
                (bodyText?.isNotEmpty() == true && title.text.isNotEmpty())

        }
    }
    private val titleTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(titleText: Editable?) {
            submitButton.isEnabled =
                titleText?.isNotEmpty() == true && body.text.isNotEmpty()
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        appDatabase = AppDatabase.getDatabase(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val onClickImgClose: (NoteImage, Int) -> Unit = { noteImage: NoteImage, position: Int ->
            val imgId = noteImage.id
            if (imgId != null) {
                imageIdsToBeRemoved.add(imgId)
            }
            noteImages.removeAt(position)
            noteImagesAdapter.update(noteImages)
            if(noteImages.isEmpty()) {
                imagesRecyclerView.visibility = View.GONE
            }
        }
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
        todoListButton = findViewById(R.id.checklist_button)
        todoCheckButton = findViewById(R.id.strike_button)
        pushCheckBox = findViewById(R.id.add_note_to_push)
        imageButton = findViewById(R.id.attach_image_button)
        body.addTextChangedListener(bodyTextWatcher)
        title.addTextChangedListener(titleTextWatcher)
        imagesRecyclerView = findViewById(R.id.images_view)
        imagesRecyclerView.setHasFixedSize(true)
        noteImagesAdapter = NoteImagesAdapter(this, noteImages, onClickImgClose)
        imagesRecyclerView.adapter = noteImagesAdapter
        imagesViewManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesRecyclerView.layoutManager = imagesViewManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        removeFormatButton = findViewById(R.id.remove_format_button)
        imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            getImageContent.launch(intent)
        }
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
        underlineBtn.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            str.setSpan(UnderlineSpan(), body.selectionStart, body.selectionEnd, 0)
            body.text = str

        }
        centerAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_CENTER
            if (body.textAlignment == View.TEXT_ALIGNMENT_CENTER) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(body.text)
            body.textAlignment = textAlignment
            body.text = spannableStr
        }
        leftAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            if (body.textAlignment == View.TEXT_ALIGNMENT_TEXT_START) {
                textAlignment = View.TEXT_ALIGNMENT_INHERIT
            }
            val spannableStr = SpannableStringBuilder(body.text)
            body.textAlignment = textAlignment
            body.text = spannableStr
        }
        rightAlignButton.setOnClickListener {
            var textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            if (body.textAlignment == View.TEXT_ALIGNMENT_TEXT_END) {
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
            body.text = str
        }

        todoListButton.setOnClickListener{
            val startSelection: Int = body.selectionStart
            val endSelection: Int = body.selectionEnd
            val selectedText:String = body.text.substring(startSelection, endSelection)
            val textBefore:String = body.text.substring(0,startSelection)

            val textAfter:String = body.text.substring(endSelection,body.text.length)
            var str = SpannableStringBuilder(selectedText)
            var bulletHollow= "\u25CB "
            var result = str.contains(bulletHollow)
            if (result){
                val n = 2
                body.text = textBefore + str.substring(n) +textAfter
            } else {
                body.text = textBefore + bulletHollow + str +"\u000A" +textAfter
            }

        }
        removeFormatButton.setOnClickListener {
            val str = SpannableStringBuilder(body.text)
            var spans = str.getSpans(0, str.length, StyleSpan::class.java)
            var strikeThrough = str.getSpans(0, str.length, StrikethroughSpan::class.java)
            var underline = str.getSpans(0, str.length, UnderlineSpan::class.java)
            spans.forEach { span -> str.removeSpan(span) }
            strikeThrough.forEach { span -> str.removeSpan(span) }
            underline.forEach { span -> str.removeSpan(span) }
            body.textAlignment = TextView.TEXT_ALIGNMENT_INHERIT
            body.text = str
        }
        submitButton.setOnClickListener {
            createNote()
        }
        createNotificationChannel()
        if(noteImages.isEmpty()) {
            imagesRecyclerView.visibility = View.GONE
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel.
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun createNote() {
        val str = SpannableStringBuilder(body.text)
        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
        var htmlStr = Html.toHtml(str, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        val alignment = getAlignmentStyle()
        htmlStr = "<p style= 'text-align: $alignment'>$htmlStr</p>"
        val note = Note(
            null,
            title.text.toString(),
            htmlStr,
            formatter.format(Date()),
            pushCheckBox.isChecked
        )
        val ctx = this
        var noteId = 0L
        GlobalScope.launch(Dispatchers.IO) {
            noteId = appDatabase.noteDao().insert(note)
            if (noteId != -1L && noteImages.isNotEmpty()) {
                noteImages.forEach{ img -> img.noteId = noteId.toInt()}
                val res = appDatabase.noteImageDao().insert(noteImages)
            }
            if (noteId != -1L && imageIdsToBeRemoved.isNotEmpty()) {
                appDatabase.noteImageDao().deleteImages(imageIdsToBeRemoved)
            }
            val content = Html.fromHtml(note.note.toString(), Html.FROM_HTML_MODE_LEGACY)
            if (noteId != -1L && pushCheckBox.isChecked) {
                note.id = noteId.toInt()
                val pendingIntent = Intent(ctx, UpdateNoteActivity::class.java)
                pendingIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                pendingIntent.putExtra("current_note", note)
                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .setBigContentTitle(note.title)
                    .bigText(content)
                val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_note_notification) // change this to icon
                    .setContentTitle(note.title)
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
                notificationManager.notify(noteId.toInt(), notification)
            }
            val intent = Intent(ctx, MainActivity::class.java)
            if(noteId != -1L) {
                intent.putExtra("note", note)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
    private fun getAlignmentStyle(): String {
        val alignment = when (body.textAlignment) {
            TextView.TEXT_ALIGNMENT_TEXT_END -> "right"
            TextView.TEXT_ALIGNMENT_CENTER -> "center"
            TextView.TEXT_ALIGNMENT_TEXT_START -> "left"
            else -> "left"
        }
        return alignment
    }
}