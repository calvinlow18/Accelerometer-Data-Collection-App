package com.calvinlow.SensorDataCollection

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.request.SimpleMultiPartRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sensorfile.view.*
import org.json.JSONException
import org.json.JSONObject

import java.io.File

class SensorFileRecyclerViewAdapter(private val mValues: ArrayList<File>) : RecyclerView.Adapter<SensorFileRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_sensorfile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = mValues[position]
        holder.mItem = file
        holder.itemView.file_name.text = file.name
        holder.itemView.delete_button.setOnClickListener {
            mValues.remove(file)
            file.delete()
            Toast.makeText(holder.itemView.context, "${file.name} is deleted.", Toast.LENGTH_LONG).show()
            notifyItemRemoved(position)
        }
        holder.itemView.send_button.setOnClickListener {
            upload(position, holder.itemView.context)
        }
    }

    fun refreshFile(files: ArrayList<File>) {
        mValues.clear()
        mValues.addAll(files)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mItem: File? = null
    }

    private fun upload(position: Int, context: Context) {
        val requestQueue = Volley.newRequestQueue(context)

        val response = Response.Listener<String> { response ->
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(response)
                val status = jsonObject.getInt("status")

                if (status == 1) {
                    Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, jsonObject.getString("errorCode"), Toast.LENGTH_LONG).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        val error = Response.ErrorListener { error ->
            Toast.makeText(context, "Fail to upload file.", Toast.LENGTH_LONG).show()
        }

        val url = "http://192.168.1.${SensorFileFragment.port}:8080/api/accelerometer/send"
//        val url = "http://192.168.221.36:8080/api/accelerometer/send"

        val simpleMultiPartRequest = SimpleMultiPartRequest(Request.Method.POST, url, response, error)
        simpleMultiPartRequest.addFile("file", mValues.get(position).absolutePath)
        requestQueue.add(simpleMultiPartRequest)
        requestQueue.start()
    }

}
