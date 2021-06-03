package eu.lukatjee.smartcolab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.paperdb.Paper


class Landing : AppCompatActivity(), View.OnClickListener {

    private lateinit var fAuth : FirebaseAuth

    private lateinit var signInButton : Button
    private lateinit var signUpButton : Button
    private lateinit var progressBarOne : ProgressBar

    private lateinit var messageInvalidSaved : String

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_landing)
        intent.putExtra("FROM_ACTIVITY", "NONE")

        Paper.init(this)
        fAuth = FirebaseAuth.getInstance()

        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        progressBarOne = findViewById(R.id.progressBarOne)

        signInButton.setOnClickListener(this)
        signUpButton.setOnClickListener(this)

        messageInvalidSaved = "Saved credentials did not match an existing account"


    }

    public override fun onStart() {

        super.onStart()

        val userData = Paper.book().read("userData", hashMapOf<String, String>())

        if (userData.isNotEmpty()) {

            progressBarOne.visibility = View.VISIBLE
            signInButton.isEnabled = false
            signUpButton.isEnabled = false

            val userDataKey = userData.keys.first()
            val userDataPassword = userData[userDataKey] ?: return

            fAuth.signInWithEmailAndPassword(userDataKey, userDataPassword).addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        intent = Intent(this, Profile::class.java)
                        intent.putExtra("FROM_ACTIVITY", "NONE")

                        progressBarOne.visibility = View.GONE
                        signInButton.isEnabled = true
                        signUpButton.isEnabled = true

                        startActivity(intent)

                    } else {

                        Toast.makeText(this, messageInvalidSaved, Toast.LENGTH_LONG).show()

                    }

                }

        }

    }

    override fun onBackPressed() {

        super.onBackPressed()

        when (intent.extras!!.getString("FROM_ACTIVITY")) {

            "NONE" -> this.finishAffinity()

        }

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.signInButton -> {

                intent = Intent(this, Login::class.java)
                intent.putExtra("FROM_ACTIVITY", "LANDING")
                startActivity(intent)

            }

            R.id.signUpButton -> {

                intent = Intent(this, Signup::class.java)
                intent.putExtra("FROM_ACTIVITY", "LANDING")
                startActivity(intent)

            }

        }

    }

}
