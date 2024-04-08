package com.profplay.catchthecorrectcolor

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.coroutines.supervisorScope

class Circle(context: Context) : AppCompatImageView (context) {
    private var isCorrect:Int = 0

    constructor(context: Context, isCorrect: Int) : this(context) {
        this.isCorrect=isCorrect

    }
    fun getIsCorret(): Int{
        return this.isCorrect
    }
    fun setIsCorret(isCorrect: Int){
        this.isCorrect = isCorrect
    }


}