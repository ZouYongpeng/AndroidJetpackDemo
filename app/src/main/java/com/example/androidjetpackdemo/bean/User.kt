package com.example.androidjetpackdemo.bean

import androidx.databinding.ObservableField

data class User(
    var name: ObservableField<String> = ObservableField("Unknow"),
    var age: ObservableField<Int> = ObservableField(0),
    var sex : ObservableField<Boolean> = ObservableField(true),
    var note : ObservableField<String> = ObservableField("null"))