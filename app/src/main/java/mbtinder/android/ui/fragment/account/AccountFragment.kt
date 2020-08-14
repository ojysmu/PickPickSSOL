package mbtinder.android.ui.fragment.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.IdRes
import kotlinx.android.synthetic.main.fragment_account.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*
import mbtinder.lib.component.user.SearchFilter
import java.io.FileInputStream

class AccountFragment : Fragment(R.layout.fragment_account) {
    private val requestCodeReadExternalStorage = 0x00
    private val resultCodeAlbum = 0x00

    override fun initializeView() {
        account_profile.setImage(StaticComponent.getUserImage(StaticComponent.user.userId))
        account_description.editText!!.setText(StaticComponent.user.description)
        account_gender_selector.check(genderToId(StaticComponent.user.searchFilter.gender))
        account_age_selector.valueFrom = StaticComponent.user.searchFilter.ageStart.toFloat()
        account_age_selector.valueTo = StaticComponent.user.searchFilter.ageEnd.toFloat()
        account_age_indicator.text = getString(R.string.account_age_indicator, account_age_selector.getStart(), account_age_selector.getEnd())
        account_distance_selector.value = StaticComponent.user.searchFilter.distance.toFloat()
        account_distance_indicator.text = getString(R.string.account_distance_indicator, account_distance_selector.value.toInt())
        account_notification_selector.isChecked = StaticComponent.user.notification

        account_profile_edit.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCodeReadExternalStorage)
        }

        account_description_edit.setOnClickListener {
            val description = account_description.getText()

            if (description.isBlank()) {
                account_description.error = getString(R.string.account_description_error)
                account_description.isErrorEnabled = true
            } else {
                hideIme()
                account_description.isErrorEnabled = false
                account_description.clearFocus()
                runOnBackground { CommandProcess.updateUserDescription(StaticComponent.user.userId, description) }
            }
        }

        account_gender_selector.addOnButtonCheckedListener { _, checkedId, _ ->
            StaticComponent.user.gender = idToGender(checkedId)
            updateSearchFilter()
        }

        account_age_selector.addOnChangeListener { _, _, _ -> updateSearchFilter() }

        account_distance_selector.addOnChangeListener { _, _, _ -> updateSearchFilter() }

        account_notification_selector.setOnCheckedChangeListener { _, isChecked ->
            runOnBackground { CommandProcess.updateUserNotification(StaticComponent.user.userId, isChecked) }
        }

        account_delete_user.setOnClickListener {
            DialogFactory.getContentedDialog(requireContext(), R.string.account_delete_user_alert, onPositive = {
                val waitDialog = DialogFactory.getWaitDialog(requireContext()).apply { show() }
                val deleteResult = runOnBackground<Boolean> {
                    CommandProcess.deleteUser(StaticComponent.user.userId).isSucceed
                }

                if (deleteResult) {
                    SharedPreferencesUtil
                        .getContext(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT)
                        .removePreference()
                    waitDialog.dismiss()
                    Toast.makeText(requireActivity(), R.string.account_delete_succeed, Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                } else {
                    waitDialog.dismiss()
                    Toast.makeText(requireContext(), R.string.account_delete_failed, Toast.LENGTH_SHORT).show()
                }
            }).show()
        }

        account_sign_out.setOnClickListener {
            DialogFactory.getContentedDialog(requireContext(), R.string.account_sign_out_alert, onPositive = {
                val waitDialog = DialogFactory.getWaitDialog(requireContext()).apply { show() }
                SharedPreferencesUtil
                    .getContext(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT)
                    .removePreference()
                waitDialog.dismiss()
                Toast.makeText(requireActivity(), R.string.account_sign_out_succeed, Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }).show()
        }
    }

    @IdRes
    private fun genderToId(gender: Int) = when (gender) {
        0 -> R.id.account_gender_male
        1 -> R.id.account_gender_female
        else -> R.id.account_gender_all
    }

    private fun idToGender(@IdRes id: Int): Int {
        return when (id) {
            R.id.account_gender_male -> 0
            R.id.account_gender_female -> 1
            R.id.account_gender_all -> 2
            else -> throw AssertionError("gender should be in [0,1]")
        }
    }

    private fun updateSearchFilter() {
        runOnBackground { CommandProcess.updateSearchFilter(buildSearchFilter()) }
        account_age_indicator.text = getString(R.string.account_age_indicator, account_age_selector.getStart(), account_age_selector.getEnd())
        account_distance_indicator.text = getString(R.string.account_distance_indicator, account_distance_selector.value.toInt())
    }

    private fun buildSearchFilter() = SearchFilter(
        StaticComponent.user.userId,
        StaticComponent.user.gender,
        account_age_selector.getStart(),
        account_age_selector.getEnd(),
        account_distance_selector.value.toInt()
    )

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
                val selectedProfileImage: ByteArray
                val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")!!
                FileInputStream(parcelFileDescriptor.fileDescriptor).apply {
                    selectedProfileImage = ImageUtil.resizeByteArray(readBytes())
                    close()
                }

                account_profile.setImageBitmap(ImageUtil.byteArrayToBitmap(selectedProfileImage))
                runOnBackground {
                    val result = CommandProcess.uploadProfileImage(StaticComponent.user.userId, selectedProfileImage)
                    Log.v("AccountFragment.onActivityResult(): result=$result")
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}