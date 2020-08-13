package mbtinder.android.ui.fragment.account

import kotlinx.android.synthetic.main.fragment_account.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.ui.model.Fragment

class AccountFragment : Fragment(R.layout.fragment_account) {
    override fun initializeView() {
        account_profile.setImage(StaticComponent.getUserImage(StaticComponent.user.userId))
        account_description.editText!!.setText(StaticComponent.user.description)
        account_gender_selector.check(StaticComponent.user.searchFilter.gender)
        account_age_selector.valueFrom = StaticComponent.user.searchFilter.ageStart.toFloat()
        account_age_selector.valueTo = StaticComponent.user.searchFilter.ageEnd.toFloat()
        account_distance_selector.value = StaticComponent.user.searchFilter.distance.toFloat()
        account_notification_selector.isChecked = StaticComponent.user.notification


    }
}