package com.example.mov2_practica12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class ListadoActivity : AppCompatActivity() {

    private lateinit var linearLayout: LinearLayout
    private val db = FirebaseFirestore.getInstance()
    private val coleccion = "Contactos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        linearLayout = findViewById(R.id.linearLayoutContactos)

        obtenerContactos()
    }

    private fun obtenerContactos() {
        db.collection(coleccion)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val contacto = document.toObject(Contacto::class.java)
                    agregarContactoVista(contacto)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener los datos.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarContactoVista(contacto: Contacto) {
        val textView = TextView(this)
        textView.text = "Nombre: ${contacto.nombre}\nApellidos: ${contacto.apellidos}\nCorreo: ${contacto.correo}\n"
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.textSize = 16f

        linearLayout.addView(textView)
    }
}