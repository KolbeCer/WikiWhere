package com.example.wikiwhere

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wikiwhere.API.RegisterResponse
import com.example.wikiwhere.API.User
import com.example.wikiwhere.API.UserAPI
import kotlinx.android.synthetic.main.log_in_screen.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class LogIn : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_in_screen)

        // listen for register button to be clicked
        btn_register.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }


        // listen for sign in button to be clicked
        btn_signin.setOnClickListener  {
            // grab current username and password fields
            val username = username_field.text.toString()
            val password = password_field.text.toString()
            val email = ""
            // reset all errors on each button press
            error_username.text = ""
            error_password.text = ""
            error_field_login.text = ""

            // check if username or password is blank
            if(username.isEmpty()){
                error_username.text = "Username is a required field"
            }
            else if(password.isEmpty()){
                error_password.text = "Password is a required field"
            }
            else {
                val user: User = User(username, password, email)

                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://wiki-where.herokuapp.com/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

                val userapi: UserAPI = retrofit.create(UserAPI::class.java)

                val call: Call<RegisterResponse> = userapi.logIn(user)

                doAsync {
                    call.enqueue(object : Callback<RegisterResponse?> {
                        override fun onFailure(call: Call<RegisterResponse?>, t: Throwable) {
                            toast(t.message.toString())
                        }

                        override fun onResponse(
                            call: Call<RegisterResponse?>,
                            response: Response<RegisterResponse?>
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
                                val sharedpreferences: SharedPreferences = getSharedPreferences("preferences",0)
                                sharedpreferences.edit().putString("token",response.body()?.token).apply()
                                sharedpreferences.edit().putBoolean("loggedin", true).apply()

                                toast("Login successful")
                                finish()
                            }
                        }

                    })
                }
            }
        }
    }
}