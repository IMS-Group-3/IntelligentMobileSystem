package com.example.ims

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class pathsListAdapter(context: Context, notelist: ArrayList<Path>) :
    ArrayAdapter<Path>(context, 0, notelist) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.paths_list_layout, parent, false)
        val path: Path = getItem(position)!!
        view.findViewById<TextView>(R.id.article_name_text_view).text = path.pathId
        view.findViewById<TextView>(R.id.article_id_text_view).text = path.startTime
        view.findViewById<TextView>(R.id.article_price_text_view).text= path.endTime

        return view

    }
}