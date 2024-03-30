package com.profplay.catchthecorrectcolor

import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.profplay.catchthecorrectcolor.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var myColors = HashMap<Int, Array<Any>>()
    var randomNumber: Int = 0

    var handler : Handler = Handler(Looper.getMainLooper())
    /*
    doğru ve yanlış için iki sınıf oluştur
    hepsini başta gizle
    Bir tane doğru renk ata diğerlerini rastgele  yanlış sınıfından ata
    hedef rengin konumu değişsin
    sınıf içine funtion larını yaz
        doğru tıklandığında yeni soru gelsin yanlış tıklandığında game over olsun.
    ----
    ok* skorView oluştur
    en hızlı yakalamayı skora işle
    renk ve konuma göre hızlı yakalamalar arasında bir fark var mı? sorusu için verileri depola
    daha sonra alınan demokratif özelliklere göre karşılaştırma yapılabilir. :)
    */
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

        binding.circleImageView1.visibility = View.INVISIBLE

    }

    fun startAction(view:View){
        randomNumber = (1..4).random()
        binding.colarTextView.text=  myColors[randomNumber]!!.get(1).toString()
        binding.circleImageView1.setColorFilter(myColors[randomNumber]!!.get(0).toString().toInt())
        binding.startButton.isEnabled= false
        binding.circleImageView1.visibility = View.VISIBLE
        object : CountDownTimer(3000,1000) {
            override fun onFinish() {
                binding.timerText.text = "Left: 0"
                binding.startButton.isEnabled= true
                binding.colarTextView.text="-"
                binding.circleImageView1.visibility = View.INVISIBLE
            }

            override fun onTick(millisUntilFinished: Long) {
                binding.timerText.text = "Left: ${millisUntilFinished/1000}"
            }

        }.start()
    }

    fun catchedCircle (view: View){
        //Toast.makeText(this, "Tebrikler", Toast.LENGTH_SHORT).show()
        /*val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setTitle("Result")
        alertDialogBuilder.setMessage("Tebrikler")
        alertDialogBuilder.show()*/
    }
    fun betouched (view: View){
        //Toast.makeText(this, "Yanlış Cevap", Toast.LENGTH_SHORT).show()
        /*if(imageColor = myColors[randomNumber]!!.get(0).toString().toInt()){
            Toast.makeText(this, "Tebrikler", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, "Yanlış Cevap", Toast.LENGTH_SHORT).show()
        }*/
    }
}