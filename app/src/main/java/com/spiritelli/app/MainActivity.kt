package com.spiritelli.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

        private val BACKGROUND = Color.rgb(10, 10, 14)
        private val CARD = Color.rgb(22, 22, 28)
        private val CARD_FOUND = Color.rgb(100, 255, 100)
        private val IMAGE_BACKGROUND = Color.rgb(34, 31, 45)

        private val TEXT = Color.rgb(245, 245, 248)
        private val TEXT_DARK = Color.rgb(15, 15, 18)
        private val SECONDARY = Color.rgb(150, 150, 165)

        private val PRIMARY = Color.rgb(125, 85, 235)
        private val BORDER = Color.rgb(44, 44, 54)
        private val GREEN = Color.rgb(65, 255, 100)
    }

    private val spiritelli = mutableListOf<Spiritello>()

    private val prefs by lazy {
        getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )
    }

    private var photoIndex = -1

    private lateinit var collectionList: LinearLayout
    private lateinit var completionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = BACKGROUND
        window.navigationBarColor = BACKGROUND

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
            setBackgroundColor(BACKGROUND)
        }

        root.addView(
            ZeroPointView(this),
            params(
                width = dp(150),
                height = dp(150)
            )
        )

        val title = TextView(this).apply {
            text = "SPIRITELLI"
            textSize = 27f
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            title,
            params(
                width = -1,
                height = dp(48)
            )
        )

        val subtitle = TextView(this).apply {
            text = "PUNTO ZERO"
            textSize = 11f
            setTextColor(SECONDARY)
            gravity = Gravity.CENTER
        }

        root.addView(
            subtitle,
            params(
                width = -1,
                height = dp(30)
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
            params(
                width = dp(52),
                height = dp(52)
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

        val title = TextView(this).apply {
            text = "Spiritelli"
            textSize = 25f
            setTextColor(TEXT)
            typeface = Typeface.DEFAULT_BOLD
        }

        headerText.addView(title)

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
            params(
                width = -1,
                height = dp(56),
                bottom = dp(18)
            )
        )

        // -----------------------------------------------------
        // SEARCH
        // -----------------------------------------------------

        val search = EditText(this).apply {
            hint = "Cerca uno Spiritello..."
            setHintTextColor(
                Color.rgb(125, 125, 140)
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
            params(
                width = -1,
                height = dp(64),
                bottom = dp(8)
            )
        )

        // -----------------------------------------------------
        // X / X
        // -----------------------------------------------------

        completionTextView = TextView(this).apply {
            text = completionText()
            textSize = 15f
            setTextColor(GREEN)
            gravity = Gravity.END
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            completionTextView,
            params(
                width = -1,
                height = dp(28),
                bottom = dp(8)
            )
        )

        // -----------------------------------------------------
        // LIST
        // -----------------------------------------------------

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
                    typeface =
                        Typeface.DEFAULT_BOLD
                }
            )

            if (spiritelli.isEmpty()) {
                empty.addView(
                    TextView(this).apply {
                        text =
                            "Aggiungi gli Spiritelli dalla modalità Developer."
                        textSize = 13f
                        setTextColor(SECONDARY)
                        gravity = Gravity.CENTER
                    }
                )
            }

            collectionList.addView(
                empty,
                params(
                    width = -1,
                    height = dp(180)
                )
            )

            return
        }

        var index = 0

        while (index < filtered.size) {

            val row = LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
            }

            val first =
                createCollectionCard(
                    filtered[index]
                )

            row.addView(
                first,
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

                val second =
                    createCollectionCard(
                        filtered[index + 1]
                    )

                row.addView(
                    second,
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

            scaleType =
                ImageView.ScaleType.CENTER_CROP

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
                width = -1,
                height = dp(160)
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
            params(
                width = -1,
                height = dp(30),
                top = dp(6)
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
            params(
                width = -1,
                height = dp(25)
            )
        )

        return card
    }

    // =========================================================
    // FOUND / NOT FOUND
    // =========================================================

    private fun toggleCollected(
        spiritello: Spiritello
    ) {

        spiritello.collected =
            !spiritello.collected

        saveSpiritelli()

        completionTextView.text =
            completionText()

        val currentSearch = ""

        renderCollection(
            currentSearch
        )
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
            params(
                width = -1,
                height = dp(52)
            )
        )

        val image = ImageView(this).apply {

            scaleType =
                ImageView.ScaleType.CENTER_CROP

            background = rounded(
                IMAGE_BACKGROUND,
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
            params(
                width = -1,
                height = dp(300),
                top = dp(14),
                bottom = dp(18)
            )
        )

        val title = TextView(this).apply {
            text = spiritello.name
            textSize = 28f
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(title)

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
            params(
                width = -1,
                height = dp(35),
                top = dp(4)
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
            params(
                width = -1,
                height = dp(55),
                top = dp(18)
            )
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
                text = "Gestisci gli Spiritelli"
                textSize = 14f
                setTextColor(SECONDARY)
                gravity = Gravity.CENTER
            },
            params(
                width = -1,
                height = dp(30),
                bottom = dp(22)
            )
        )

        root.addView(
            primaryButton(
                "＋  Aggiungi Spiritello"
            ) {
                addSpiritello()
            },
            params(
                width = -1,
                height = dp(56),
                bottom = dp(10)
            )
        )

        root.addView(
            secondaryButton(
                "✏  Gestisci Spiritelli"
            ) {
                manageSpiritelli()
            },
            params(
                width = -1,
                height = dp(56),
                bottom = dp(10)
            )
        )

        root.addView(
            secondaryButton(
                "←  Torna alla collezione"
            ) {
                showHome()
            },
            params(
                width = -1,
                height = dp(56)
            )
        )

        setContentView(root)
    }

    // =========================================================
    // ADD SPIRITELLO
    // =========================================================

    private fun addSpiritello() {

        val nameInput = EditText(this).apply {
            hint = "Nome dello Spiritello"
            setSingleLine(true)
            setTextColor(TEXT)
            setHintTextColor(SECONDARY)
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

                showRarityEditor(
                    name,
                    null
                )
            }
            .show()
    }

    // =========================================================
    // RARITY EDITOR
    // =========================================================

    private fun showRarityEditor(
        initialName: String,
        existing: Spiritello?
    ) {

        val layout =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

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
            setTextColor(TEXT)
            setHintTextColor(SECONDARY)
        }

        layout.addView(
            nameInput,
            params(
                width = -1,
                height = dp(54)
            )
        )

        val rarityInput = EditText(this).apply {
            hint = "Rarità"
            setSingleLine(true)
            setText(
                existing?.rarity ?: "Comune"
            )
            setTextColor(TEXT)
            setHintTextColor(SECONDARY)
        }

        layout.addView(
            rarityInput,
            params(
                width = -1,
                height = dp(54),
                top = dp(8)
            )
        )

        layout.addView(
            TextView(this).apply {
                text = "Colore della rarità"
                textSize = 14f
                setTextColor(TEXT)
                setPadding(
                    dp(4),
                    dp(14),
                    dp(4),
                    dp(6)
                )
            }
        )

        var selectedColor =
            existing?.rarityColor
                ?: Color.rgb(170, 170, 170)

        val colors =
            listOf(
                "Grigio" to Color.rgb(
                    170,
                    170,
                    170
                ),
                "Verde" to Color.rgb(
                    70,
                    230,
                    120
                ),
                "Blu" to Color.rgb(
                    70,
                    150,
                    255
                ),
                "Viola" to Color.rgb(
                    180,
                    90,
                    255
                ),
                "Arancione" to Color.rgb(
                    255,
                    170,
                    40
                ),
                "Rosso" to Color.rgb(
                    255,
                    80,
                    100
                )
            )

        val colorContainer =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        colors.forEach { pair ->

            val colorName = pair.first
            val colorValue = pair.second

            val colorButton =
                TextView(this).apply {

                    text =
                        "●  $colorName"

                    textSize = 15f

                    setTextColor(
                        colorValue
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
                            colorValue
                        ) {
                            1f
                        } else {
                            0.45f
                        }

                    setOnClickListener {

                        selectedColor =
                            colorValue

                        for (
                            i in 0 until
                                colorContainer.childCount
                        ) {

                            val child =
                                colorContainer
                                    .getChildAt(i)

                            child.alpha =
                                if (
                                    i ==
                                    colors.indexOf(pair)
                                ) {
                                    1f
                                } else {
                                    0.45f
                                }
                        }
                    }
                }

            colorContainer.addView(
                colorButton,
                params(
                    width = -1,
                    height = dp(44),
                    bottom = dp(5)
                )
            )
        }

        layout.addView(
            colorContainer
        )

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
                                name = finalName,
                                rarity = finalRarity,
                                rarityColor =
                                    selectedColor,
                                collected = false
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
            params(
                width = -1,
                height = dp(52)
            )
        )

        val title = TextView(this).apply {
            text = "Gestisci Spiritelli"
            textSize = 26f
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            title,
            params(
                width = -1,
                height = dp(48),
                top = dp(8),
                bottom = dp(8)
            )
        )

        val scroll =
            ScrollView(this)

        val list =
            LinearLayout(this).apply {
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
                params(
                    width = -1,
                    height = dp(150)
                )
            )

        } else {

            spiritelli.forEachIndexed {
                index,
                spiritello ->

                list.addView(
                    createManageCard(
                        index,
                        spiritello
                    ),
                    params(
                        width = -1,
                        height = dp(84),
                        bottom = dp(10)
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

        val card =
            LinearLayout(this).apply {

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
                        IMAGE_BACKGROUND,
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
            params(
                width = dp(64),
                height = dp(64),
                right = dp(12)
            )
        )

        val infoBox =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val name =
            TextView(this).apply {
                text = spiritello.name
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

        infoBox.addView(name)

        val rarity =
            TextView(this).apply {
                text = spiritello.rarity
                textSize = 12f
                setTextColor(
                    if (spiritello.collected) {
                        TEXT_DARK
                    } else {
                        spiritello.rarityColor
                    }
                )
            }

        infoBox.addView(rarity)

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

        val options = arrayOf(
            "Modifica nome e rarità",
            "Cambia foto",
            "Segna come trovato / non trovato",
            "Elimina Spiritello"
        )

        AlertDialog.Builder(this)
            .setTitle(
                spiritelli[index].name
            )
            .setItems(
                options
            ) { _, which ->

                when (which) {

                    0 -> {
                        val spiritello =
                            spiritelli[index]

                        showRarityEditor(
                            spiritello.name,
                            spiritello
                        )
                    }

                    1 -> {
                        choosePhoto(index)
                    }

                    2 -> {
                        toggleCollected(
                            spiritelli[index]
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
            .setTitle("Eliminare Spiritello?")
            .setMessage(
                "Questo Spiritello verrà rimosso dalla collezione."
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

        spiritelli[photoIndex].imageUri =
            uri.toString()

        saveSpiritelli()

        photoIndex = -1

        manageSpiritelli()
    }

    // =========================================================
    // STORAGE
    // =========================================================

    private fun saveSpiritelli() {

        val editor =
            prefs.edit()

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
                "img_$index",
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
                    prefs.getString(
                        "img_$index",
                        null
                    )
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
                        170,
                        170,
                        170
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
    // COMPLETION X/X
    // =========================================================

    private fun completionText(): String {

        val found =
            spiritelli.count {
                it.collected
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
                BACKGROUND
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

    private fun dp(value: Int): Int {
        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    // =========================================================
    // PUNTO ZERO ICON
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

        init {

            outerPaint.style =
                Paint.Style.STROKE

            outerPaint.strokeWidth =
                4f

            outerPaint.color =
                Color.rgb(
                    125,
                    85,
                    235
                )

            innerPaint.style =
                Paint.Style.STROKE

            innerPaint.strokeWidth =
                2.5f

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
                    100,
                    60,
                    210
                )
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val cx =
                width / 2f

            val cy =
                height / 2f

            val radius =
                min(
                    width,
                    height
                ) *
                    0.30f

            canvas.drawCircle(
                cx,
                cy,
                radius,
                outerPaint
            )

            canvas.drawCircle(
                cx,
                cy,
                radius * 0.66f,
                innerPaint
            )

            canvas.drawCircle(
                cx,
                cy,
                radius * 0.25f,
                centerPaint
            )

            val path =
                android.graphics.Path()

            path.moveTo(
                cx,
                cy - radius * 0.93f
            )

            path.lineTo(
                cx + radius * 0.15f,
                cy - radius * 0.50f
            )

            path.lineTo(
                cx,
                cy - radius * 0.25f
            )

            path.lineTo(
                cx - radius * 0.15f,
                cy - radius * 0.50f
            )

            path.close()

            val diamondPaint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            diamondPaint.style =
                Paint.Style.FILL

            diamondPaint.color =
                Color.rgb(
                    175,
                    140,
                    255
                )

            canvas.drawPath(
                path,
                diamondPaint
            )
        }
    }
}
