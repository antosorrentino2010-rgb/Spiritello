package com.spiritelli.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

data class Spiritello(
    var name: String,
    var rarity: String = "Comune",
    var rarityColor: Int = Color.rgb(150, 150, 150),
    var imageUri: String? = null,
    var collected: Boolean = false
)

class MainActivity : Activity() {

    private val developerCode = "131013"
    private val developerKeyword = "kira"

    private val spiritelli = ArrayList<Spiritello>()

    private val prefs by lazy {
        getSharedPreferences("spiritelli_data", MODE_PRIVATE)
    }

    private var photoIndex = -1

    private lateinit var mainRoot: LinearLayout
    private lateinit var searchBox: EditText
    private lateinit var grid: LinearLayout
    private lateinit var counter: TextView

    private val backgroundColor = Color.rgb(12, 13, 16)
    private val cardColor = Color.rgb(24, 25, 30)
    private val cardColor2 = Color.rgb(30, 31, 37)
    private val textColor = Color.WHITE
    private val secondaryText = Color.rgb(160, 163, 170)
    private val accentColor = Color.rgb(170, 255, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadData()

        showSplash()

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
    }

    // ---------------------------------------------------------
    // SPLASH
    // ---------------------------------------------------------

    private fun showSplash() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(backgroundColor)

        val icon = TextView(this)
        icon.text = "👻"
        icon.textSize = 72f
        icon.gravity = Gravity.CENTER

        val title = TextView(this)
        title.text = "SPIRITELLI"
        title.textSize = 28f
        title.setTextColor(textColor)
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "Colleziona tutti gli Spiritelli"
        subtitle.textSize = 14f
        subtitle.setTextColor(secondaryText)
        subtitle.gravity = Gravity.CENTER

        root.addView(icon, lp(-1, 100))
        root.addView(title, lp(-1, 55))
        root.addView(subtitle, lp(-1, 45))

        setContentView(root)

        root.postDelayed({
            showHome()
        }, 1400)
    }

    // ---------------------------------------------------------
    // HOME
    // ---------------------------------------------------------

    private fun showHome() {

        mainRoot = LinearLayout(this)
        mainRoot.orientation = LinearLayout.VERTICAL
        mainRoot.setBackgroundColor(backgroundColor)
        mainRoot.setPadding(18, 22, 18, 18)

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        val titleBox = LinearLayout(this)
        titleBox.orientation = LinearLayout.VERTICAL

        val title = textView(
            "Spiritelli",
            30f,
            textColor,
            Gravity.START
        )

        val subtitle = textView(
            "La tua collezione",
            14f,
            secondaryText,
            Gravity.START
        )

        titleBox.addView(title)
        titleBox.addView(subtitle)

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val settings = Button(this)
        settings.text = "⚙"
        settings.textSize = 22f
        settings.setTextColor(textColor)
        settings.background = rounded(cardColor2, 18)
        settings.setOnClickListener {
            developerLogin()
        }

        header.addView(settings, lp(58, 58))

        mainRoot.addView(header)

        searchBox = EditText(this)
        searchBox.hint = "Cerca uno Spiritello..."
        searchBox.setHintTextColor(Color.rgb(120, 123, 130))
        searchBox.setTextColor(textColor)
        searchBox.textSize = 17f
        searchBox.setSingleLine(true)
        searchBox.setPadding(22, 0, 22, 0)
        searchBox.background = rounded(cardColor2, 25)

        mainRoot.addView(
            searchBox,
            LinearLayout.LayoutParams(
                -1,
                64
            ).apply {
                topMargin = 18
                bottomMargin = 14
            }
        )

        counter = textView(
            "0/0",
            15f,
            Color.rgb(190, 255, 110),
            Gravity.START
        )

        mainRoot.addView(
            counter,
            LinearLayout.LayoutParams(-1, 30)
        )

        grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.addView(grid)

        mainRoot.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(mainRoot)

        searchBox.addTextChangedListener(
            object : android.text.TextWatcher {

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

                    val value = s?.toString()?.trim() ?: ""

                    if (
                        value.lowercase(Locale.getDefault()) ==
                        developerKeyword
                    ) {
                        searchBox.setText("")
                        developerLogin()
                        return
                    }

                    renderGrid(value)
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {
                }
            }
        )

        renderGrid("")
    }

    // ---------------------------------------------------------
    // GRID 2 PER VOLTA
    // ---------------------------------------------------------

    private fun renderGrid(query: String) {

        grid.removeAllViews()

        val q = query
            .trim()
            .lowercase(Locale.getDefault())

        val filtered = ArrayList<Spiritello>()

        for (s in spiritelli) {

            if (
                q.isEmpty() ||
                s.name.lowercase(Locale.getDefault()).contains(q)
            ) {
                filtered.add(s)
            }
        }

        var collected = 0

        for (s in spiritelli) {
            if (s.collected) {
                collected++
            }
        }

        counter.text = "$collected/${spiritelli.size}"

        if (filtered.isEmpty()) {

            val empty = textView(
                "Nessuno Spiritello",
                17f,
                secondaryText,
                Gravity.CENTER
            )

            grid.addView(
                empty,
                LinearLayout.LayoutParams(-1, 180)
            )

            return
        }

        var i = 0

        while (i < filtered.size) {

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val first = filtered[i]

            row.addView(
                createCard(first),
                LinearLayout.LayoutParams(
                    0,
                    250,
                    1f
                ).apply {
                    rightMargin = 6
                }
            )

            if (i + 1 < filtered.size) {

                val second = filtered[i + 1]

                row.addView(
                    createCard(second),
                    LinearLayout.LayoutParams(
                        0,
                        250,
                        1f
                    ).apply {
                        leftMargin = 6
                    }
                )

            } else {

                row.addView(
                    Space(this),
                    LinearLayout.LayoutParams(
                        0,
                        250,
                        1f
                    ).apply {
                        leftMargin = 6
                    }
                )
            }

            grid.addView(
                row,
                LinearLayout.LayoutParams(-1, 250).apply {
                    bottomMargin = 12
                }
            )

            i += 2
        }
    }

    // ---------------------------------------------------------
    // CARD
    // ---------------------------------------------------------

    private fun createCard(s: Spiritello): View {

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(8, 8, 8, 10)

        if (s.collected) {
            card.background = rounded(
                Color.rgb(75, 105, 25),
                20
            )
        } else {
            card.background = rounded(
                cardColor,
                20
            )
        }

        card.setOnClickListener {
            showDetails(s)
        }

        val image = ImageView(this)

        image.scaleType = ImageView.ScaleType.CENTER_CROP

        if (s.imageUri != null) {

            try {
                image.setImageURI(
                    Uri.parse(s.imageUri)
                )
            } catch (_: Exception) {
                image.setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }

        } else {

            image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        card.addView(
            image,
            LinearLayout.LayoutParams(
                -1,
                165
            )
        )

        val name = textView(
            s.name,
            16f,
            textColor,
            Gravity.CENTER
        )

        card.addView(
            name,
            LinearLayout.LayoutParams(
                -1,
                35
            )
        )

        val rarity = TextView(this)
        rarity.text = s.rarity
        rarity.textSize = 13f
        rarity.setTextColor(s.rarityColor)
        rarity.gravity = Gravity.CENTER

        card.addView(
            rarity,
            LinearLayout.LayoutParams(
                -1,
                30
            )
        )

        return card
    }

    // ---------------------------------------------------------
    // DETAILS
    // ---------------------------------------------------------

    private fun showDetails(s: Spiritello) {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(18, 22, 18, 18)
        root.setBackgroundColor(backgroundColor)

        val back = Button(this)
        back.text = "‹  Indietro"
        back.textSize = 16f
        back.setTextColor(textColor)
        back.background = rounded(cardColor, 18)

        back.setOnClickListener {
            showHome()
        }

        root.addView(
            back,
            LinearLayout.LayoutParams(-1, 55)
        )

        val image = ImageView(this)
        image.scaleType = ImageView.ScaleType.CENTER_CROP

        if (s.imageUri != null) {

            try {
                image.setImageURI(
                    Uri.parse(s.imageUri)
                )
            } catch (_: Exception) {
                image.setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }

        } else {

            image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        root.addView(
            image,
            LinearLayout.LayoutParams(-1, 360).apply {
                topMargin = 15
            }
        )

        val name = textView(
            s.name,
            29f,
            textColor,
            Gravity.CENTER
        )

        root.addView(
            name,
            LinearLayout.LayoutParams(-1, 55)
        )

        val rarity = TextView(this)
        rarity.text = s.rarity
        rarity.textSize = 18f
        rarity.setTextColor(s.rarityColor)
        rarity.gravity = Gravity.CENTER

        root.addView(
            rarity,
            LinearLayout.LayoutParams(-1, 45)
        )

        val status = if (s.collected) {
            "✓ Nella tua collezione"
        } else {
            "Non ancora raccolto"
        }

        val collected = textView(
            status,
            15f,
            if (s.collected) accentColor else secondaryText,
            Gravity.CENTER
        )

        root.addView(
            collected,
            LinearLayout.LayoutParams(-1, 45)
        )

        setContentView(root)
    }

    // ---------------------------------------------------------
    // DEVELOPER LOGIN
    // ---------------------------------------------------------

    private fun developerLogin() {

        val input = EditText(this)

        input.hint = "Codice Developer"
        input.inputType = 2
        input.setSingleLine(true)
        input.setTextColor(Color.BLACK)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Developer")
            .setMessage("Inserisci il codice")
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

    // ---------------------------------------------------------
    // DEVELOPER
    // ---------------------------------------------------------

    private fun showDeveloper() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(18, 22, 18, 18)
        root.setBackgroundColor(backgroundColor)

        root.addView(
            textView(
                "Developer",
                30f,
                textColor,
                Gravity.CENTER
            ),
            LinearLayout.LayoutParams(-1, 60)
        )

        root.addView(
            textView(
                "Gestisci la collezione",
                14f,
                secondaryText,
                Gravity.CENTER
            ),
            LinearLayout.LayoutParams(-1, 40)
        )

        val add = developerButton(
            "＋  Aggiungi Spiritello"
        )

        add.setOnClickListener {
            addSpiritello()
        }

        root.addView(
            add,
            LinearLayout.LayoutParams(-1, 60).apply {
                bottomMargin = 10
            }
        )

        val manage = developerButton(
            "✏  Gestisci Spiritelli"
        )

        manage.setOnClickListener {
            manageSpiritelli()
        }

        root.addView(
            manage,
            LinearLayout.LayoutParams(-1, 60).apply {
                bottomMargin = 10
            }
        )

        val back = developerButton(
            "←  Torna all'app"
        )

        back.setOnClickListener {
            showHome()
        }

        root.addView(
            back,
            LinearLayout.LayoutParams(-1, 60)
        )

        setContentView(root)
    }

    // ---------------------------------------------------------
    // ADD
    // ---------------------------------------------------------

    private fun addSpiritello() {

        val name = EditText(this)
        name.hint = "Nome Spiritello"

        val rarity = EditText(this)
        rarity.hint = "Rarità"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 0, 30, 0)

        layout.addView(name)
        layout.addView(rarity)

        AlertDialog.Builder(this)
            .setTitle("Nuovo Spiritello")
            .setView(layout)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Aggiungi") { _, _ ->

                val n = name.text.toString().trim()
                val r = rarity.text.toString().trim()

                if (n.isNotEmpty()) {

                    spiritelli.add(
                        Spiritello(
                            name = n,
                            rarity = if (r.isEmpty()) {
                                "Comune"
                            } else {
                                r
                            }
                        )
                    )

                    saveData()
                    showDeveloper()
                }
            }
            .show()
    }

    // ---------------------------------------------------------
    // MANAGE
    // ---------------------------------------------------------

    private fun manageSpiritelli() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(18, 22, 18, 18)
        root.setBackgroundColor(backgroundColor)

        val back = developerButton(
            "‹  Indietro"
        )

        back.setOnClickListener {
            showDeveloper()
        }

        root.addView(
            back,
            LinearLayout.LayoutParams(-1, 55)
        )

        root.addView(
            textView(
                "Spiritelli",
                28f,
                textColor,
                Gravity.CENTER
            ),
            LinearLayout.LayoutParams(-1, 60)
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL

        for (i in spiritelli.indices) {

            val s = spiritelli[i]

            val button = developerButton(
                "👻  ${s.name}  •  ${s.rarity}"
            )

            button.setOnClickListener {
                editSpiritello(i)
            }

            list.addView(
                button,
                LinearLayout.LayoutParams(-1, 60).apply {
                    bottomMargin = 8
                }
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

        setContentView(root)
    }

    // ---------------------------------------------------------
    // EDIT
    // ---------------------------------------------------------

    private fun editSpiritello(index: Int) {

        if (index < 0 || index >= spiritelli.size) {
            return
        }

        val s = spiritelli[index]

        val name = EditText(this)
        name.setText(s.name)
        name.setSingleLine(true)

        val rarity = EditText(this)
        rarity.setText(s.rarity)
        rarity.setSingleLine(true)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 0, 30, 0)

        layout.addView(name)
        layout.addView(rarity)

        AlertDialog.Builder(this)
            .setTitle("Modifica Spiritello")
            .setView(layout)
            .setNeutralButton("📷 Foto") { _, _ ->

                photoIndex = index

                val intent = Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                )

                intent.type = "image/*"

                intent.addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                intent.addFlags(
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )

                startActivityForResult(
                    intent,
                    100
                )
            }
            .setNegativeButton("Elimina") { _, _ ->

                spiritelli.removeAt(index)

                saveData()
                manageSpiritelli()
            }
            .setPositiveButton("Salva") { _, _ ->

                val newName =
                    name.text.toString().trim()

                val newRarity =
                    rarity.text.toString().trim()

                if (newName.isNotEmpty()) {
                    s.name = newName
                }

                if (newRarity.isNotEmpty()) {
                    s.rarity = newRarity
                }

                saveData()
                manageSpiritelli()
            }
            .setNeutralButton(
                "🎨 Colore rarità"
            ) { _, _ ->
                chooseRarityColor(index)
            }
            .show()
    }

    // ---------------------------------------------------------
    // RARITY COLOR
    // ---------------------------------------------------------

    private fun chooseRarityColor(index: Int) {

        if (index < 0 || index >= spiritelli.size) {
            return
        }

        val colors = arrayOf(
            "Bianco",
            "Grigio",
            "Verde",
            "Blu",
            "Viola",
            "Rosa",
            "Arancione",
            "Rosso",
            "Giallo",
            "Verde fluo"
        )

        val values = intArrayOf(
            Color.WHITE,
            Color.GRAY,
            Color.rgb(80, 220, 100),
            Color.rgb(70, 150, 255),
            Color.rgb(170, 80, 255),
            Color.rgb(255, 100, 190),
            Color.rgb(255, 150, 50),
            Color.rgb(255, 70, 70),
            Color.YELLOW,
            Color.rgb(170, 255, 0)
        )

        AlertDialog.Builder(this)
            .setTitle("Colore rarità")
            .setItems(colors) { _, which ->

                spiritelli[index].rarityColor =
                    values[which]

                saveData()
                manageSpiritelli()
            }
            .show()
    }

    // ---------------------------------------------------------
    // PHOTO RESULT
    // ---------------------------------------------------------

    @Deprecated("Legacy Android API")
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
            requestCode == 100 &&
            resultCode == RESULT_OK &&
            data != null &&
            data.data != null &&
            photoIndex >= 0 &&
            photoIndex < spiritelli.size
        ) {

            val uri = data.data!!

            try {

                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

            } catch (_: Exception) {
            }

            spiritelli[photoIndex].imageUri =
                uri.toString()

            saveData()

            photoIndex = -1

            manageSpiritelli()
        }
    }

    // ---------------------------------------------------------
    // SAVE
    // ---------------------------------------------------------

    private fun saveData() {

        val editor = prefs.edit()

        editor.putInt(
            "count",
            spiritelli.size
        )

        for (i in spiritelli.indices) {

            val s = spiritelli[i]

            editor.putString(
                "name_$i",
                s.name
            )

            editor.putString(
                "rarity_$i",
                s.rarity
            )

            editor.putInt(
                "color_$i",
                s.rarityColor
            )

            editor.putString(
                "image_$i",
                s.imageUri
            )

            editor.putBoolean(
                "collected_$i",
                s.collected
            )
        }

        editor.apply()
    }

    // ---------------------------------------------------------
    // LOAD
    // ---------------------------------------------------------

    private fun loadData() {

        spiritelli.clear()

        val count = prefs.getInt(
            "count",
            0
        )

        for (i in 0 until count) {

            val name =
                prefs.getString(
                    "name_$i",
                    "Spiritello"
                ) ?: "Spiritello"

            val rarity =
                prefs.getString(
                    "rarity_$i",
                    "Comune"
                ) ?: "Comune"

            val color =
                prefs.getInt(
                    "color_$i",
                    Color.GRAY
                )

            val image =
                prefs.getString(
                    "image_$i",
                    null
                )

            val collected =
                prefs.getBoolean(
                    "collected_$i",
                    false
                )

            spiritelli.add(
                Spiritello(
                    name = name,
                    rarity = rarity,
                    rarityColor = color,
                    imageUri = image,
                    collected = collected
                )
            )
        }
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

    private fun textView(
        text: String,
        size: Float,
        color: Int,
        gravity: Int
    ): TextView {

        val v = TextView(this)

        v.text = text
        v.textSize = size
        v.setTextColor(color)
        v.gravity = gravity

        return v
    }

    private fun developerButton(
        text: String
    ): Button {

        val b = Button(this)

        b.text = text
        b.textSize = 16f
        b.setTextColor(textColor)
        b.isAllCaps = false
        b.background = rounded(
            cardColor,
            18
        )

        return b
    }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        val drawable = GradientDrawable()

        drawable.setColor(color)
        drawable.cornerRadius =
            radius.toFloat()

        drawable.setStroke(
            1,
            Color.rgb(45, 46, 52)
        )

        return drawable
    }

    private fun lp(
        width: Int,
        height: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            width,
            height
        )
    }
}
