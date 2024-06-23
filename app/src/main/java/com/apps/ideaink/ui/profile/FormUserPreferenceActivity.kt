package com.apps.ideaink.ui.profile


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.apps.ideaink.R
import com.apps.ideaink.databinding.ActivityFormUserPreferenceBinding
import com.example.PPAB10.UserModel
import com.example.PPAB10.UserPreference
import com.soundcloud.android.crop.Crop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FormUserPreferenceActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFormUserPreferenceBinding
    private lateinit var userModel: UserModel

    companion object {
        const val EXTRA_TYPE_FORM = "extra_type_form"
        const val EXTRA_RESULT = "extra_result"
        const val RESULT_CODE = 101
        const val TYPE_ADD = 1
        const val TYPE_EDIT = 2
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2

        private const val FIELD_REQUIRED = "Wajib diisi!"
        private const val FIELD_IS_NOT_VALID = "Email tidak valid!"
        private const val TARGET_WIDTH = 200
        private const val TARGET_HEIGHT = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormUserPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener(this)
        binding.tvImage.setOnClickListener { showImageDialog() }

        userModel = intent.getParcelableExtra<UserModel>("USER") ?: UserModel()
        val formType = intent.getIntExtra(EXTRA_TYPE_FORM, 0)
        val actionBarTitle: String
        val btnTitle: String

        when (formType) {
            TYPE_ADD -> {
                actionBarTitle = "Tambah Baru"
                btnTitle = "Save"
            }
            TYPE_EDIT -> {
                actionBarTitle = "Ubah"
                btnTitle = "Update"
                showPreferenceInForm()
            }
            else -> {
                actionBarTitle = ""
                btnTitle = ""
            }
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnSave.text = btnTitle
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_save) {
            if (validateInput()) {
                saveUser(
                    binding.edtName.text.toString().trim(),
                    binding.edtEmail.text.toString().trim(),
                    binding.edtQuote.text.toString().trim()
                )

                val resultIntent = Intent().apply {
                    putExtra(EXTRA_RESULT, userModel)
                }
                setResult(RESULT_CODE, resultIntent)
                finish()
            }
        }
    }

    private fun showImageDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> choosePhoto()
                }
            }
            .show()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun choosePhoto() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    private fun showPreferenceInForm() {
        with(binding) {
            edtName.setText(userModel.name)
            edtEmail.setText(userModel.email)
            edtQuote.setText(userModel.quote)
            userModel.img?.let {
                val imgFile = File(it)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    tvImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        with(binding) {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val bio = edtQuote.text.toString().trim()

            when {
                name.isEmpty() -> {
                    edtName.error = FIELD_REQUIRED
                    return false
                }
                email.isEmpty() -> {
                    edtEmail.error = FIELD_REQUIRED
                    return false
                }
                !isValidEmail(email) -> {
                    edtEmail.error = FIELD_IS_NOT_VALID
                    return false
                }
                bio.isEmpty() -> {
                    edtQuote.error = FIELD_REQUIRED
                    return false
                }
                else -> return true
            }
        }
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveUser(name: String, email: String, bio: String) {
        val userPreference = UserPreference(this)
        userModel.apply {
            this.name = name
            this.email = email
            this.quote = bio
        }
        userPreference.setUser(userModel)
        Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val imageUri = getImageUriFromBitmap(imageBitmap)
                    startCrop(imageUri)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let { startCrop(it) }
                }
                Crop.REQUEST_CROP -> {
                    handleCrop(resultCode, data)
                }
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped"))
        Crop.of(uri, destinationUri).asSquare().start(this, Crop.REQUEST_CROP)
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == RESULT_OK) {
            val resultUri = Crop.getOutput(result)
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
            val resizedBitmap = resizeBitmap(bitmap, TARGET_WIDTH, TARGET_HEIGHT)
            binding.tvImage.setImageBitmap(resizedBitmap)
            saveImageToInternalStorage(resizedBitmap)
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "temp_image.jpg")
        try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val file = File(filesDir, "profile_photo.jpg")
        try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            userModel.img = file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
