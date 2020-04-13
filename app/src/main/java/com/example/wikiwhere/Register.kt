package com.example.wikiwhere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wikiwhere.API.RegisterResponse
import com.example.wikiwhere.API.User
import com.example.wikiwhere.API.UserAPI
//import jdk.nashorn.internal.objects.NativeFunction.call
import kotlinx.android.synthetic.main.register_screen.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
//import org.junit.experimental.results.ResultMatchers.isSuccessful
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
//import sun.text.normalizer.UTF16.append


class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_screen)

        // on complete register button
        btn_finish_register.setOnClickListener {
            val username = username_field_register.text.toString()
            val password = password_field_register.text.toString()
            val email = email_field_register.text.toString()

            // reset all errors on each button press
            error_username.text = ""
            error_password.text = ""
            error_email.text = ""

            // check if username or password is blank
            if (username.isEmpty()) {
                error_username.text = "Username is a required field"
            } else if (password.isEmpty()) {
                error_password.text = "Password is a required field"
            } else if (email.isEmpty()) {
                error_email.text = "email is a required field"
            } else if (email != email_field_register_confirm.text.toString()) {
            error_email.text = "Emails do not match"
            }  else if (password != password_field_register_confirm.text.toString()) {
            error_password.text = "Passwords do not match"
            }
            else {
                // create new user
                val user: User = User(username, password, email)

                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://wiki-where.herokuapp.com/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

                val userapi: UserAPI = retrofit.create(UserAPI::class.java)

                val call: Call<RegisterResponse> = userapi.signUp(user)

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
                                error_username.text = "Server error"
                            }
                            if (response.code() == 400){
                                error_username.text = "Username taken"
                            }
                            else if (response.code() == 200){
                                toast("Registration successful")
                                finish()
                            }
                        }

                    })
                }

            }
        }
    }
}