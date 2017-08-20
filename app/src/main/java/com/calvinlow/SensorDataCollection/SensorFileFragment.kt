package com.calvinlow.SensorDataCollection

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_sensorfile_list.view.*


/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class SensorFileFragment : Fragment() {

    companion object {
        var port: Int = 80
        var sensorFileRecyclerViewAdapter: SensorFileRecyclerViewAdapter? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_sensorfile_list, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        sensorFileRecyclerViewAdapter = SensorFileRecyclerViewAdapter(activity.filesDir.listFiles().toCollection(ArrayList()))
        view?.recyclerview?.adapter = sensorFileRecyclerViewAdapter
        view?.port_edittext?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrBlank()) {
                    view.port_edittext.error = "Port cannot be null or blank."
                    port = 80
                } else {
                    port = Integer.parseInt(text.toString())
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    fun invokeRefreshFile() {
        sensorFileRecyclerViewAdapter?.let {
            Log.d("Refresh", "List refreshed")
            val fileList = activity.filesDir.listFiles().toCollection(ArrayList())
            it.refreshFile(fileList)
        }
    }
}
