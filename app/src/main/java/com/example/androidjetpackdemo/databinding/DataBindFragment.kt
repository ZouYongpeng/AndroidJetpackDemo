package com.example.androidjetpackdemo.databinding

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import com.example.androidjetpackdemo.bean.User


class DataBindFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewBinding = FragmentDataBindBinding.inflate(inflater).apply {
            var userName = ObservableField("ZYP")
            var userAge = ObservableField(20)
            var userSex = ObservableField(true)
            var userNote = ObservableField("null")

            user = User(userName, userAge, userSex, userNote)
            boy = "boy"
            girl = "girl"

            changeAgeBtn.setOnClickListener {
                Log.d("zyp", "note = ${userNote.get()} ")
                userName.set(userName.get() + "-1")
                userAge.set(userAge.get()?.plus(1))
            }
        }

        return viewBinding.root
    }

}

/**
 * @BindingAdapter(“isYoungUser”) 中的isYoungUser 对应xml文件中的 app:isYoungUser
 * 方法名不重要
 */
@BindingAdapter("isYoungUser")
fun isYoungUserMethod(view : View, age : Int) {
    view.background = ColorDrawable(
        if (age < 25 )
            Color.BLUE
        else
            Color.RED
    )
}
