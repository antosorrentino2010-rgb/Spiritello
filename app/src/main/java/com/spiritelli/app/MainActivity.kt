package com.spiritelli.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    var imageUri: String? = null,
    var rarity: String = "Comune",
    var rarityColor: Int = Color.rgb(160, 160, 160),
    var collected: Boolean = false
)

class MainActivity : Activity() {

    companion object {
        private const val PREFS = "spiritelli"
        private const val PHOTO_REQUEST = 42
        private const val DEVELOPER_WORD = "kira"

        private val BG = Color.rgb(11, 11, 15)
        private val CARD = Color.rgb(22, 22, 28)
        private val CARD_FOUND = Color.rgb(80, 255, 80)
        private val TEXT = Color.rgb(245, 245, 248)
        private val SECONDARY = Color.rgb(155, 155, 168)
        private val PURPLE = Color.rgb(125, 85, 235)
        private val BORDER = Color.rgb(43, 43, 52)
        private val IMAGE_BG = Color.rgb(32, 30, 42)
    }

    private val spiritelli = mutableListOf<Spiritello>()

    private val prefs by lazy {
        getSharedPreferences(PREFS, MODE_PRIVATE)
    }

    private lateinit var collectionList: LinearLayout

    private var photoIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadSpiritelli()
        showSplash()
    }

    // =========================================================
    // SPLASH
    // =========================================================

    private fun showSplash() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(BG)
        }

        val icon = TextView(this).apply {
            text = "◈"
            textSize = 90f
            setTextColor(PURPLE)
            gravity = Gravity.CENTER
        }

        root.addView(
            icon,
            params(-1, 130)
        )

        val title = TextView(this).apply {
            text = "SPIRITELLI"
            textSize = 28f
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "PUNTO ZERO"
            textSize = 12f
            setTextColor(SECONDARY)
            gravity = Gravity.CENTER
        }

        root.addView(
            subtitle,
            params(-1, 40)
        )

        setContentView(root)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                showHome()
            },
            1000
        )
    }

    // =========================================================
    // HOME
    // =========================================================

    private fun showHome() {

        val root = createRoot()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val logo = TextView(this).apply {
            text = "◈"
            textSize = 38f
            setTextColor(PURPLE)
            gravity = Gravity.CENTER
        }

        header.addView(
            logo,
            params(52, 52)
        )

        val headerText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Spiritelli"
            textSize = 25f
            setTextColor(TEXT)
            typeface = Typeface.DEFAULT_BOLD
        }

        headerText.addView(title)

        headerText.addView(
            TextView(this).apply {
                text = "La tua collezione"
                textSize = 13f
                setTextColor(SECONDARY)
            }
        )

        header.addView(
            headerText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        root.addView(
            header,
            params(-1, 58, 0, 0, 0, 18)
        )

        // SEARCH

        val search = EditText(this).apply {
            hint = "Cerca uno Spiritello..."
            textSize = 17f
            setTextColor(TEXT)
            setHintTextColor(Color.rgb(125, 125, 140))
            setSingleLine(true)
            background = rounded(CARD, 22)
            setPadding(22, 0, 22, 0)
        }

        root.addView(
            search,
            params(-1, 64, 0, 0, 0, 12)
        )

        // NUMERI X/X

        val numbers = TextView(this).apply {
            text = completionText()
            textSize = 15f
            setTextColor(Color.rgb(90, 255, 110))
            gravity = Gravity.END
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            numbers,
            params(-1, 28)
        )

        // LISTA

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        collectionList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(collectionList)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        renderCollection("")

        search.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val value = s
                        ?.toString()
                        ?.trim()
                        ?.lowercase(Locale.getDefault())
                        .orEmpty()

                    if (value == DEVELOPER_WORD) {
                        showDeveloper()
                        return
                    }

                    renderCollection(
                        s?.toString().orEmpty()
                    )

                    numbers.text = completionText()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {}
            }
        )
    }

    // =========================================================
    // COLLECTION
    // =========================================================

    private fun renderCollection(query: String) {

        collectionList.removeAllViews()

        val q = query
            .trim()
            .lowercase(Locale.getDefault())

        val filtered = spiritelli.filter {
            q.isEmpty() ||
                it.name
                    .lowercase(Locale.getDefault())
                    .contains(q)
        }

        if (filtered.isEmpty()) {

            val empty = TextView(this).apply {
                text =
                    if (spiritelli.isEmpty()) {
                        "La collezione è vuota"
                    } else {
                        "Nessuno Spiritello trovato"
                    }

                textSize = 17f
                setTextColor(SECONDARY)
                gravity = Gravity.CENTER
            }

            collectionList.addView(
                empty,
                params(-1, 180)
            )

            return
        }

        var index = 0

        while (index < filtered.size) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            row.addView(
                createCard(filtered[index]),
                LinearLayout.LayoutParams(
                    0,
                    245,
                    1f
                ).apply {
                    setMargins(0, 0, 5, 10)
                }
            )

            if (index + 1 < filtered.size) {

                row.addView(
                    createCard(filtered[index + 1]),
                    LinearLayout.LayoutParams(
                        0,
                        245,
                        1f
                    ).apply {
                        setMargins(5, 0, 0, 10)
                    }
                )

            } else {

                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(
                        0,
                        245,
                        1f
                    )
                )
            }

            collectionList.addView(row)

            index += 2
        }
    }

    // =========================================================
    // CARD
    // =========================================================

    private fun createCard(
        spiritello: Spiritello
    ): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(9, 9, 9, 8)

            background = rounded(
                if (spiritello.collected) {
                    CARD_FOUND
                } else {
                    CARD
                },
                20
            )

            elevation = 3f

            setOnClickListener {
                toggleCollected(spiritello)
            }

            setOnLongClickListener {
                showDetails(spiritello)
                true
            }
        }

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(
                IMAGE_BG,
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
            params(-1, 160)
        )

        val name = TextView(this).apply {
            text = spiritello.name
            textSize = 16f
            setTextColor(
                if (spiritello.collected) {
                    Color.BLACK
                } else {
                    TEXT
                }
            )
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
        }

        card.addView(
            name,
            params(-1, 30, 0, 6)
        )

        val rarity = TextView(this).apply {
            text = spiritello.rarity
            textSize = 13f
            setTextColor(
                if (spiritello.collected) {
                    Color.BLACK
                } else {
                    spiritello.rarityColor
                }
            )
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        card.addView(
            rarity,
            params(-1, 25)
        )

        return card
    }

    // =========================================================
    // CONTRASSEGNA
    // =========================================================

    private fun toggleCollected(
        spiritello: Spiritello
    ) {

        spiritello.collected =
            !spiritello.collected

        saveSpiritelli()

        renderCollection("")

        Toast.makeText(
            this,
            if (spiritello.collected) {
                "Spiritello trovato ✓"
            } else {
                "Spiritello non trovato"
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    // =========================================================
    // DETTAGLI
    // =========================================================

    private fun showDetails(
        spiritello: Spiritello
    ) {

        val root = createRoot()

        root.addView(
            secondaryButton("‹  Indietro") {
                showHome()
            },
            params(-1, 52)
        )

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(
                IMAGE_BG,
                22
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
            params(-1, 300, 0, 14, 0, 18)
        )

        val name = TextView(this).apply {
            text = spiritello.name
            textSize = 28f
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(name)

        root.addView(
            TextView(this).apply {
                text = spiritello.rarity
                textSize = 16f
                setTextColor(
                    spiritello.rarityColor
                )
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            },
            params(-1, 35, 0, 4)
        )

        root.addView(
            primaryButton(
                if (spiritello.collected) {
                    "✓ Trovato"
                } else {
                    "○ Non trovato"
                }
            ) {
                toggleCollected(spiritello)
                showDetails(spiritello)
            },
            params(-1, 55, 0, 20)
        )

        setContentView(root)
    }

    // =========================================================
    // DEVELOPER
    // =========================================================

    private fun showDeveloper() {

        val root = createRoot()

        val title = TextView(this).apply {
            text = "Developer"
            textSize = 28f
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(title)

        root.addView(
            TextView(this).apply {
                text = "Gestisci la collezione"
                textSize = 14f
                setTextColor(SECONDARY)
                gravity = Gravity.CENTER
            },
            params(-1, 30, 0, 2, 0, 20)
        )

        root.addView(
            primaryButton(
                "＋  Aggiungi Spiritello"
            ) {
                addSpiritello()
            },
            params(-1, 56, 0, 0, 0, 10)
        )

        root.addView(
            secondaryButton(
                "✏  Gestisci Spiritelli"
            ) {
                manageSpiritelli()
            },
            params(-1, 56, 0, 0, 0, 10)
        )

        root.addView(
            secondaryButton(
                "←  Torna alla collezione"
            ) {
                showHome()
            },
            params(-1, 56)
        )

        setContentView(root)
    }

    // =========================================================
    // ADD
    // =========================================================

    private fun addSpiritello() {

        val input = EditText(this).apply {
            hint = "Nome dello Spiritello"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Nuovo Spiritello")
            .setView(input)
            .setNegativeButton(
                "Annulla",
                null
            )
            .setPositiveButton(
                "Continua"
            ) { _, _ ->

                val name =
                    input.text
                        .toString()
                        .trim()

                if (name.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Inserisci un nome",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                showRarityDialog(
                    name
                )
            }
            .show()
    }

    // =========================================================
    // RARITÀ
    // =========================================================

    private fun showRarityDialog(
        name: String,
        existing: Spiritello? = null
    ) {

        val rarities = arrayOf(
            "Comune",
            "Non comune",
            "Raro",
            "Epico",
            "Leggendario",
            "Mitico"
        )

        val colors = intArrayOf(
            Color.rgb(170, 170, 170),
            Color.rgb(80, 220, 120),
            Color.rgb(70, 150, 255),
            Color.rgb(180, 90, 255),
            Color.rgb(255, 170, 40),
            Color.rgb(255, 70, 100)
        )

        var selected = 0

        if (existing != null) {
            selected =
                rarities.indexOf(
                    existing.rarity
                )

            if (selected < 0) {
                selected = 0
            }
        }

        val selectedColor =
            intArrayOf(
                if (existing != null) {
                    existing.rarityColor
                } else {
                    colors[0]
                }
            )

        val layout =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    20,
                    10,
                    20,
                    10
                )
            }

        val nameInput =
            EditText(this).apply {

                hint = "Nome"

                setText(name)

                setSingleLine(true)
            }

        layout.addView(
            nameInput,
            params(-1, 55)
        )

        val rarityText =
            TextView(this).apply {
                text =
                    "Rarità"

                textSize = 15f
                setTextColor(TEXT)

                setPadding(
                    4,
                    15,
                    4,
                    5
                )
            }

        layout.addView(rarityText)

        val rarityList =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        rarities.forEachIndexed {
                index,
                rarity ->

            val button =
                TextView(this).apply {

                    text =
                        "●  $rarity"

                    textSize = 16f

                    setTextColor(
                        colors[index]
                    )

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        14,
                        0,
                        14,
                        0
                    )

                    background =
                        rounded(
                            CARD,
                            14
                        )

                    setOnClickListener {

                        selected = index

                        selectedColor[0] =
                            colors[index]

                        for (
                            i in 0 until
                                rarityList.childCount
                        ) {
                            rarityList
                                .getChildAt(i)
                                .alpha =
                                if (i == selected) {
                                    1f
                                } else {
                                    0.45f
                                }
                        }
                    }
                }

            button.alpha =
                if (index == selected) {
                    1f
                } else {
                    0.45f
                }

            rarityList.addView(
                button,
                params(
                    -1,
                    48,
                    0,
                    4,
                    0,
                    4
                )
            )
        }

        layout.addView(rarityList)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Rarità")
                .setView(layout)
                .setNegativeButton(
                    "Annulla",
                    null
                )
                .setPositiveButton(
                    "Salva",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    val finalName =
                        nameInput.text
                            .toString()
                            .trim()

                    if (finalName.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Inserisci un nome",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (existing == null) {

                        spiritelli.add(
                            Spiritello(
                                name = finalName,
                                rarity =
                                    rarities[selected],
                                rarityColor =
                                    selectedColor[0]
                            )
                        )

                    } else {

                        existing.name =
                            finalName

                        existing.rarity =
                            rarities[selected]

                        existing.rarityColor =
                            selectedColor[0]
                    }

                    saveSpiritelli()

                    dialog.dismiss()

                    if (existing == null) {
                        showDeveloper()
                    } else {
                        manageSpiritelli()
                    }
                }
        }

        dialog.show()
    }

    // =========================================================
    // MANAGE
    // =========================================================

    private fun manageSpiritelli() {

        val root = createRoot()

        root.addView(
            secondaryButton(
                "‹  Indietro"
            ) {
                showDeveloper()
            },
            params(-1, 52)
        )

        root.addView(
            TextView(this).apply {
                text = "Gestisci Spiritelli"
                textSize = 26f
                setTextColor(TEXT)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            },
            params(-1, 45, 0, 10, 0, 10)
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        spiritelli.forEachIndexed {
                index,
                spiritello ->

            list.addView(
                manageCard(
                    index,
                    spiritello
                ),
                params(
                    -1,
                    82,
                    0,
                    0,
                    0,
                    10
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

        setContentView(root)
    }

    private fun manageCard(
        index: Int,
        spiritello: Spiritello
    ): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(
                CARD,
                18
            )
            setPadding(
                10,
                8,
                12,
                8
            )

            setOnClickListener {
                editSpiritello(index)
            }
        }

        val image = ImageView(this).apply {
            scaleType =
                ImageView.ScaleType.CENTER_CROP

            background =
                rounded(
                    IMAGE_BG,
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
            params(64, 64, 0, 0, 12, 0)
        )

        val text = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        text.addView(
            TextView(this).apply {
                this.text =
                    spiritello.name

                textSize = 16f
                setTextColor(TEXT)
                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        text.addView(
            TextView(this).apply {
                this.text =
                    spiritello.rarity

                textSize = 12f
                setTextColor(
                    spiritello.rarityColor
                )
            }
        )

        card.addView(
            text,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        card.addView(
            TextView(this).apply {
                text = "›"
                textSize = 26f
                setTextColor(SECONDARY)
            }
        )

        return card
    }

    // =========================================================
    // EDIT
    // =========================================================

    private fun editSpiritello(
        index: Int
    ) {

        if (index !in spiritelli.indices) {
            return
        }

        val spiritello =
            spiritelli[index]

        val options = arrayOf(
            "Modifica nome e rarità",
            "Cambia foto",
            "Elimina Spiritello"
        )

        AlertDialog.Builder(this)
            .setTitle(
                spiritello.name
            )
            .setItems(
                options
            ) { _, choice ->

                when (choice) {

                    0 -> {
                        showRarityDialog(
                            spiritello.name,
                            spiritello
                        )
                    }

                    1 -> {
                        choosePhoto(index)
                    }

                    2 -> {
                        deleteSpiritello(index)
                    }
                }
            }
            .show()
    }

    // =========================================================
    // DELETE
    // =========================================================

    private fun deleteSpiritello(
        index: Int
    ) {

        if (index !in spiritelli.indices) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminare?")
            .setMessage(
                "Questo Spiritello verrà rimosso."
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

    // =========================================================
    // PHOTO
    // =========================================================

    private fun choosePhoto(
        index: Int
    ) {

        photoIndex = index

        val intent =
            Intent(
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

        val uri = data?.data ?: return

        if (
            photoIndex < 0 ||
            photoIndex >= spiritelli.size
        ) {
            return
        }

        runCatching {
            contentResolver
                .takePersistableUriPermission(
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

    // =========================================================
    // SAVE
    // =========================================================

    private fun saveSpiritelli() {

        val editor = prefs.edit()

        editor.clear()

        editor.putInt(
            "count",
            spiritelli.size
        )

        spiritelli.forEachIndexed {
                index,
                spiritello ->

            editor.putString(
                "name_$index",
                spiritello.name
            )

            editor.putString(
                "image_$index",
                spiritello.imageUri
            )

            editor.putString(
                "rarity_$index",
                spiritello.rarity
            )

            editor.putInt(
                "rarityColor_$index",
                spiritello.rarityColor
            )

            editor.putBoolean(
                "collected_$index",
                spiritello.collected
            )
        }

        editor.apply()
    }

    // =========================================================
    // LOAD
    // =========================================================

    private fun loadSpiritelli() {

        spiritelli.clear()

        val count =
            prefs.getInt(
                "count",
                0
            )

        repeat(count) { index ->

            val name =
                prefs.getString(
                    "name_$index",
                    "Spiritello"
                ) ?: "Spiritello"

            val image =
                prefs.getString(
                    "image_$index",
                    null
                )

            val rarity =
                prefs.getString(
                    "rarity_$index",
                    "Comune"
                ) ?: "Comune"

            val rarityColor =
                prefs.getInt(
                    "rarityColor_$index",
                    Color.rgb(
                        160,
                        160,
                        160
                    )
                )

            val collected =
                prefs.getBoolean(
                    "collected_$index",
                    false
                )

            spiritelli.add(
                Spiritello(
                    name = name,
                    imageUri = image,
                    rarity = rarity,
                    rarityColor = rarityColor,
                    collected = collected
                )
            )
        }
    }

    // =========================================================
    // X/X
    // =========================================================

    private fun completionText(): String {

        val found =
            spiritelli.count {
                it.collected
            }

        val total =
            spiritelli.size

        return "$found/$total"
    }

    // =========================================================
    // UI
    // =========================================================

    private fun createRoot(): LinearLayout {

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                20,
                22,
                20,
                20
            )

            setBackgroundColor(BG)
        }
    }

    private fun primaryButton(
        textValue: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {

            text = textValue

            textSize = 15f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            background =
                rounded(
                    PURPLE,
                    18
                )

            setOnClickListener {
                action()
            }
        }
    }

    private fun secondaryButton(
        textValue: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {

            text = textValue

            textSize = 15f

            gravity = Gravity.CENTER

            setTextColor(TEXT)

            background =
                rounded(
                    CARD,
                    18
                )

            setOnClickListener {
                action()
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
