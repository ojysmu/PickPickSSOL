package mbtinder.android.ui.fragment.message_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment

class MessageListFragment : Fragment() {
    override fun initializeView() {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_message_list, inflater, container!!)
    }
}