package com.example.wikiwhere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.main_menu_screen.*

class MainMenu: AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_account -> {
                replaceFragment(AccountFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_map -> {
                replaceFragment(MapFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_favorites -> {
                replaceFragment(NearbyFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_menu_screen)

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        replaceFragment(AccountFragment())
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }
}