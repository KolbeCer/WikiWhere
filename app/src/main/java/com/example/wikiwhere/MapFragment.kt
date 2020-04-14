package com.example.wikiwhere



import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wikiwhere.API.AuthToken
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

        return inflater.inflate(R.layout.fragment_map, container, false)
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
            val sharedpreferences: SharedPreferences = activity?.getSharedPreferences("preferences",
                Context.MODE_PRIVATE)!!

            val token = "Bearer " + sharedpreferences.getString("token","")

            val call: Call<List<FavoritesResponse>> = userapi.getFavs(token.toString())

            btn_Nearby_Places.setOnClickListener {
                startActivity(Intent(activity, com.example.wikiwhere.NearbyPlaces.MainActivity::class.java))
            }


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
                            var camera: CameraPosition = CameraPosition.builder().target(LatLng(0.0,0.0)).build()

                            for(i in response.body()!!.indices) {
                                val lat: Double = response.body()!!.elementAt(i).favorite.placeLocation.lat.toDouble()
                                val long: Double = response.body()!!.elementAt(i).favorite.placeLocation.lng.toDouble()
                                val name: String = response.body()!!.elementAt(i).favorite.placeName
                                val location: LatLng = LatLng(lat, long)
                                // val url: String = response.body()!!.elementAt(i).url
                                // val article: String = response.body()!!.elementAt(i).articleName

                                gMap.addMarker(MarkerOptions().position(location).title(name)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                var camera: CameraPosition = CameraPosition.builder().target(location).zoom(16F).build()


                            }
                            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera))
                        }
                    }


                })
            }


    }


}

