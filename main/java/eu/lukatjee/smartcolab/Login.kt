package eu.lukatjee.smartcolab

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import io.paperdb.Paper
import java.util.*

class Login : AppCompatActivity(), View.OnClickListener {

    private lateinit var fAuth : FirebaseAuth

    private lateinit var signInBtn : Button
    private lateinit var emailEt : EditText
    private lateinit var passwordEt : EditText
    private lateinit var progressBarTwo : ProgressBar
    private lateinit var forgotPasswordBtn : TextView

    private lateinit var messageFailedLogin : String
    private lateinit var messageIncorrectEmail : String
    private lateinit var messageIncorrectPassword : String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        Paper.init(this)
        fAuth = FirebaseAuth.getInstance()

        signInBtn = findViewById(R.id.signInBtn)
        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        progressBarTwo = findViewById(R.id.progressBarTwo)
        forgotPasswordBtn = findViewById(R.id.resetPasswordBtn)

        messageFailedLogin = "Failed to log in, please check the entered credentials"
        messageIncorrectEmail = "Invalid e-mail address"
        messageIncorrectPassword = "Password is required"

        signInBtn.setOnClickListener(this)
        forgotPasswordBtn.setOnClickListener(this)

    }

    override fun onClick(v: View?) { when (v?.id) {

        R.id.signInBtn -> {

            progressBarTwo.visibility = View.VISIBLE
            userLogin()

        } } }

    private fun userLogin() {

        val emailIpt = emailEt.text.toString().trim().toLowerCase(Locale.ROOT)
        val passwordIpt = passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(emailIpt).matches()) {

            emailEt.error = messageIncorrectEmail
            emailEt.requestFocus()

            progressBarTwo.visibility = View.GONE

            return

        }

        if (passwordIpt.isEmpty()) {

            passwordEt.error = messageIncorrectPassword
            passwordEt.requestFocus()

            progressBarTwo.visibility = View.GONE

            return

        }

        fAuth.signInWithEmailAndPassword(emailIpt, passwordIpt).addOnCompleteListener(this) { task ->

            if (task.isSuccessful) {

                intent = Intent(this, Profile::class.java)
                intent.putExtra("FROM_ACTIVITY", "LOGIN")
                startActivity(intent)

                val userData = hashMapOf(emailIpt to passwordIpt)
                Paper.book().write("userData", userData)

                progressBarTwo.visibility = View.GONE

            } else {

                Toast.makeText(this, messageFailedLogin, Toast.LENGTH_LONG).show()

            }

        }

    }

}