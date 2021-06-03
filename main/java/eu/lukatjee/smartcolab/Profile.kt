package eu.lukatjee.smartcolab

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.paperdb.Paper


class Profile : AppCompatActivity(), View.OnClickListener {

    private var isPressed = false
    private var isEditing = false

    private lateinit var fAuth : FirebaseAuth
    private lateinit var currentUser : FirebaseUser

    private lateinit var storageRef : StorageReference
    private lateinit var fileRef : StorageReference

    private lateinit var displaynameEt : TextView
    private lateinit var emailEt : TextView
    private lateinit var editProfileBtn : Button
    private lateinit var profilePictureVw : CircleImageView
    private lateinit var logoutButton : Button
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fAuth = FirebaseAuth.getInstance()
        currentUser = fAuth.currentUser!!

        storageRef = FirebaseStorage.getInstance().reference
        fileRef = storageRef.child("profile_${currentUser.uid}.png")
        displaynameEt = findViewById(R.id.displaynameEt)
        emailEt = findViewById(R.id.emailEtProfile)
        editProfileBtn = findViewById(R.id.editProfileBtn)
        profilePictureVw = findViewById(R.id.profilePictureVw)
        logoutButton = findViewById(R.id.logoutBtn)
        progressBar = findViewById(R.id.progressBar)

        editProfileBtn.setOnClickListener(this)
        profilePictureVw.setOnClickListener(this)
        logoutButton.setOnClickListener(this)

        getData() // Sync all data for the profile page

    }

    private fun doubleTapBack() {

        if (isPressed) {

            this.finishAffinity()

        } else {

            Toast.makeText(this, "Press the return key again to exit the app", Toast.LENGTH_LONG).show()
            isPressed = true

        }

    }

    override fun onBackPressed() {

        when (intent.extras!!.getString("FROM_ACTIVITY")) {

            "NONE" -> doubleTapBack()
            "LOGIN" -> doubleTapBack()

        }

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.editProfileBtn -> {

                if (!isEditing) {

                    isEditing = true

                    editProfileBtn.text = getString(R.string.save)
                    editProfileBtn.invalidate()

                    displaynameEt.isEnabled = true
                    emailEt.isEnabled = true

                    return

                }

                val displaynameIpt = displaynameEt.text.toString().trim()
                val emailIpt = emailEt.text.toString().trim()

                if (displaynameIpt.isEmpty()) {

                    emailEt.error = "Invalid username"
                    emailEt.requestFocus()

                    return

                }

                val profileChangeRqst = UserProfileChangeRequest.Builder().setDisplayName(displaynameIpt).build()

                if (!(Patterns.EMAIL_ADDRESS.matcher(emailIpt).matches())) {

                    emailEt.error = "Invalid e-mail address"
                    emailEt.requestFocus()

                    return

                }

                progressBar.visibility = View.VISIBLE

                currentUser.updateProfile(profileChangeRqst).addOnCompleteListener(this){ task ->

                    if (task.isSuccessful) {

                        displaynameSv()
                        progressBar.visibility = View.GONE

                    }

                }

                progressBar.visibility = View.VISIBLE

                currentUser.updateEmail(emailIpt).addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        emailSv()
                        progressBar.visibility = View.GONE

                    }

                }

                fullProfileSv()

            }

            R.id.logoutBtn -> {

                fAuth.signOut()

                intent = Intent(this, Landing::class.java)
                intent.putExtra("FROM_ACTIVITY", "NONE")

                startActivity(intent)
                Paper.book().destroy()

            }

            R.id.profilePictureVw -> {

                if (isEditing) {

                    val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(openGalleryIntent, 1000)

                } /* else {

                    intent = Intent(this, Chats::class.java)
                    intent.putExtra("FROM_ACTIVITY", "PROFILE")

                    startActivity(intent)

                } */

            }

        }

    }

    private lateinit var imageUri : Uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode != 1000) || (resultCode != Activity.RESULT_OK)) {

            return

        }

        imageUri = data!!.data!!
        uploadImageToFirebase(imageUri)

    }

    private fun uploadImageToFirebase(imageUrl : Uri?) {

        progressBar.visibility = View.VISIBLE

        fileRef.putFile(imageUrl!!).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                fileRef.downloadUrl.addOnSuccessListener { uri : Uri? ->

                    progressBar.visibility = View.GONE
                    Picasso.get().load(uri).into(profilePictureVw)

                }

                Toast.makeText(this, "Successfully uploaded the image", Toast.LENGTH_LONG).show()

            } else {

                progressBar.visibility = View.GONE
                println("Oh no")

            }

        }

    }

    private fun fullProfileSv() {

        editProfileBtn.text = getString(R.string.edit)
        editProfileBtn.invalidate()

        isEditing = false

    }

    private fun displaynameSv() {

        displaynameEt.isEnabled = false

    }

    private fun emailSv() {

        emailEt.isEnabled = false

    }

    private fun getData() {

        displaynameEt.text = FirebaseAuth.getInstance().currentUser!!.displayName
        emailEt.text = FirebaseAuth.getInstance().currentUser!!.email

        displaynameEt.invalidate()
        emailEt.invalidate()

        progressBar.visibility = View.VISIBLE

        fileRef.downloadUrl.addOnSuccessListener { uri : Uri? ->

            Picasso.get().load(uri).into(profilePictureVw)
            progressBar.visibility = View.GONE

        }

    }

}
