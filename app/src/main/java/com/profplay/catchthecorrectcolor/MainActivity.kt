package com.profplay.catchthecorrectcolor

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import com.profplay.catchthecorrectcolor.databinding.ActivityMainBinding
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var circleView:ImageView? = null

    var myColors = HashMap<Int, Array<Any>>()
    var randomNumber: Int = 0
    private lateinit var circleList: List<Circle>

    var number :Double =0.0
    var bestScore :Double? = null
    var level :Int = 1
    var puan :Double = 0.0
    var runnable :Runnable = Runnable {  }
    var handler :Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        myColors[1] = arrayOf(Color.RED,"RED")
        myColors[2] = arrayOf(Color.BLUE,"BLUE")
        myColors[3] = arrayOf(Color.YELLOW,"YELLOW")
        myColors[4] = arrayOf(Color.GREEN,"GREEN")

        val scale = this.resources.displayMetrics.density
        for(item in myColors){
            circleView = ImageView(this)
            binding.circleLinearLayout.addView(circleView)
            circleView?.let {
                it.id = item.key
                it.layoutParams.height=  (94.toFloat() * scale + 0.5f).toInt()
                it.layoutParams.width =  (97.toFloat() * scale + 0.5f).toInt()
                it.setImageResource(R.drawable.daire)
                it.setColorFilter(item.value.get(0).toString().toInt())
                circleList = listOf(Circle(this,0))
            }

        }
        binding.levelTextView.text = "Level: ${level.toString()}"


    }

    fun startAction(view:View){
        randomNumber = (1..4).random()
        binding.colorTextView.text =  myColors[randomNumber]!!.get(1).toString()
        var shuffledColor = arrayOf(1,2,3,4)
        shuffledColor.shuffle()
        for (iv in binding.circleLinearLayout.children){
            var imageView = iv as ImageView
            imageView.isEnabled=true
            imageView.setColorFilter(myColors[shuffledColor[iv.id -1]]!!.get(0).toString().toInt())
            if(shuffledColor[iv.id -1]==randomNumber){
                imageView.setOnClickListener {view -> catchedCircle(view) }
            }else {
                imageView.setOnClickListener { view -> betouched(view) }
            }
        }

        binding.startButton.isEnabled= false
        binding.puanTextView.visibility = View.GONE
        number = 0.0
        runnable = object : Runnable {
            override fun run() {
                number = number + 1
                binding.timerText.text = (number).toString()
                handler.postDelayed(this,10)
                if(number>(300-(level))){timeUp()}
            }
        }
        handler.post(runnable)
        binding.startButton.isEnabled = false
    }

    fun stop() { handler.removeCallbacks(runnable) }

    fun catchedCircle (view: View){
        Toast.makeText(this, "Tebrikler", Toast.LENGTH_SHORT).show()
        println("Correct")
        stop()
        if(bestScore == null){
            bestScore = (number/100)
        }else{
            bestScore = ((bestScore!! * (level-1)) +  (number/100))/level
        }
        puan = level*(1/bestScore!!)
        binding.puanTextView.text = "Puan: ${puan.toString()}"
        println(puan)
        level += 1
        binding.scoreTextView.text = "Score: ${bestScore}"
        binding.timerText.text = "Time: ${number/100}"
        binding.levelTextView.text = "Level: ${level.toString()}"
        startAction(view)

    }
    fun betouched (view: View){
        resetGame("Yanlış Cevap")
    }

    fun timeUp(){
        resetGame("Süre Doldu!")
    }

    fun resetGame(message:String){
        for (iv in binding.circleLinearLayout.children){
            var imageView = iv as ImageView
            imageView.isEnabled=false
        }
        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            .setTitle("Result")
            .setMessage(message)
            .show()
        stop()
        binding.startButton.isEnabled = true
        println("Try Again")
        binding.puanTextView.visibility = View.VISIBLE
        bestScore = null
        number = 0.0
        level = 1
        puan=0.0

    }
}