package mbtinder.android.ui.fragment.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment

class AccountFragment : Fragment() {
    override fun initializeView() {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_account, inflater, container!!)
    }
}