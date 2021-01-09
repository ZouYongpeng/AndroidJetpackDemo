package com.example.androidjetpackdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

class MainFragment : Fragment() {

    companion object {
        const val KEY_BUNDLE = "KEY_BUNDLE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.findViewById<Button>(R.id.work_manager_btn).executeNavAction(
            R.id.action_mainFragment_to_workManagerFragment,
            Bundle().let {
                it.putString(KEY_BUNDLE, "Hello, WorkManager!")
                it
            })
        return view
    }

    private fun Button.executeNavAction(actionId : Int, bundle: Bundle? = null) {
        setOnClickListener {
            NavHostFragment.findNavController(this@MainFragment).navigate(actionId, bundle)
        }
    }

}