package com.calvinlow.SensorDataCollection

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    object Fragments {
        val dataCollectionFragment = DataCollectionFragment()

        val sensorFileFragment = SensorFileFragment()
    }

    private var currentFragment: Fragment = Fragments.dataCollectionFragment

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_home -> {
                showHideFragment(Fragments.dataCollectionFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                Fragments.sensorFileFragment.invokeRefreshFile()
                showHideFragment(Fragments.sensorFileFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val transaction = supportFragmentManager.beginTransaction()

        transaction.add(R.id.content, Fragments.sensorFileFragment)
                .hide(Fragments.sensorFileFragment)
                .add(R.id.content, Fragments.dataCollectionFragment)
                .commit()

    }

    private fun showHideFragment(show:Fragment) {
        if (currentFragment != show) {
            supportFragmentManager.beginTransaction()
                    .hide(currentFragment)
                    .show(show)
                    .commit()
            currentFragment = show
        }
    }

}
