package com.example.wikiwhere

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast


class AccountFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_account, container, false)

        val clickable_text = view.findViewById<TextView>(R.id.btn_fragment_account)


        clickable_text.setOnClickListener {

            val sharedpreferences: SharedPreferences = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)!!
            val loggedin: Boolean = sharedpreferences.getBoolean("loggedin", true)

            if (loggedin) {
                clickable_text.text = "Sign Out"
            }else{
                clickable_text.text = "Sign In"
            }


            if (loggedin){
                sharedpreferences.edit().putBoolean("loggedin",false).apply()
                sharedpreferences.edit().putString("token","").apply()
                context?.toast("Logged Out")
                clickable_text.text = "Sign in"
            } else {
                val temp = 0
                startActivityForResult(Intent(activity, LogIn::class.java), temp)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val clickable_text = view?.findViewById<TextView>(R.id.btn_fragment_account)

        val sharedpreferences: SharedPreferences = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)!!
        val loggedin: Boolean = sharedpreferences.getBoolean("loggedin", true)

        if(loggedin)
            clickable_text?.text   = "Sign Out"
    }
}
