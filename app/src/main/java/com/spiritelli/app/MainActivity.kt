package com.spiritelli.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
    var rarityColor: Int = Color.rgb(170, 170, 170),
    var collected: Boolean = false
)

class MainActivity : Activity() {

    companion object {
        private const val PREFS_NAME = "spiritelli"
        private const val PHOTO_REQUEST = 42
        private const val DEVELOPER_TRIGGER = "kira"

        private val BG = Color.rgb(10, 10, 14)
        private val CARD = Color.rgb(22, 22, 28)
        private val CARD_FOUND = Color.rgb(100, 255, 100)
        private val IMAGE_BG = Color.rgb(34, 31, 45)

        private val TEXT = Color.rgb(245, 245, 248)
        private val TEXT_DARK = Color.rgb(15, 15, 18)
        private val SECONDARY = Color.rgb(150, 150, 165)

        private val PRIMARY = Color.rgb(125, 85, 235)
        private val BORDER = Color.rgb(45, 45, 55)
        private val GREEN = Color.rgb(65, 255, 100)
    }

    private val spiritelli = mutableListOf<Spiritello>()

    private val prefs by lazy {
        getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )
    }

    private lateinit var collectionList: LinearLayout
    private lateinit var completionTextView: TextView

    private var photoIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = BG
        window.navigationBarColor = BG

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

        root.addView(
            ZeroPointView(this),
            linearParams(
                dp(150),
                dp(150)
            )
        )

        root.addView(
            TextView(this).apply {
                text = "SPIRITELLI"
                textSize = 27f
                setTextColor(TEXT)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            },
            linearParams(
                -1,
                dp(48)
            )
        )

        root.addView(
            TextView(this).apply {
                text = "PUNTO ZERO"
                textSize = 11f
                setTextColor(SECONDARY)
                gravity = Gravity.CENTER
            },
            linearParams(
                -1,
                dp(30)
            )
        )

        setContentView(root)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                showHome()
            },
            1000L
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

        header.addView(
            ZeroPointView(this),
            linearParams(
                dp(52),
                dp(52)
            )
        )

        val headerText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(12),
                0,
                0,
                0
            )
        }

        headerText.addView(
            TextView(this).apply {
                text = "Spiritelli"
                textSize = 25f
                setTextColor(TEXT)
                typeface = Typeface.DEFAULT_BOLD
            }
        )

        headerText.addView(
            TextView(this).apply {
                text = "La tua collezione Fortnite"
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
            margins(
                -1,
                dp(56),
                0,
                0,
                0,
                dp(18)
            )
        )

        // Ricerca

        val search = EditText(this).apply {
            hint = "Cerca uno Spiritello..."
            setHintTextColor(
                Color.rgb(
                    125,
                    125,
                    140
                )
            )
            textSize = 17f
            setTextColor(TEXT)
            setSingleLine(true)

            background = rounded(
                CARD,
                22
            )

            setPadding(
                dp(22),
                0,
                dp(22),
                0
            )
        }

        root.addView(
            search,
            margins(
                -1,
                dp(64),
                0,
                0,
                0,
                dp(8)
            )
        )

        // X/X

        completionTextView = TextView(this).apply {
            text = completionText()
            textSize = 15f
            setTextColor(GREEN)
            gravity = Gravity.END
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            completionTextView,
            margins(
                -1,
                dp(28),
                0,
                0,
                0,
                dp(8)
            )
        )

        // Lista

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
                ) {
                }

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

                    if (value == DEVELOPER_TRIGGER) {
                        showDeveloper()
                        return
                    }

                    renderCollection(
                        s?.toString().orEmpty()
                    )

                    completionTextView.text =
                        completionText()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    // =========================================================
    // COLLECTION
    // =========================================================

    private fun renderCollection(
        query: String
    ) {

        collectionList.removeAllViews()

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
                    dp(20),
                    dp(60),
                    dp(20),
                    dp(60)
                )
            }

            empty.addView(
                TextView(this).apply {
                    text =
                        if (spiritelli.isEmpty()) {
                            "La collezione è vuota"
                        } else {
                            "Nessuno Spiritello trovato"
                        }

                    textSize = 18f
                    setTextColor(TEXT)
                    gravity = Gravity.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
            )

            empty.addView(
                TextView(this).apply {
                    text =
                        if (spiritelli.isEmpty()) {
                            "Aggiungi gli Spiritelli dalla modalità Developer."
                        } else {
                            "Prova con un altro nome."
                        }

                    textSize = 13f
                    setTextColor(SECONDARY)
                    gravity = Gravity.CENTER
                }
            )

            collectionList.addView(
                empty,
                linearParams(
                    -1,
                    dp(180)
                )
            )

            return
        }

        var index = 0

        while (index < filtered.size) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            row.addView(
                createCollectionCard(
                    filtered[index]
                ),
                LinearLayout.LayoutParams(
                    0,
                    dp(245),
                    1f
                ).apply {
                    setMargins(
                        0,
                        0,
                        dp(5),
                        dp(10)
                    )
                }
            )

            if (index + 1 < filtered.size) {

                row.addView(
                    createCollectionCard(
                        filtered[index + 1]
                    ),
                    LinearLayout.LayoutParams(
                        0,
                        dp(245),
                        1f
                    ).apply {
                        setMargins(
                            dp(5),
                            0,
                            0,
                            dp(10)
                        )
                    }
                )

            } else {

                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(
                        0,
                        dp(245),
                        1f
                    )
                )
            }

            collectionList.addView(row)

            index += 2
        }
    }

    // =========================================================
    // COLLECTION CARD
    // =========================================================

    private fun createCollectionCard(
        spiritello: Spiritello
    ): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(9),
                dp(9),
                dp(9),
                dp(8)
            )

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
            linearParams(
                -1,
                dp(160)
            )
        )

        val name = TextView(this).apply {
            text = spiritello.name
            textSize = 16f
            setTextColor(
                if (spiritello.collected) {
                    TEXT_DARK
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
            margins(
                -1,
                dp(30),
                0,
                dp(6),
                0,
                0
            )
        )

        val rarity = TextView(this).apply {
            text = spiritello.rarity
            textSize = 13f
            setTextColor(
                if (spiritello.collected) {
                    TEXT_DARK
                } else {
                    spiritello.rarityColor
                }
            )
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
        }

        card.addView(
            rarity,
            linearParams(
                -1,
                dp(25)
            )
        )

        return card
    }

    // =========================================================
    // FOUND
    // =========================================================

    private fun toggleCollected(
        spiritello: Spiritello
    ) {

        spiritello.collected =
            !spiritello.collected

        saveSpiritelli()

        if (::completionTextView.isInitialized) {
            completionTextView.text =
                completionText()
        }

        renderCollection("")
    }

    // =========================================================
    // DETAILS
    // =========================================================

    private fun showDetails(
        spiritello: Spiritello
    ) {

        val root = createRoot()

        root.addView(
            secondaryButton(
                "‹  Indietro"
            ) {
                showHome()
            },
            linearParams(
                -1,
                dp(52)
            )
        )

        val image = ImageView(this).apply {

            scaleType =
                ImageView.ScaleType.CENTER_CROP

            background =
                rounded(
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
            margins(
                -1,
                dp(300),
                0,
                dp(14),
                0,
                dp(18)
            )
        )

        root.addView(
            TextView(this).apply {
                text = spiritello.name
                textSize = 28f
                setTextColor(TEXT)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
        )

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
            linearParams(
                -1,
                dp(35)
            )
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
            margins(
                -1,
                dp(55),
                0,
                dp(18),
                0,
                0
            )
        )

        setContentView(root)
    }

    // =========================================================
    // DEVELOPER
    // =========================================================

    private fun showDeveloper() {

        val root = createRoot()

        root.addView(
            TextView(this).apply {
                text = "Developer"
                textSize = 28f
                setTextColor(TEXT)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
        )

        root.addView(
            TextView(this).apply {
                text = "Gestisci gli Spiritelli"
                textSize = 14f
                setTextColor(SECONDARY)
                gravity = Gravity.CENTER
            },
            margins(
                -1,
                dp(30),
                0,
                0,
                0,
                dp(22)
            )
        )

        root.addView(
            primaryButton(
                "＋  Aggiungi Spiritello"
            ) {
                addSpiritello()
            },
            margins(
                -1,
                dp(56),
                0,
                0,
                0,
                dp(10)
            )
        )

        root.addView(
            secondaryButton(
                "✏  Gestisci Spiritelli"
            ) {
                manageSpiritelli()
            },
            margins(
                -1,
                dp(56),
                0,
                0,
                0,
                dp(10)
            )
        )

        root.addView(
            secondaryButton(
                "←  Torna alla collezione"
            ) {
                showHome()
            },
            linearParams(
                -1,
                dp(56)
            )
        )

        setContentView(root)
    }

    // =========================================================
    // ADD
    // =========================================================

    private fun addSpiritello() {

        val nameInput = EditText(this).apply {
            hint = "Nome dello Spiritello"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Nuovo Spiritello")
            .setView(nameInput)
            .setNegativeButton(
                "Annulla",
                null
            )
            .setPositiveButton(
                "Continua"
            ) { _, _ ->

                val name =
                    nameInput.text
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

                showRarityDialog(
                    name,
                    null
                )
            }
            .show()
    }

    // =========================================================
    // RARITY
    // =========================================================

    private fun showRarityDialog(
        initialName: String,
        existing: Spiritello?
    ) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(16),
                dp(4),
                dp(16),
                dp(4)
            )
        }

        val nameInput = EditText(this).apply {
            hint = "Nome"
            setSingleLine(true)
            setText(initialName)
        }

        layout.addView(
            nameInput,
            linearParams(
                -1,
                dp(54)
            )
        )

        val rarityInput = EditText(this).apply {
            hint = "Rarità"
            setSingleLine(true)
            setText(
                existing?.rarity
                    ?: "Comune"
            )
        }

        layout.addView(
            rarityInput,
            margins(
                -1,
                dp(54),
                0,
                dp(8),
                0,
                0
            )
        )

        layout.addView(
            TextView(this).apply {
                text = "Colore della rarità"
                textSize = 14f
                setTextColor(TEXT)
                setPadding(
                    dp(4),
                    dp(10),
                    dp(4),
                    dp(6)
                )
            }
        )

        val colorOptions =
            listOf(
                Pair(
                    "Grigio",
                    Color.rgb(
                        170,
                        170,
                        170
                    )
                ),
                Pair(
                    "Verde",
                    Color.rgb(
                        70,
                        230,
                        120
                    )
                ),
                Pair(
                    "Blu",
                    Color.rgb(
                        70,
                        150,
                        255
                    )
                ),
                Pair(
                    "Viola",
                    Color.rgb(
                        180,
                        90,
                        255
                    )
                ),
                Pair(
                    "Arancione",
                    Color.rgb(
                        255,
                        170,
                        40
                    )
                ),
                Pair(
                    "Rosso",
                    Color.rgb(
                        255,
                        80,
                        100
                    )
                )
            )

        var selectedColor =
            existing?.rarityColor
                ?: colorOptions[0].second

        val colorList = LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
        }

        for (
            i in colorOptions.indices
        ) {

            val option =
                colorOptions[i]

            val button =
                TextView(this).apply {

                    text =
                        "●  ${option.first}"

                    textSize = 15f

                    setTextColor(
                        option.second
                    )

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        dp(14),
                        0,
                        dp(14),
                        0
                    )

                    background =
                        rounded(
                            CARD,
                            14
                        )

                    alpha =
                        if (
                            selectedColor ==
                            option.second
                        ) {
                            1f
                        } else {
                            0.45f
                        }

                    setOnClickListener {

                        selectedColor =
                            option.second

                        for (
                            childIndex in
                            0 until
                                colorList.childCount
                        ) {

                            colorList
                                .getChildAt(
                                    childIndex
                                )
                                .alpha =
                                if (
                                    childIndex == i
                                ) {
                                    1f
                                } else {
                                    0.45f
                                }
                        }
                    }
                }

            colorList.addView(
                option,
                linearParams(
                    -1,
                    dp(44)
                )
            )
        }

        layout.addView(colorList)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    if (existing == null) {
                        "Rarità Spiritello"
                    } else {
                        "Modifica Spiritello"
                    }
                )
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

                    val finalRarity =
                        rarityInput.text
                            .toString()
                            .trim()

                    if (finalName.isEmpty()) {

                        Toast.makeText(
                            this,
                            "Inserisci un nome.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    if (finalRarity.isEmpty()) {

                        Toast.makeText(
                            this,
                            "Inserisci una rarità.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    if (existing == null) {

                        spiritelli.add(
                            Spiritello(
                                name =
                                    finalName,
                                rarity =
                                    finalRarity,
                                rarityColor =
                                    selectedColor,
                                collected =
                                    false
                            )
                        )

                    } else {

                        existing.name =
                            finalName

                        existing.rarity =
                            finalRarity

                        existing.rarityColor =
                            selectedColor
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
            linearParams(
                -1,
                dp(52)
            )
        )

        root.addView(
            TextView(this).apply {
                text = "Gestisci Spiritelli"
                textSize = 26f
                setTextColor(TEXT)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            },
            margins(
                -1,
                dp(48),
                0,
                dp(8),
                0,
                dp(8)
            )
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
        }

        if (spiritelli.isEmpty()) {

            list.addView(
                TextView(this).apply {
                    text =
                        "Nessuno Spiritello presente."
                    textSize = 15f
                    setTextColor(SECONDARY)
                    gravity = Gravity.CENTER
                },
                linearParams(
                    -1,
                    dp(150)
                )
            )

        } else {

            for (
                i in spiritelli.indices
            ) {

                list.addView(
                    createManageCard(
                        i,
                        spiritelli[i]
                    ),
                    margins(
                        -1,
                        dp(84),
                        0,
                        0,
                        0,
                        dp(10)
                    )
                )
            }
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

    private fun createManageCard(
        index: Int,
        spiritello: Spiritello
    ): View {

        val card = LinearLayout(this).apply {

            orientation =
                LinearLayout.HORIZONTAL

            gravity =
                Gravity.CENTER_VERTICAL

            setPadding(
                dp(10),
                dp(8),
                dp(12),
                dp(8)
            )

            background =
                rounded(
                    if (spiritello.collected) {
                        CARD_FOUND
                    } else {
                        CARD
                    },
                    18
                )

            setOnClickListener {
                showEditMenu(index)
            }
        }

        val image =
            ImageView(this).apply {

                scaleType =
                    ImageView.ScaleType.CENTER_CROP

                background =
                    rounded(
                        IMAGE_BG,
                        14
                    )

                if (
                    spiritello.imageUri != null
                ) {

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
            margins(
                dp(64),
                dp(64),
                0,
                0,
                dp(12),
                0
            )
        )

        val infoBox =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        infoBox.addView(
            TextView(this).apply {
                text =
                    spiritello.name
                textSize = 16f

                setTextColor(
                    if (spiritello.collected) {
                        TEXT_DARK
                    } else {
                        TEXT
                    }
                )

                typeface =
                    Typeface.DEFAULT_BOLD
            }
        )

        infoBox.addView(
            TextView(this).apply {
                text =
                    spiritello.rarity
                textSize = 12f

                setTextColor(
                    if (spiritello.collected) {
                        TEXT_DARK
                    } else {
                        spiritello.rarityColor
                    }
                )
            }
        )

        card.addView(
            infoBox,
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
                setTextColor(
                    if (spiritello.collected) {
                        TEXT_DARK
                    } else {
                        SECONDARY
                    }
                )
            }
        )

        return card
    }

    // =========================================================
    // EDIT MENU
    // =========================================================

    private fun showEditMenu(
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
            "Segna trovato / non trovato",
            "Elimina Spiritello"
        )

        AlertDialog.Builder(this)
            .setTitle(
                spiritello.name
            )
            .setItems(
                options
            ) { _, which ->

                when (which) {

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
                        toggleCollected(
                            spiritello
                        )
                        manageSpiritelli()
                    }

                    3 -> {
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
            .setTitle(
                "Eliminare Spiritello?"
            )
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

        val uri =
            data?.data ?: return

        if (
            photoIndex < 0 ||
            photoIndex >= spiritelli.size
        ) {
            photoIndex = -1
            return
        }

        runCatching {
            contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
        }

        spiritelli[
            photoIndex
        ].imageUri =
            uri.toString()

        saveSpiritelli()

        photoIndex = -1

        manageSpiritelli()
    }

    // =========================================================
    // SAVE
    // =========================================================

    private fun saveSpiritelli() {

        val editor =
            prefs.edit()

        editor.clear()

        editor.putInt(
            "count",
            spiritelli.size
        )

        for (
            i in spiritelli.indices
        ) {

            val spiritello =
                spiritelli[i]

            editor.putString(
                "name_$i",
                spiritello.name
            )

            editor.putString(
                "image_$i",
                spiritello.imageUri
            )

            // Compatibilità con la vecchia versione
            editor.putString(
                "img_$i",
                spiritello.imageUri
            )

            editor.putString(
                "rarity_$i",
                spiritello.rarity
            )

            editor.putInt(
                "rarityColor_$i",
                spiritello.rarityColor
            )

            editor.putBoolean(
                "collected_$i",
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

        for (
            i in 0 until count
        ) {

            val name =
                prefs.getString(
                    "name_$i",
                    "Spiritello"
                ) ?: "Spiritello"

            val image =
                prefs.getString(
                    "image_$i",
                    prefs.getString(
                        "img_$i",
                        null
                    )
                )

            val rarity =
                prefs.getString(
                    "rarity_$i",
                    "Comune"
                ) ?: "Comune"

            val rarityColor =
                prefs.getInt(
                    "rarityColor_$i",
                    Color.rgb(
                        170,
                        170,
                        170
                    )
                )

            val collected =
                prefs.getBoolean(
                    "collected_$i",
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
    // X / X
    // =========================================================

    private fun completionText(): String {

        var found = 0

        for (spiritello in spiritelli) {
            if (spiritello.collected) {
                found++
            }
        }

        return "$found/${spiritelli.size}"
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private fun createRoot(): LinearLayout {

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                dp(20),
                dp(22),
                dp(20),
                dp(20)
            )

            setBackgroundColor(
                BG
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

            gravity =
                Gravity.CENTER

            setTextColor(
                Color.WHITE
            )

            background =
                rounded(
                    PRIMARY,
                    18
                )

            setOnClickListener {
                action()
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

            gravity =
                Gravity.CENTER

            setTextColor(
                TEXT
            )

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

    private fun linearParams(
        width: Int,
        height: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            width,
            height
        )
    }

    private fun margins(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
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

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    // =========================================================
    // PUNTO ZERO
    // =========================================================

    private class ZeroPointView(
        context: Context
    ) : View(context) {

        private val outerPaint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private val innerPaint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private val centerPaint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private val diamondPaint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        init {

            outerPaint.style =
                Paint.Style.STROKE

            outerPaint.strokeWidth = 4f

            outerPaint.color =
                Color.rgb(
                    125,
                    85,
                    235
                )

            innerPaint.style =
                Paint.Style.STROKE

            innerPaint.strokeWidth = 2.5f

            innerPaint.color =
                Color.rgb(
                    190,
                    160,
                    255
                )

            centerPaint.style =
                Paint.Style.FILL

            centerPaint.color =
                Color.rgb(
                    95,
                    55,
                    205
                )

            diamondPaint.style =
                Paint.Style.FILL

            diamondPaint.color =
                Color.rgb(
                    180,
                    145,
                    255
                )
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val centerX =
                width / 2f

            val centerY =
                height / 2f

            val smallest =
                if (width < height) {
                    width
                } else {
                    height
                }

            val radius =
                smallest * 0.30f

            canvas.drawCircle(
                centerX,
                centerY,
                radius,
                outerPaint
            )

            canvas.drawCircle(
                centerX,
                centerY,
                radius * 0.66f,
                innerPaint
            )

            canvas.drawCircle(
                centerX,
                centerY,
                radius * 0.25f,
                centerPaint
            )

            val path =
                Path()

            path.moveTo(
                centerX,
                centerY - radius * 0.92f
            )

            path.lineTo(
                centerX +
                    radius * 0.16f,
                centerY -
                    radius * 0.48f
            )

            path.lineTo(
                centerX,
                centerY -
                    radius * 0.24f
            )

            path.lineTo(
                centerX -
                    radius * 0.16f,
                centerY -
                    radius * 0.48f
            )

            path.close()

            canvas.drawPath(
                path,
                diamondPaint
            )
        }
    }
}
