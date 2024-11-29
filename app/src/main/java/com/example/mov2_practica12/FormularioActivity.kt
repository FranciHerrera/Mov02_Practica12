package com.example.mov2_practica12

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FormularioActivity : AppCompatActivity() {

    private lateinit var mum: EditText
    private lateinit var nom: EditText
    private lateinit var ape: EditText
    private lateinit var cor: EditText
    private lateinit var agregar: ImageButton
    private lateinit var buscar: ImageButton
    private lateinit var actualizar: ImageButton
    private lateinit var borrar: ImageButton
    private lateinit var cerrar: Button
    private lateinit var buttonListar: Button

    private var coleccion = "Contactos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)
        mum = findViewById(R.id.editTextNumero)
        nom = findViewById(R.id.editTextNombre)
        ape = findViewById(R.id.editTextApellido)
        cor = findViewById(R.id.editTextCorreo)
        agregar = findViewById(R.id.imageButtonAgregar)
        buscar = findViewById(R.id.imageButtonBuscar)
        actualizar = findViewById(R.id.imageButtonActualizar)
        borrar = findViewById(R.id.imageButtonEliminar)
        cerrar = findViewById(R.id.buttonSalir)
        buttonListar = findViewById(R.id.buttonListar)

        agregar.setOnClickListener { movimientoContacto("Contacto registrado.") }
        buscar.setOnClickListener { buscarContacto() }
        actualizar.setOnClickListener { actualizarContacto() }
        borrar.setOnClickListener { borrarContacto() }
        cerrar.setOnClickListener { logout() }
        buttonListar.setOnClickListener {
            startActivity(Intent(this, ListadoActivity::class.java))
        }
    }

    private fun movimientoContacto(mensaje: String) {
        val bdAgenda = FirebaseFirestore.getInstance()

        if (validarCampos()) {
            val numero = mum.text.toString().toInt()
            val nombre = nom.text.toString()
            val apellidos = ape.text.toString()
            val correo = cor.text.toString()
            val idDocumento = "$numero-$correo"

            bdAgenda.collection(coleccion).document(idDocumento)
                .set(hashMapOf(
                    "numero" to numero,
                    "nombre" to nombre,
                    "apellidos" to apellidos,
                    "correo" to correo
                ))
                .addOnSuccessListener {
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscarContacto() {
        val bdAgenda = FirebaseFirestore.getInstance()

        if (cor.text.isNotBlank() && cor.text.isNotEmpty()) {
            val correo = cor.text.toString()

            bdAgenda.collection(coleccion).whereEqualTo("correo", correo)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val documento = documentos.documents[0]
                        val contacto = documento.toObject(Contacto::class.java)
                        mum.setText(contacto?.numero.toString())
                        nom.setText(contacto?.nombre)
                        ape.setText(contacto?.apellidos)
                    } else {
                        Toast.makeText(this, "Contacto no encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarContacto() {
        val bdAgenda = FirebaseFirestore.getInstance()

        if (validarCampos()) {
            val numero = mum.text.toString().toInt()
            val nombre = nom.text.toString()
            val apellidos = ape.text.toString()
            val correo = cor.text.toString()
            val idDocumento = "$numero-$correo"

            bdAgenda.collection(coleccion).document(idDocumento)
                .update(
                    "numero", numero,
                    "nombre", nombre,
                    "apellidos", apellidos,
                    "correo", correo
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Contacto actualizado.", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun borrarContacto() {
        val bdAgenda = FirebaseFirestore.getInstance()

        if (cor.text.isNotBlank() && cor.text.isNotEmpty()) {
            val correo = cor.text.toString()

            bdAgenda.collection(coleccion).whereEqualTo("correo", correo)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val documento = documentos.documents[0]
                        documento.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Contacto eliminado.", Toast.LENGTH_SHORT).show()
                                limpiarFormulario()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Contacto no encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarCampos(): Boolean {
        return mum.text.isNotBlank() && mum.text.isNotEmpty() &&
                nom.text.isNotBlank() && nom.text.isNotEmpty() &&
                ape.text.isNotBlank() && ape.text.isNotEmpty() &&
                cor.text.isNotBlank() && cor.text.isNotEmpty()
    }

    private fun limpiarFormulario() {
        mum.setText("")
        nom.setText("")
        ape.setText("")
        cor.setText("")
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }
}