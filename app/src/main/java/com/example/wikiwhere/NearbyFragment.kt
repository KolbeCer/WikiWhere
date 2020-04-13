package com.example.wikiwhere


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.wikiwhere.API.Article
import com.example.wikiwhere.API.FavoritesResponse
import com.example.wikiwhere.API.UserAPI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.log_in_screen.*
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

        val call: Call<List<FavoritesResponse>> = userapi.getFavs()

        doAsync {
            call.enqueue(object : Callback<List<FavoritesResponse>> {
                override fun onFailure(call: Call<List<FavoritesResponse>>, t: Throwable) {
                    context?.toast(t.message.toString())
                }

                override fun onResponse(
                    call: Call<List<FavoritesResponse>>,
                    response: Response<List<FavoritesResponse>>
                ) {
                    if (response.code() == 500){
                        error_field_login.text = "Server error"
                    }
                    if (response.code() == 400){
                        error_field_login.text = "Invalid Login Information"
                    }
                    if (response.code() == 405){
                        error_field_login.text = "Account awaiting email verification"
                    }
                    else if (response.code() == 200){
                        var list = ArrayList<Article>()
                        for(i in 0 until response.body()!!.size) {
//                            var lat: Double = response.body()!!.elementAt(i).lat.toDouble()
//                            var long: Double = response.body()!!.elementAt(i).lng.toDouble()
                            var description: String = response.body()!!.elementAt(i).placeName
                            var url: String = response.body()!!.elementAt(i).url
                            var artic: String = response.body()!!.elementAt(i).article

                            var article: Article = Article()
                            context?.toast("Favorites Filled")

                            // add to list of favs
                            list.add(article)
                        }
                        listView.adapter = ListAdapter(view.context, list)

                    }
                }
            })
        }
        return view
    }
}
