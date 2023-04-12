package com.example.notesapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.text.getSpans
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateNoteActivity : AppCompatActivity() {
    private lateinit var appDatabase: AppDatabase
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
    private val CHANNEL_ID = "notes_notifications"
    private lateinit var notificationManager: NotificationManager
    private val getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val noteImg = NoteImage(null, uri.toString(), null)
            noteImages.add(noteImg)
            noteImagesAdapter.update(noteImages)
        }
    private val bodyTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(bodyText: Editable?) {
            submitButton.isEnabled = bodyText?.isNotEmpty() == true

        }

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onClickImgClose: (NoteImage, Int) -> Unit = { noteImage: NoteImage, position: Int ->
            val imgId = noteImage.id
            if (imgId != null) {
                imageIdsToBeRemoved.add(imgId)
            }
            noteImages.removeAt(position)
            noteImagesAdapter.update(noteImages)
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
        imagesRecyclerView = findViewById(R.id.images_view)
        imagesRecyclerView.setHasFixedSize(true)
        noteImagesAdapter = NoteImagesAdapter(this, noteImages, onClickImgClose)
        imagesRecyclerView.adapter = noteImagesAdapter
        imagesViewManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesRecyclerView.layoutManager = imagesViewManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        imageButton.setOnClickListener {
            getImageContent.launch("image/*")
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
        italicBtn.setOnClickListener {
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
            bulletStr.setSpan(
                BulletSpan(40, Color.BLUE,20),
                body.selectionStart,
                body.selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            body.text = bulletStr
        }
//        todoListButton.setOnClickListener{
//            var str = SpannableStringBuilder(body.text)
//            var bulletHollow= "\u25CB "
//            var result = str.contains(bulletHollow)
//            if (result){
//                val n = 2
//                body.text = str.substring(n)
//            } else {
//                body.text = bulletHollow + str
//            }
//
//        }

        submitButton.setOnClickListener {
            createNote()
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
        createNotificationChannel()
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

    override fun onDestroy() {
        createNote()
        super.onDestroy()
    }

    private fun createNote() {
        if (body.text.toString().isNotEmpty() && title.text.toString().isNotEmpty()) {
            val str = SpannableStringBuilder(body.text)
            val note = Note(
                null,
                title.text.toString(),
                Html.toHtml(str, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
            )
            val ctx = this
            var noteId = 0L
            GlobalScope.launch(Dispatchers.IO) {
                noteId = appDatabase.noteDao().insert(note)
                if (noteImages.isNotEmpty()) {
                    appDatabase.noteImageDao().insert(noteImages)

                }
                val content = Html.fromHtml(note.body.toString(), Html.FROM_HTML_MODE_LEGACY)
                if (noteId != 0L && pushCheckBox.isChecked) {
                    val bigTextStyle = NotificationCompat.BigTextStyle()
                        .setBigContentTitle(note.title)
                        .bigText(content)
                    val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_link) // change this to icon
                        .setContentTitle(note.title)
                        .setContentText(content)
                        .setStyle(bigTextStyle)
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
            }
        }
    }

}