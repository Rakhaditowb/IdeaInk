package com.apps.ideaink.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.apps.ideaink.R
import com.apps.ideaink.databinding.FragmentProfileBinding
import com.example.PPAB10.UserModel
import com.example.PPAB10.UserPreference
import java.io.File

class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var mUserPreference: UserPreference
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var isPreferenceEmpty = false
    private lateinit var userModel: UserModel

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.data != null && result.resultCode == FormUserPreferenceActivity.RESULT_CODE) {
            userModel = result.data?.getParcelableExtra<UserModel>(FormUserPreferenceActivity.EXTRA_RESULT) as UserModel
            populateView(userModel)
            checkForm(userModel)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUserPreference = UserPreference(requireContext())
        userModel = mUserPreference.getUser()

        showExistingPreference()

        binding.btnSave.setOnClickListener(this)
    }

    private fun showExistingPreference() {
        userModel = mUserPreference.getUser()
        populateView(userModel)
        checkForm(userModel)
    }

    private fun populateView(userModel: UserModel) {
        binding.tvName.text =
            if (userModel.name.toString().isEmpty()) "Not Filled" else userModel.name
        binding.tvEmail.text =
            if (userModel.email.toString().isEmpty()) "Not Filled" else userModel.email
        binding.tvQuote.text =
            if (userModel.quote.toString().isEmpty()) "Not Filled" else userModel.quote
        if (userModel.img != null) {
            val imgFile = File(userModel.img)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                binding.tvImage.setImageBitmap(bitmap)
            }
        }
    }

    private fun checkForm(userModel: UserModel) {
        val nameNotEmpty = userModel.name?.isNotEmpty() ?: false
        val emailNotEmpty = userModel.email?.isNotEmpty() ?: false
        val quoteNotEmpty = userModel.quote?.isNotEmpty() ?: false

        isPreferenceEmpty = !(nameNotEmpty || emailNotEmpty || quoteNotEmpty)

        binding.btnSave.text = if (isPreferenceEmpty) getString(R.string.isi) else getString(R.string.change)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_save) {
            val intent = Intent(requireContext(), FormUserPreferenceActivity::class.java)
            userModel = userModel ?: UserModel()
            intent.putExtra(
                FormUserPreferenceActivity.EXTRA_TYPE_FORM,
                if (isPreferenceEmpty) FormUserPreferenceActivity.TYPE_ADD else FormUserPreferenceActivity.TYPE_EDIT
            )
            intent.putExtra("USER", userModel)
            resultLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
