package com.example.wikiwhere


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.wikiwhere.API.FavoritesResponse
import com.example.wikiwhere.API.UserAPI
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/**
 * A simple [Fragment] subclass.
 */
class NearbyFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_nearby, container, false)

        val listView = view.findViewById<ListView>(R.id.nearby_list)


        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://wiki-where.herokuapp.com/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val userapi: UserAPI = retrofit.create(UserAPI::class.java)

        val sharedpreferences: SharedPreferences = activity?.getSharedPreferences("preferences",
            Context.MODE_PRIVATE)!!

        val token = "Bearer " + sharedpreferences.getString("token","")

        val call: Call<List<FavoritesResponse>> = userapi.getFavs(token.toString())


        doAsync {

            call.enqueue(object : Callback<List<FavoritesResponse>> {
                override fun onFailure(call: Call<List<FavoritesResponse>>, t: Throwable) {
                    //context?.toast(t.message.toString())
                    Log.d("Kolbe",t.message)
                }

                override fun onResponse(
                    call: Call<List<FavoritesResponse>>,
                    response: Response<List<FavoritesResponse>>
                ) {
                    if (response.code() == 500){
                        context?.toast("server error")
                    }
                    if (response.code() == 400){
                        context?.toast("Error "+response.code())
                    }
                    else if (response.code() == 200){
                        var list: List<FavoritesResponse>? = response.body()

                        listView.adapter = ListAdapter(view.context, list)

                    }
                }
            })
        }

        listView.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            val response: FavoritesResponse? = listView.getItemAtPosition(i) as FavoritesResponse?
            val url: String = response!!.favorite.articleURL

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

        }

        return view
    }

}
