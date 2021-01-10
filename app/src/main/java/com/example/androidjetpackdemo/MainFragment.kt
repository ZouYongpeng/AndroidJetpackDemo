package com.example.androidjetpackdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.androidjetpackdemo.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    companion object {
        const val KEY_BUNDLE = "KEY_BUNDLE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentMainBinding.inflate(inflater).apply { initButton() }
        return binding.root
    }

    private fun FragmentMainBinding.initButton() {
        workManagerBtn.executeNavAction(
            R.id.action_mainFragment_to_workManagerFragment,
            Bundle().let {
                it.putString(KEY_BUNDLE, "Hello, WorkManager!")
                it
            })
        dataBindingBtn.executeNavAction(
            R.id.action_mainFragment_to_dataBindFragment)
    }

    private fun Button.executeNavAction(actionId : Int, bundle: Bundle? = null) {
        setOnClickListener {
            NavHostFragment.findNavController(this@MainFragment).navigate(actionId, bundle)
        }
    }

}