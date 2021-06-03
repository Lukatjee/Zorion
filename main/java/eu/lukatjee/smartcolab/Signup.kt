package eu.lukatjee.smartcolab

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import io.paperdb.Paper
import kotlin.math.sign

class Signup : AppCompatActivity(), View.OnClickListener {

    private lateinit var fAuth : FirebaseAuth

    private lateinit var usernameEt : EditText
    private lateinit var emailEt : EditText
    private lateinit var passwordEt : EditText
    private lateinit var signUpBtn : Button

    private lateinit var messageInvalidUsername : String
    private lateinit var messageInvalidEmail : String
    private lateinit var messageInvalidPassword : String
    private lateinit var messageFailedSignup : String
    private lateinit var messageFailedLogin : String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fAuth = FirebaseAuth.getInstance()

        usernameEt = findViewById(R.id.editTextTextUsername)
        emailEt = findViewById(R.id.editTextEmail)
        passwordEt = findViewById(R.id.editTextPassword)
        signUpBtn = findViewById(R.id.button)

        messageInvalidUsername = "Invalid username"
        messageInvalidEmail = "Invalid email"
        messageInvalidPassword = "Invalid password"
        messageFailedSignup = "Couldn't create a new account, please try again later."
        messageFailedLogin = "Failed to log in, please try logging in manually."

        signUpBtn.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.button -> {

                val usernameIpt = usernameEt.text.toString().trim()
                val emailIpt = emailEt.text.toString().trim()
                val passwordIpt = passwordEt.text.toString().trim()

                if (usernameIpt.isEmpty()) {

                    usernameEt.error = messageInvalidUsername
                    usernameEt.requestFocus()

                    return

                }

                if (emailIpt.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailIpt).matches()) {

                    emailEt.error = messageInvalidEmail
                    emailEt.requestFocus()

                    return

                }

                if (passwordIpt.isEmpty()) {

                    passwordEt.error = messageInvalidPassword
                    passwordEt.requestFocus()

                    return

                }

                fAuth.createUserWithEmailAndPassword(emailIpt, passwordIpt).addOnCompleteListener { task ->

                    if (task.isComplete) {

                        fAuth.signInWithEmailAndPassword(emailIpt, passwordIpt).addOnCompleteListener(this) { taskTwo ->

                            if (taskTwo.isSuccessful) {

                                val currentUser = fAuth.currentUser
                                val profileChangeRqst = UserProfileChangeRequest.Builder().setDisplayName(usernameIpt).build()

                                currentUser!!.updateProfile(profileChangeRqst).addOnCompleteListener { taskThree ->

                                    if (taskThree.isSuccessful) {

                                        intent = Intent(this, Profile::class.java)
                                        intent.putExtra("FROM_ACTIVITY", "LOGIN")
                                        startActivity(intent)

                                        val userData = hashMapOf(emailIpt to passwordIpt)
                                        Paper.book().write("userData", userData)

                                    }

                                }

                            } else {

                                Toast.makeText(this, messageFailedLogin, Toast.LENGTH_LONG).show()

                            }

                        }

                    } else {

                        Toast.makeText(this, messageFailedSignup, Toast.LENGTH_LONG).show()

                    }

                }

            }

        }

    }

}