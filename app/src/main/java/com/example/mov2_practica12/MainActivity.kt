package com.example.mov2_practica12

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contra: EditText
    private lateinit var ingresar: Button
    private lateinit var limpiar: Button
    private lateinit var iniciar: Button
    private lateinit var switchNuevaCuenta: Switch

    private var correo: String = ""
    private var passwd: String = ""

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de vistas
        usuario = findViewById(R.id.editTextText)
        contra = findViewById(R.id.editTextTextPassword)
        ingresar = findViewById(R.id.buttonIngresar)
        limpiar = findViewById(R.id.buttonLimpiar)
        iniciar = findViewById(R.id.buttonGoogle)
        switchNuevaCuenta = findViewById(R.id.switchNuevaCuenta)

        ingresar.isEnabled = false

        try {
            // Inicialización de FirebaseAuth y Google Sign-In
            mAuth = FirebaseAuth.getInstance()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.e("Error", "Excepción en onCreate: ${e.message}")
        }

        // Configuración de listeners
        ingresar.setOnClickListener { validarFormulario() }
        limpiar.setOnClickListener { borrarValores() }
        iniciar.setOnClickListener { signInWithGoogle() }
        usuario.doOnTextChanged { _, _, _, _ -> existeDominio() }
    }

    private fun validarFormulario() {
        correo = usuario.text.toString()
        passwd = contra.text.toString()

        if (switchNuevaCuenta.isChecked) {
            registrarNuevaCuenta()
        } else {
            validarCorreo()
        }
    }

    private fun validarCorreo() {
        if (correo.isEmpty() || passwd.isEmpty()) {
            Toast.makeText(this, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(correo, passwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    borrarValores() // Limpiar los campos solo al éxito
                    lanzarFormulario()
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Datos incorrectos"
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("LoginError", "Error al iniciar sesión: ${task.exception}")
                }
            }
    }

    private fun registrarNuevaCuenta() {
        if (correo.isEmpty() || passwd.isEmpty()) {
            Toast.makeText(this, "Por favor complete ambos campos", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(correo, passwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Cuenta registrada exitosamente", Toast.LENGTH_SHORT).show()
                    borrarValores()
                    lanzarFormulario()
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Error al registrar la cuenta"
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("RegisterError", "Error al registrar la cuenta: ${task.exception}")
                }
            }
    }

    private fun lanzarFormulario() {
        val formulario = Intent(this, FormularioActivity::class.java)
        startActivity(formulario)
    }

    private fun borrarValores() {
        usuario.text.clear()
        contra.text.clear()
        usuario.requestFocus()
    }

    private fun existeDominio() {
        correo = usuario.text.toString()
        val existe = correo.indexOf("@")
        val dominioValido = correo.substringAfter("@").isNotEmpty()

        ingresar.isEnabled = existe > 0 && dominioValido
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("GoogleSignIn", "Cuenta seleccionada: ${account.email}")
                lanzarFormulario()
            } catch (e: ApiException) {
                Log.e("GoogleSignInError", "Error al seleccionar la cuenta de Google: ${e.statusCode} - ${e.message}")
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
