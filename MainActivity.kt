package com.spiritelli.app

import android.app.*
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.widget.*
import java.util.Locale

data class Spiritello(var name: String, var imageUri: String? = null)

class MainActivity : Activity() {
    private val developerCode = "131013"
    private val spiritelli = mutableListOf<Spiritello>()
    private val prefs by lazy { getSharedPreferences("spiritelli", MODE_PRIVATE) }
    private lateinit var list: LinearLayout
    private var photoIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()
        home()
    }

    private fun home() {
        val root = root()
        root.addView(label("👻", 52f, Color.rgb(118,84,232), Gravity.CENTER))
        root.addView(label("SPIRITELLI", 30f, Color.rgb(35,32,48), Gravity.CENTER))
        root.addView(label("La tua raccolta di Spiritelli", 15f, Color.GRAY, Gravity.CENTER))
        val search = EditText(this).apply {
            hint = "🔎  Cerca uno Spiritello..."
            singleLine = true
            textSize = 16f
            background = rounded(Color.WHITE, 26)
            setPadding(24,0,24,0)
        }
        root.addView(search, params(-1,60,0,18,0,14))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list, params(-1,-2,0,0,0,0))
        fun render(q: String) {
            list.removeAllViews()
            val x=q.trim().lowercase(Locale.getDefault())
            val found=spiritelli.filter { x.isEmpty() || it.name.lowercase(Locale.getDefault()).contains(x) }
            if(found.isEmpty()) list.addView(label("👻\n\nNessuno Spiritello trovato",17f,Color.GRAY,Gravity.CENTER),params(-1,150,0,0,0,0))
            found.forEach { s ->
                val card=LinearLayout(this).apply {
                    orientation=LinearLayout.HORIZONTAL
                    gravity=Gravity.CENTER_VERTICAL
                    setPadding(16,12,16,12)
                    background=rounded(Color.WHITE,22)
                    elevation=4f
                    setOnClickListener { details(spiritelli.indexOf(s)) }
                }
                val image=ImageView(this).apply {
                    scaleType=ImageView.ScaleType.CENTER_CROP
                    if(s.imageUri!=null) runCatching { setImageURI(Uri.parse(s.imageUri)) }
                    else setImageResource(android.R.drawable.ic_menu_gallery)
                }
                card.addView(image,params(68,68,0,0,14,0))
                val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
                box.addView(label(s.name,18f,Color.rgb(35,32,48),Gravity.START))
                box.addView(label("Tocca per vedere i dettagli",13f,Color.GRAY,Gravity.START))
                card.addView(box,LinearLayout.LayoutParams(0,-2,1f))
                card.addView(label("›",30f,Color.GRAY,Gravity.CENTER))
                list.addView(card,params(-1,94,0,0,0,12))
            }
        }
        search.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){}
            override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){render(s?.toString().orEmpty())}
            override fun afterTextChanged(s:android.text.Editable?){}
        })
        render("")
        root.addView(button("⚙️  Modalità Developer"){ developerLogin() },params(-1,58,0,18,0,0))
        setContentView(root)
    }

    private fun details(i:Int) {
        val s=spiritelli[i]; val root=root()
        root.addView(button("‹  Indietro"){home()})
        val image=ImageView(this).apply {
            scaleType=ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.rgb(235,232,248))
            if(s.imageUri!=null) runCatching { setImageURI(Uri.parse(s.imageUri)) }
            else setImageResource(android.R.drawable.ic_menu_gallery)
        }
        root.addView(image,params(-1,270,0,10,0,16))
        root.addView(label(s.name,29f,Color.rgb(35,32,48),Gravity.CENTER))
        root.addView(label("Spiritello di Fortnite",15f,Color.GRAY,Gravity.CENTER))
        setContentView(root)
    }

    private fun developerLogin() {
        val input=EditText(this).apply { hint="Codice Developer"; inputType=2; singleLine=true }
        val d=AlertDialog.Builder(this).setTitle("⚙️ Developer").setMessage("Inserisci il codice").setView(input)
            .setNegativeButton("Annulla",null).setPositiveButton("Accedi",null).create()
        d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if(input.text.toString()==developerCode){d.dismiss(); developer()} else Toast.makeText(this,"Codice errato",Toast.LENGTH_SHORT).show()
        }}
        d.show()
    }

    private fun developer() {
        val root=root()
        root.addView(label("⚙️ Developer",28f,Color.rgb(35,32,48),Gravity.CENTER))
        root.addView(label("Gestisci gli Spiritelli",15f,Color.GRAY,Gravity.CENTER))
        root.addView(button("＋  Aggiungi Spiritello"){add() },params(-1,58,0,22,0,8))
        root.addView(button("✏️  Gestisci Spiritelli"){manage()},params(-1,58,0,0,0,8))
        root.addView(button("←  Torna all'app"){home()},params(-1,58,0,0,0,8))
        setContentView(root)
    }

    private fun add() {
        val input=EditText(this).apply{hint="Nome dello Spiritello";singleLine=true}
        AlertDialog.Builder(this).setTitle("Nuovo Spiritello").setView(input).setNegativeButton("Annulla",null)
            .setPositiveButton("Aggiungi"){_,_-> val n=input.text.toString().trim(); if(n.isNotEmpty()){spiritelli.add(Spiritello(n));save();developer()} }.show()
    }

    private fun manage() {
        val root=root(); root.addView(button("‹  Indietro"){developer()})
        root.addView(label("Gestisci Spiritelli",26f,Color.rgb(35,32,48),Gravity.CENTER))
        spiritelli.forEachIndexed { i,s -> root.addView(button("👻  ${s.name}"){edit(i)},params(-1,58,0,12,0,0)) }
        setContentView(root)
    }

    private fun edit(i:Int) {
        val s=spiritelli[i]
        val input=EditText(this).apply{setText(s.name);selectAll();singleLine=true}
        AlertDialog.Builder(this).setTitle("Modifica Spiritello").setView(input)
            .setNeutralButton("📷 Foto"){_,_-> photoIndex=i; val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="image/*";addCategory(Intent.CATEGORY_OPENABLE);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)};startActivityForResult(intent,42)}
            .setNegativeButton("Elimina"){_,_->spiritelli.removeAt(i);save();manage()}
            .setPositiveButton("Salva"){_,_->val n=input.text.toString().trim();if(n.isNotEmpty())s.name=n;save();manage()}.show()
    }

    @Deprecated("Legacy API")
    override fun onActivityResult(r:Int,c:Int,d:Intent?) {
        super.onActivityResult(r,c,d)
        if(r==42 && c==RESULT_OK && d?.data!=null && photoIndex>=0){
            val u=d.data!!
            runCatching{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
            spiritelli[photoIndex].imageUri=u.toString(); save(); photoIndex=-1; manage()
        }
    }

    private fun save(){prefs.edit().putInt("count",spiritelli.size).apply().also{spiritelli.forEachIndexed{ i,s->prefs.edit().putString("name_$i",s.name).putString("img_$i",s.imageUri).apply()}}}
    private fun load(){spiritelli.clear();repeat(prefs.getInt("count",0)){i->spiritelli.add(Spiritello(prefs.getString("name_$i","Spiritello")!!,prefs.getString("img_$i",null)))}}

    private fun root()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(26,28,26,22);setBackgroundColor(Color.rgb(247,245,255))}
    private fun label(s:String,size:Float,color:Int,g:Int)=TextView(this).apply{text=s;textSize=size;setTextColor(color);gravity=g;setPadding(4,6,4,6)}
    private fun button(s:String,a:()->Unit)=Button(this).apply{text=s;textSize=16f;isAllCaps=false;background=rounded(Color.WHITE,18);elevation=3f;setOnClickListener{a()}}
    private fun rounded(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=r.toFloat();setStroke(1,Color.rgb(232,229,242))}
    private fun params(w:Int,h:Int,l:Int,t:Int,r:Int,b:Int)=LinearLayout.LayoutParams(w,h).apply{setMargins(l,t,r,b)}
}
