package com.spiritelli.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
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

    private val developerCode = "131013"

    private val spiritelli = mutableListOf<Spiritello>()

    private val prefs by lazy {
        getSharedPreferences("spiritelli", MODE_PRIVATE)
    }

    private var photoIndex = -1

    private lateinit var content: LinearLayout

    private val bgColor = Color.rgb(248, 247, 252)
    private val cardColor = Color.WHITE
    private val primaryColor = Color.rgb(112, 76, 220)
    private val textColor = Color.rgb(30, 28, 38)
    private val secondaryText = Color.rgb(110, 106, 120)
    private val borderColor = Color.rgb(232, 229, 239)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        load()
        showHome()
    }

    // ------------------------------------------------------------
    // HOME
    // ------------------------------------------------------------

    private fun showHome() {

        val root = baseScreen()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val logo = TextView(this).apply {
            text = "👻"
            textSize = 42f
            gravity = Gravity.CENTER
        }

        header.addView(
            logo,
            lp(-1, 60)
        )

        header.addView(
            text(
                "Spiritelli",
                30f,
                textColor,
                Gravity.CENTER
            )
        )

        header.addView(
            text(
                "La tua collezione Fortnite",
                14f,
                secondaryText,
                Gravity.CENTER
            )
        )

        root.addView(
            header,
            lp(-1, -2, 0, 10, 0, 20)
        )

        val search = EditText(this).apply {
            hint = "Cerca uno Spiritello"
            hintTextColor = Color.rgb(145, 140, 155)
            textSize = 16f
            setSingleLine(true)
            setTextColor(textColor)
            background = rounded(Color.WHITE, 22)
            setPadding(22, 0, 22, 0)
        }

        root.addView(
            search,
            lp(-1, 58, 0, 0, 0, 18)
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val developerButton = button(
            "⚙  Modalità Developer",
            primaryColor
        ) {
            developerLogin()
        }

        root.addView(
            developerButton,
            lp(-1, 54, 0, 14, 0, 0)
        )

        setContentView(root)

        fun render(query: String) {

            content.removeAllViews()

            val q = query
                .trim()
                .lowercase(Locale.getDefault())

            val results = spiritelli.filter {
                q.isEmpty() ||
                    it.name
                        .lowercase(Locale.getDefault())
                        .contains(q)
            }

            if (results.isEmpty()) {

                val empty = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(20, 50, 20, 50)
                }

                empty.addView(
                    text(
                        "👻",
                        42f,
                        secondaryText,
                        Gravity.CENTER
                    )
                )

                empty.addView(
                    text(
                        "Nessuno Spiritello",
                        18f,
                        textColor,
                        Gravity.CENTER
                    )
                )

                empty.addView(
                    text(
                        "Prova con un altro nome",
                        14f,
                        secondaryText,
                        Gravity.CENTER
                    )
                )

                content.addView(empty)
                return
            }

            results.forEach { spiritello ->

                val index = spiritelli.indexOf(spiritello)

                content.addView(
                    spiritelloCard(spiritello, index),
                    lp(-1, 92, 0, 0, 0, 12)
                )
            }
        }

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
                    render(s?.toString().orEmpty())
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        render("")
    }

    private fun spiritelloCard(
        spiritello: Spiritello,
        index: Int
    ): View {

        val card = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(12, 10, 16, 10)

            background = rounded(cardColor, 20)

            elevation = 2f

            setOnClickListener {
                showDetails(index)
            }
        }

        val image = ImageView(this).apply {

            scaleType = ImageView.ScaleType.CENTER_CROP

            background = rounded(
                Color.rgb(239, 235, 250),
                16
            )

            if (spiritello.imageUri != null) {

                runCatching {
                    setImageURI(
                        Uri.parse(spiritello.imageUri)
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
            lp(72, 72, 0, 0, 14, 0)
        )

        val information = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        information.addView(
            text(
                spiritello.name,
                17f,
                textColor,
                Gravity.START
            )
        )

        information.addView(
            text(
                "Spiritello di Fortnite",
                13f,
                secondaryText,
                Gravity.START
            )
        )

        card.addView(
            information,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        card.addView(
            text(
                "›",
                28f,
                secondaryText,
                Gravity.CENTER
            ),
            lp(30, -1)
        )

        return card
    }

    // ------------------------------------------------------------
    // DETAILS
    // ------------------------------------------------------------

    private fun showDetails(index: Int) {

        val spiritello = spiritelli[index]

        val root = baseScreen()

        root.addView(
            button(
                "‹  Indietro",
                Color.TRANSPARENT,
                textColor
            ) {
                showHome()
            },
            lp(-1, 50)
        )

        val image = ImageView(this).apply {

            scaleType = ImageView.ScaleType.CENTER_CROP

            background = rounded(
                Color.rgb(239, 235, 250),
                24
            )

            if (spiritello.imageUri != null) {

                runCatching {
                    setImageURI(
                        Uri.parse(spiritello.imageUri)
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
            lp(-1, 300, 0, 12, 0, 20)
        )

        root.addView(
            text(
                spiritello.name,
                28f,
                textColor,
                Gravity.CENTER
            )
        )

        root.addView(
            text(
                "Spiritello di Fortnite",
                14f,
                secondaryText,
                Gravity.CENTER
            )
        )

        setContentView(root)
    }

    // ------------------------------------------------------------
    // DEVELOPER LOGIN
    // ------------------------------------------------------------

    private fun developerLogin() {

        val input = EditText(this).apply {

            hint = "Codice Developer"
            inputType = 2
            setSingleLine(true)
            setPadding(20, 0, 20, 0)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Modalità Developer")
            .setMessage("Inserisci il codice per continuare.")
            .setView(input)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Accedi", null)
            .create()

        dialog.setOnShowListener {

            dialog
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    if (
                        input.text.toString() ==
                        developerCode
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

    // ------------------------------------------------------------
    // DEVELOPER
    // ------------------------------------------------------------

    private fun showDeveloper() {

        val root = baseScreen()

        root.addView(
            text(
                "Developer",
                28f,
                textColor,
                Gravity.CENTER
            )
        )

        root.addView(
            text(
                "Gestisci la tua collezione",
                14f,
                secondaryText,
                Gravity.CENTER
            ),
            lp(-1, -2, 0, 4, 0, 24)
        )

        root.addView(
            button(
                "＋  Aggiungi Spiritello",
                primaryColor
            ) {
                addSpiritello()
            },
            lp(-1, 56, 0, 0, 0, 10)
        )

        root.addView(
            button(
                "✏  Gestisci Spiritelli",
                Color.WHITE,
                textColor
            ) {
                manageSpiritelli()
            },
            lp(-1, 56, 0, 0, 0, 10)
        )

        root.addView(
            button(
                "←  Torna all'app",
                Color.WHITE,
                textColor
            ) {
                showHome()
            },
            lp(-1, 56, 0, 0, 0, 0)
        )

        setContentView(root)
    }

    // ------------------------------------------------------------
    // ADD
    // ------------------------------------------------------------

    private fun addSpiritello() {

        val input = EditText(this).apply {

            hint = "Nome dello Spiritello"
            setSingleLine(true)
            setPadding(20, 0, 20, 0)
        }

        AlertDialog.Builder(this)
            .setTitle("Nuovo Spiritello")
            .setView(input)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Aggiungi") { _, _ ->

                val name =
                    input.text.toString().trim()

                if (name.isNotEmpty()) {

                    spiritelli.add(
                        Spiritello(name)
                    )

                    save()

                    showDeveloper()
                }
            }
            .show()
    }

    // ------------------------------------------------------------
    // MANAGE
    // ------------------------------------------------------------

    private fun manageSpiritelli() {

        val root = baseScreen()

        root.addView(
            button(
                "‹  Indietro",
                Color.TRANSPARENT,
                textColor
            ) {
                showDeveloper()
            },
            lp(-1, 50)
        )

        root.addView(
            text(
                "I tuoi Spiritelli",
                26f,
                textColor,
                Gravity.CENTER
            ),
            lp(-1, -2, 0, 10, 0, 18)
        )

        if (spiritelli.isEmpty()) {

            root.addView(
                text(
                    "Non hai ancora aggiunto Spiritelli.",
                    15f,
                    secondaryText,
                    Gravity.CENTER
                )
            )

        } else {

            spiritelli.forEachIndexed { index, spiritello ->

                root.addView(
                    button(
                        "👻  ${spiritello.name}",
                        Color.WHITE,
                        textColor
                    ) {
                        editSpiritello(index)
                    },
                    lp(-1, 56, 0, 0, 0, 10)
                )
            }
        }

        setContentView(root)
    }

    // ------------------------------------------------------------
    // EDIT
    // ------------------------------------------------------------

    private fun editSpiritello(index: Int) {

        val spiritello = spiritelli[index]

        val input = EditText(this).apply {

            setText(spiritello.name)
            selectAll()
            setSingleLine(true)
            setPadding(20, 0, 20, 0)
        }

        AlertDialog.Builder(this)
            .setTitle("Modifica Spiritello")
            .setView(input)
            .setNeutralButton("📷 Foto") { _, _ ->

                photoIndex = index

                val intent =
                    Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .apply {

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
                    42
                )
            }
            .setNegativeButton("Elimina") { _, _ ->

                confirmDelete(index)
            }
            .setPositiveButton("Salva") { _, _ ->

                val name =
                    input.text.toString().trim()

                if (name.isNotEmpty()) {
                    spiritello.name = name
                }

                save()
                manageSpiritelli()
            }
            .show()
    }

    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------

    private fun confirmDelete(index: Int) {

        AlertDialog.Builder(this)
            .setTitle("Eliminare Spiritello?")
            .setMessage(
                "Questa operazione non può essere annullata."
            )
            .setNegativeButton(
                "Annulla",
                null
            )
            .setPositiveButton(
                "Elimina"
            ) { _, _ ->

                spiritelli.removeAt(index)

                save()

                manageSpiritelli()
            }
            .show()
    }

    // ------------------------------------------------------------
    // PHOTO
    // ------------------------------------------------------------

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
            requestCode == 42 &&
            resultCode == RESULT_OK &&
            data?.data != null &&
            photoIndex >= 0
        ) {

            val uri = data.data!!

            runCatching {

                contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
            }

            spiritelli[photoIndex].imageUri =
                uri.toString()

            save()

            photoIndex = -1

            manageSpiritelli()
        }
    }

    // ------------------------------------------------------------
    // STORAGE
    // ------------------------------------------------------------

    private fun save() {

        val editor = prefs.edit()

        editor.putInt(
            "count",
            spiritelli.size
        )

        spiritelli.forEachIndexed { index, spiritello ->

            editor.putString(
                "name_$index",
                spiritello.name
            )

            editor.putString(
                "img_$index",
                spiritello.imageUri
            )
        }

        editor.apply()
    }

    private fun load() {

        spiritelli.clear()

        val count =
            prefs.getInt("count", 0)

        repeat(count) { index ->

            val name =
                prefs.getString(
                    "name_$index",
                    "Spiritello"
                ) ?: "Spiritello"

            val image =
                prefs.getString(
                    "img_$index",
                    null
                )

            spiritelli.add(
                Spiritello(
                    name,
                    image
                )
            )
        }
    }

    // ------------------------------------------------------------
    // UI HELPERS
    // ------------------------------------------------------------

    private fun baseScreen(): LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                22,
                24,
                22,
                20
            )

            setBackgroundColor(
                bgColor
            )
        }
    }

    private fun text(
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

    private fun button(
        value: String,
        backgroundColor: Int,
        foregroundColor: Int = Color.WHITE,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {

            text = value
            textSize = 15f

            gravity = Gravity.CENTER

            setTextColor(
                foregroundColor
            )

            background = rounded(
                backgroundColor,
                18
            )

            elevation = 2f

            setOnClickListener {
                action()
            }

            setPadding(
                12,
                0,
                12,
                0
            )
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
                borderColor
            )
        }
    }

    private fun lp(
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
