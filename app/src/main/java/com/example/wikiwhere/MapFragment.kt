package com.example.wikiwhere



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wikiwhere.API.FavoritesResponse
import com.example.wikiwhere.API.UserAPI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.log_in_screen.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        map_view.onCreate(savedInstanceState)
        map_view.onResume()

        map_view.getMapAsync(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val view: View = inflater.inflate(com.example.wikiwhere.R.layout.fragment_map, container, false)

        return view
    }

        override fun onMapReady(gmap: GoogleMap?) {
        gmap?.let {
            gMap  = it
        }

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
                            for(i in 0 until response.body()!!.size) {
                                val lat: Double = response.body()!!.elementAt(i).lat.toDouble()
                                val long: Double = response.body()!!.elementAt(i).lng.toDouble()
                                val description: String = response.body()!!.elementAt(i).placeName
                                val url: String = response.body()!!.elementAt(i).url
                                val article: String = response.body()!!.elementAt(i).article

                                gMap.addMarker(MarkerOptions().position(LatLng(lat, long)).title(description))
                                var camera: CameraPosition = CameraPosition.builder().target(LatLng(lat,long)).zoom(16F).build()

                                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera))
                                context?.toast("Maps Done")
                            }

                        }
                    }


                })
            }


    }


}

