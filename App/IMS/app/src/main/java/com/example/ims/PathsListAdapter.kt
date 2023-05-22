package com.example.ims

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView


class PathsListAdapter(context: Context, paths: ArrayList<Path>) :
    ArrayAdapter<Path>(context, 0, paths) {

    @SuppressLint("MissingInflatedId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.paths_list_layout, parent, false)

        val path: Path = getItem(position)!!
        view.findViewById<TextView>(R.id.article_name_text_view).text = path.pathId
        view.findViewById<TextView>(R.id.article_id_text_view).text = path.startTime
        view.findViewById<TextView>(R.id.article_price_text_view).text= path.endTime

        return view
    }
}