package mbtinder.android.ui.fragment.account

import androidx.annotation.IdRes
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.fragment_account.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ViewUtil
import mbtinder.android.util.runOnBackground
import mbtinder.lib.component.user.SearchFilter

class AccountFragment : Fragment(R.layout.fragment_account) {
    override fun initializeView() {
        account_profile.setImage(StaticComponent.getUserImage(StaticComponent.user.userId))
        account_description.editText!!.setText(StaticComponent.user.description)
        account_gender_selector.check(genderToId(StaticComponent.user.searchFilter.gender))
        account_age_selector.valueFrom = StaticComponent.user.searchFilter.ageStart.toFloat()
        account_age_selector.valueTo = StaticComponent.user.searchFilter.ageEnd.toFloat()
        account_distance_selector.value = StaticComponent.user.searchFilter.distance.toFloat()
        account_notification_selector.isChecked = StaticComponent.user.notification

        account_description_edit.setOnClickListener {
            val description = ViewUtil.getText(account_description)

            if (description.isBlank()) {
                account_description.error = getString(R.string.account_description_error)
                account_description.isErrorEnabled = true
            } else {
                account_description.isErrorEnabled = false
                runOnBackground { CommandProcess.updateUserDescription(StaticComponent.user.userId, description) }
            }
        }

        account_gender_selector.addOnButtonCheckedListener { group, checkedId, isChecked ->
            StaticComponent.user.gender = idToGender(checkedId)
            runOnBackground { CommandProcess.updateSearchFilter(buildSearchFilter()) }
        }

        account_age_selector.addOnChangeListener { _: RangeSlider, _: Float, _: Boolean ->
            runOnBackground { CommandProcess.updateSearchFilter(buildSearchFilter()) }
        }

        account_distance_selector.addOnChangeListener { _, _, _ ->
            runOnBackground { CommandProcess.updateSearchFilter(buildSearchFilter()) }
        }

        account_notification_selector.setOnCheckedChangeListener { _, isChecked ->
            runOnBackground { CommandProcess.updateUserNotification(StaticComponent.user.userId, isChecked) }
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

    private fun buildSearchFilter() = SearchFilter(
        StaticComponent.user.userId,
        StaticComponent.user.gender,
        account_age_selector.valueFrom.toInt(),
        account_age_selector.valueTo.toInt(),
        account_distance_selector.value.toInt()
    )
}