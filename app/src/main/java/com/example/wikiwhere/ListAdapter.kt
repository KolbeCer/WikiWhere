package com.example.wikiwhere

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.wikiwhere.API.FavoritesResponse

class ListAdapter (private val context: Context,
                   private val dataSource: List<FavoritesResponse>?): BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource?.size!!
    }

    override fun getItem(position: Int): Any {
        return dataSource?.get(position)!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.nearby_list_item, parent, false)

        val titlTextview = rowView.findViewById<TextView>(R.id.item_title)
        val snippetTextview = rowView.findViewById<TextView>(R.id.item_snippet)
        val distanceTextview = rowView.findViewById<TextView>(R.id.item_url)


        titlTextview.text = dataSource?.elementAt(position)!!.favorite.placeName
        snippetTextview.text = dataSource?.elementAt(position)!!.favorite.articleTitle
        distanceTextview.text = dataSource?.elementAt(position)!!.favorite.articleURL

        // Get thumbnail element
        //val thumbnailImageView = rowView.findViewById(R.id.recipe_list_thumbnail) as ImageView
        //Picasso.with(context).load(recipe.imageUrl).placeholder(R.mipmap.ic_launcher).into(thumbnailImageView)

        return rowView
    }
}