package mbtinder.android.ui.fragment.sign_up

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.text.Editable
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import mbtinder.android.R
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*
import java.io.FileInputStream

class SignUp4Fragment : Fragment(R.layout.fragment_sign_up4) {
    private val requestCodeReadExternalStorage = 0x00
    private val resultCodeAlbum = 0x00

    private val formStateChecker = FormStateChecker()

    private var selectedProfileImage: ByteArray? = null

    override fun initializeView() {
        formStateChecker.addViews(sign_up4_profile, sign_up4_description)

        initializeFocusableEditText(sign_up4_description, this::onDescriptionChanged)

        sign_up4_profile.clipToOutline = true
        sign_up4_profile.setOnClickListener { onProfileClicked() }
        sign_up4_profile_select.setOnClickListener { onProfileClicked() }
        switchable_next.setOnClickListener { onNextClicked() }
    }

    private fun onProfileClicked() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCodeReadExternalStorage)
    }

    private fun onNextClicked() {
        ViewUtil.switchNextButton(layout_sign_up4)
        runOnBackground {
            val getResult = CommandProcess.getSignUpQuestion()
            if (getResult.isSucceed) {
                val arguments = requireArguments()
                arguments.putByteArray("profile", selectedProfileImage)
                arguments.putString("description", sign_up4_description.getText())
                arguments.putJSONArray("sign_up_questions", getResult.result!!.toJSONArray())
                runOnUiThread { findNavController().navigate(R.id.action_to_sign_up5, arguments) }
            } else {
                runOnUiThread {
                    Toast.makeText(requireContext(), R.string.sign_up4_sign_up_question_failed, Toast.LENGTH_SHORT).show()
                    ViewUtil.switchNextButton(layout_sign_up4)
                }
            }
        }
    }

    private fun enableNextButton() {
        switchable_next.isEnabled = !formStateChecker.hasFalse()
    }

    private fun onDescriptionChanged(editable: Editable?) {
        editable?.let {
            formStateChecker.setState(sign_up4_description, it.isNotBlank())
            enableNextButton()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            requestCodeReadExternalStorage -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ViewUtil.callAlbum(this, resultCodeAlbum)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            resultCodeAlbum -> {
                if (data == null || data.data == null) {
                    return
                }

                val uri = data.data!!
                val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")!!
                FileInputStream(parcelFileDescriptor.fileDescriptor).apply {
                    selectedProfileImage = ImageUtil.resizeByteArray(readBytes())
                    close()
                }

                sign_up4_profile.setImageBitmap(ImageUtil.byteArrayToBitmap(selectedProfileImage!!))
                formStateChecker.setState(sign_up4_profile, true)
                enableNextButton()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}