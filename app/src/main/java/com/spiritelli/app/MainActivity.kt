package com.spiritelli.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

data class Spiritello(
    var name: String,
    var imageUri: String? = null
)

class MainActivity : Activity() {

    companion object {
        private const val PREFS_NAME = "spiritelli"
        private const val PHOTO_REQUEST = 42
        private const val DEVELOPER_CODE = "131013"

        private val BACKGROUND = Color.rgb(247, 246, 251)
        private val CARD = Color.WHITE
        private val PRIMARY = Color.rgb(111, 76, 220)
        private val PRIMARY_DARK = Color.rgb(87, 56, 180)
        private val TEXT = Color.rgb(30, 29, 38)
        private val SECONDARY = Color.rgb(112, 108, 122)
        private val BORDER = Color.rgb(229, 226, 237)
        private val IMAGE_BACKGROUND = Color.rgb(239, 235, 249)
    }

    private val spiritelli = mutableListOf<Spiritello>()

    private val prefs by lazy {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    private lateinit var homeList: LinearLayout

    private var photoIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadSpiritelli()
        showHome()
    }

    // ============================================================
    // HOME
    // ============================================================

    private fun showHome() {

        val root = createRoot()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val logo = TextView(this).apply {
            text = "👻"
            textSize = 40f
            gravity = Gravity.CENTER
        }

        header.addView(
            logo,
            params(
                width = -1,
                height = 58
            )
        )

        val title = textView(
            value = "Spiritelli",
            size = 30f,
            color = TEXT,
            gravity = Gravity.CENTER
        )

        title.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        header.addView(title)

        header.addView(
            textView(
                value = "La tua collezione Fortnite",
                size = 14f,
                color = SECONDARY,
                gravity = Gravity.CENTER
            )
        )

        root.addView(
            header,
            params(
                width = -1,
                height = -2,
                bottom = 20
            )
        )

        val search = EditText(this).apply {
            hint = "Cerca uno Spiritello"
            setHintTextColor(
                Color.rgb(145, 141, 154)
            )
            textSize = 16f
            setSingleLine(true)
            setTextColor(TEXT)
            background = rounded(
                CARD,
                18
            )
            setPadding(
                20,
                0,
                20,
                0
            )
        }

        root.addView(
            search,
            params(
                width = -1,
                height = 56,
                bottom = 16
            )
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        homeList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(homeList)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            primaryButton(
                value = "⚙  Modalità Developer"
            ) {
                openDeveloperLogin()
            },
            params(
                width = -1,
                height = 54,
                top = 14
            )
        )

        setContentView(root)

        search.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    renderHome(
                        s?.toString().orEmpty()
                    )
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        renderHome("")
    }

    private fun renderHome(query: String) {

        homeList.removeAllViews()

        val normalized = query
            .trim()
            .lowercase(Locale.getDefault())

        val filtered = spiritelli.filter { spiritello ->
            normalized.isEmpty() ||
                spiritello.name
                    .lowercase(Locale.getDefault())
                    .contains(normalized)
        }

        if (filtered.isEmpty()) {

            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(
                    20,
                    55,
                    20,
                    55
                )
            }

            empty.addView(
                textView(
                    value = "👻",
                    size = 38f,
                    color = SECONDARY,
                    gravity = Gravity.CENTER
                )
            )

            empty.addView(
                textView(
                    value = "Nessuno Spiritello",
                    size = 18f,
                    color = TEXT,
                    gravity = Gravity.CENTER
                )
            )

            empty.addView(
                textView(
                    value = if (spiritelli.isEmpty()) {
                        "Aggiungine uno dalla Modalità Developer"
                    } else {
                        "Prova a cercare un altro nome"
                    },
                    size = 14f,
                    color = SECONDARY,
                    gravity = Gravity.CENTER
                )
            )

            homeList.addView(empty)
            return
        }

        filtered.forEach { spiritello ->

            val index = spiritelli.indexOf(spiritello)

            homeList.addView(
                createSpiritelloCard(
                    spiritello = spiritello,
                    index = index
                ),
                params(
                    width = -1,
                    height = 92,
                    bottom = 12
                )
            )
        }
    }

    private fun createSpiritelloCard(
        spiritello: Spiritello,
        index: Int
    ): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                10,
                10,
                14,
                10
            )
            background = rounded(
                CARD,
                20
            )
            elevation = 2f

            setOnClickListener {
                showDetails(index)
            }
        }

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(
                IMAGE_BACKGROUND,
                16
            )

            if (spiritello.imageUri != null) {
                runCatching {
                    setImageURI(
                        Uri.parse(
                            spiritello.imageUri
                        )
                    )
                }
            } else {
                setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }
        }

        card.addView(
            image,
            params(
                width = 72,
                height = 72,
                right = 14
            )
        )

        val middle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val name = textView(
            value = spiritello.name,
            size = 17f,
            color = TEXT,
            gravity = Gravity.START
        )

        name.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        middle.addView(name)

        middle.addView(
            textView(
                value = "Tocca per vedere i dettagli",
                size = 13f,
                color = SECONDARY,
                gravity = Gravity.START
            )
        )

        card.addView(
            middle,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        card.addView(
            textView(
                value = "›",
                size = 28f,
                color = SECONDARY,
                gravity = Gravity.CENTER
            ),
            params(
                width = 28,
                height = 72
            )
        )

        return card
    }

    // ============================================================
    // DETTAGLI
    // ============================================================

    private fun showDetails(index: Int) {

        if (index !in spiritelli.indices) {
            showHome()
            return
        }

        val spiritello = spiritelli[index]
        val root = createRoot()

        root.addView(
            secondaryButton("‹  Indietro") {
                showHome()
            },
            params(
                width = -1,
                height = 50
            )
        )

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(
                IMAGE_BACKGROUND,
                24
            )

            if (spiritello.imageUri != null) {
                runCatching {
                    setImageURI(
                        Uri.parse(
                            spiritello.imageUri
                        )
                    )
                }
            } else {
                setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }
        }

        root.addView(
            image,
            params(
                width = -1,
                height = 300,
                top = 14,
                bottom = 20
            )
        )

        val title = textView(
            value = spiritello.name,
            size = 27f,
            color = TEXT,
            gravity = Gravity.CENTER
        )

        title.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        root.addView(title)

        root.addView(
            textView(
                value = "Spiritello di Fortnite",
                size = 14f,
                color = SECONDARY,
                gravity = Gravity.CENTER
            ),
            params(
                width = -1,
                height = -2,
                top = 4
            )
        )

        setContentView(root)
    }

    // ============================================================
    // DEVELOPER LOGIN
    // ============================================================

    private fun openDeveloperLogin() {

        val input = EditText(this).apply {
            hint = "Codice Developer"
            inputType = 2
            setSingleLine(true)
            setPadding(
                18,
                0,
                18,
                0
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Modalità Developer")
            .setMessage(
                "Inserisci il codice per continuare."
            )
            .setView(input)
            .setNegativeButton(
                "Annulla",
                null
            )
            .setPositiveButton(
                "Accedi",
                null
            )
            .create()

        dialog.setOnShowListener {

            dialog
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    if (
                        input.text
                            .toString() ==
                        DEVELOPER_CODE
                    ) {
                        dialog.dismiss()
                        showDeveloper()
                    } else {
                        Toast.makeText(
                            this,
                            "Codice errato",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialog.show()
    }

    // ============================================================
    // DEVELOPER
    // ============================================================

    private fun showDeveloper() {

        val root = createRoot()

        val title = textView(
            value = "Developer",
            size = 28f,
            color = TEXT,
            gravity = Gravity.CENTER
        )

        title.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        root.addView(title)

        root.addView(
            textView(
                value = "Gestisci la tua collezione",
                size = 14f,
                color = SECONDARY,
                gravity = Gravity.CENTER
            ),
            params(
                width = -1,
                height = -2,
                top = 4,
                bottom = 24
            )
        )

        root.addView(
            primaryButton(
                value = "＋  Aggiungi Spiritello"
            ) {
                addSpiritello()
            },
            params(
                width = -1,
                height = 56,
                bottom = 10
            )
        )

        root.addView(
            secondaryButton(
                value = "✏  Gestisci Spiritelli"
            ) {
                manageSpiritelli()
            },
            params(
                width = -1,
                height = 56,
                bottom = 10
            )
        )

        root.addView(
            secondaryButton(
                value = "←  Torna all'app"
            ) {
                showHome()
            },
            params(
                width = -1,
                height = 56
            )
        )

        setContentView(root)
    }

    // ============================================================
    // AGGIUNGI
    // ============================================================

    private fun addSpiritello() {

        val input = EditText(this).apply {
            hint = "Nome dello Spiritello"
            setSingleLine(true)
            setPadding(
                18,
                0,
                18,
                0
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Nuovo Spiritello")
            .setView(input)
            .setNegativeButton(
                "Annulla",
                null
            )
            .setPositiveButton(
                "Aggiungi"
            ) { _, _ ->

                val name = input.text
                    .toString()
                    .trim()

                if (name.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Inserisci un nome.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                spiritelli.add(
                    Spiritello(name)
                )

                saveSpiritelli()
                showDeveloper()
            }
            .show()
    }

    // ============================================================
    // GESTIONE SPIRITELLI
    // ============================================================

    private fun manageSpiritelli() {

        val root = createRoot()

        root.addView(
            secondaryButton("‹  Indietro") {
                showDeveloper()
            },
            params(
                width = -1,
                height = 50
            )
        )

        val title = textView(
            value = "I tuoi Spiritelli",
            size = 26f,
            color = TEXT,
            gravity = Gravity.CENTER
        )

        title.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        root.addView(
            title,
            params(
                width = -1,
                height = -2,
                top = 10,
                bottom = 18
            )
        )

        if (spiritelli.isEmpty()) {

            root.addView(
                textView(
                    value = "Non hai ancora aggiunto Spiritelli.",
                    size = 15f,
                    color = SECONDARY,
                    gravity = Gravity.CENTER
                )
            )

        } else {

            val scroll = ScrollView(this)

            val list = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            spiritelli.forEachIndexed { index, spiritello ->

                list.addView(
                    manageCard(
                        spiritello = spiritello,
                        index = index
                    ),
                    params(
                        width = -1,
                        height = 84,
                        bottom = 10
                    )
                )
            }

            scroll.addView(list)

            root.addView(
                scroll,
                LinearLayout.LayoutParams(
                    -1,
                    0,
                    1f
                )
            )
        }

        setContentView(root)
    }

    private fun manageCard(
        spiritello: Spiritello,
        index: Int
    ): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                10,
                10,
                12,
                10
            )
            background = rounded(
                CARD,
                18
            )
            elevation = 1f

            setOnClickListener {
                editSpiritello(index)
            }
        }

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(
                IMAGE_BACKGROUND,
                14
            )

            if (spiritello.imageUri != null) {
                runCatching {
                    setImageURI(
                        Uri.parse(
                            spiritello.imageUri
                        )
                    )
                }
            } else {
                setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }
        }

        card.addView(
            image,
            params(
                width = 64,
                height = 64,
                right = 12
            )
        )

        val name = textView(
            value = spiritello.name,
            size = 16f,
            color = TEXT,
            gravity = Gravity.CENTER_VERTICAL
        )

        name.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        card.addView(
            name,
            LinearLayout.LayoutParams(
                0,
                -1,
                1f
            )
        )

        card.addView(
            textView(
                value = "›",
                size = 26f,
                color = SECONDARY,
                gravity = Gravity.CENTER
            ),
            params(
                width = 26,
                height = -1
            )
        )

        return card
    }

    // ============================================================
    // MODIFICA
    // ============================================================

    private fun editSpiritello(index: Int) {

        if (index !in spiritelli.indices) {
            return
        }

        val spiritello = spiritelli[index]

        val input = EditText(this).apply {
            setText(spiritello.name)
            selectAll()
            setSingleLine(true)
            setPadding(
                18,
                0,
                18,
                0
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Modifica Spiritello")
            .setView(input)
            .setNeutralButton(
                "📷 Foto"
            ) { _, _ ->
                choosePhoto(index)
            }
            .setNegativeButton(
                "Elimina"
            ) { _, _ ->
                confirmDelete(index)
            }
            .setPositiveButton(
                "Salva"
            ) { _, _ ->

                val newName = input.text
                    .toString()
                    .trim()

                if (newName.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Il nome non può essere vuoto.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                spiritello.name = newName

                saveSpiritelli()
                manageSpiritelli()
            }
            .show()
    }

    // ============================================================
    // ELIMINA
    // ============================================================

    private fun confirmDelete(index: Int) {

        if (index !in spiritelli.indices) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminare Spiritello?")
            .setMessage(
                "Lo Spiritello verrà rimosso dalla raccolta."
            )
            .setNegativeButton(
                "Annulla",
                null
            )
            .setPositiveButton(
                "Elimina"
            ) { _, _ ->

                spiritelli.removeAt(index)

                saveSpiritelli()
                manageSpiritelli()
            }
            .show()
    }

    // ============================================================
    // FOTO
    // ============================================================

    private fun choosePhoto(index: Int) {

        if (index !in spiritelli.indices) {
            return
        }

        photoIndex = index

        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT
        ).apply {
            type = "image/*"

            addCategory(
                Intent.CATEGORY_OPENABLE
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }

        startActivityForResult(
            intent,
            PHOTO_REQUEST
        )
    }

    @Deprecated("Legacy API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode != PHOTO_REQUEST ||
            resultCode != RESULT_OK
        ) {
            return
        }

        val uri = data?.data

        if (uri == null) {
            photoIndex = -1
            return
        }

        if (
            photoIndex < 0 ||
            photoIndex >= spiritelli.size
        ) {
            photoIndex = -1
            return
        }

        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        spiritelli[photoIndex].imageUri =
            uri.toString()

        saveSpiritelli()

        photoIndex = -1

        manageSpiritelli()
    }

    // ============================================================
    // SALVATAGGIO
    // ============================================================

    private fun saveSpiritelli() {

        val editor = prefs.edit()

        editor.clear()

        editor.putInt(
            "count",
            spiritelli.size
        )

        spiritelli.forEachIndexed { index, spiritello ->

            editor.putString(
                "name_$index",
                spiritello.name
            )

            if (spiritello.imageUri != null) {
                editor.putString(
                    "image_$index",
                    spiritello.imageUri
                )
            }
        }

        editor.apply()
    }

    private fun loadSpiritelli() {

        spiritelli.clear()

        val count = prefs.getInt(
            "count",
            0
        )

        repeat(count) { index ->

            val name = prefs.getString(
                "name_$index",
                "Spiritello"
            ) ?: "Spiritello"

            val imageUri = prefs.getString(
                "image_$index",
                null
            )

            spiritelli.add(
                Spiritello(
                    name = name,
                    imageUri = imageUri
                )
            )
        }
    }

    // ============================================================
    // UI HELPERS
    // ============================================================

    private fun createRoot(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                20,
                22,
                20,
                20
            )

            setBackgroundColor(
                BACKGROUND
            )
        }
    }

    private fun textView(
        value: String,
        size: Float,
        color: Int,
        gravity: Int
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            this.gravity = gravity

            setPadding(
                4,
                4,
                4,
                4
            )
        }
    }

    private fun primaryButton(
        value: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = 15f
            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            background = rounded(
                PRIMARY,
                18
            )

            elevation = 2f

            setPadding(
                12,
                0,
                12,
                0
            )

            setOnClickListener {
                action()
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        view.alpha = 0.85f
                    }

                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1f
                    }
                }

                false
            }
        }
    }

    private fun secondaryButton(
        value: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = 15f
            gravity = Gravity.CENTER

            setTextColor(TEXT)

            background = rounded(
                CARD,
                18
            )

            elevation = 1f

            setPadding(
                12,
                0,
                12,
                0
            )

            setOnClickListener {
                action()
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        view.alpha = 0.85f
                    }

                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1f
                    }
                }

                false
            }
        }
    }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)

            cornerRadius =
                radius.toFloat()

            setStroke(
                1,
                BORDER
            )
        }
    }

    private fun params(
        width: Int,
        height: Int,
        left: Int = 0,
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            width,
            height
        ).apply {
            setMargins(
                left,
                top,
                right,
                bottom
            )
        }
    }
}
