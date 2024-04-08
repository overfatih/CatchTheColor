package com.profplay.catchthecorrectcolor

import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.get
import com.profplay.catchthecorrectcolor.databinding.ActivityMainBinding
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var circleView:ImageView? = null

    var myColors = HashMap<Int, Array<Any>>()
    var randomNumber: Int = 0
    private lateinit var circleList: List<Circle>

    var number: Double =0.0
    var bestScore : Double? = null
    var runnable : Runnable = Runnable {  }
    var handler : Handler = Handler(Looper.getMainLooper())
    /*
    doğru ve yanlış için iki circle sınıfı oluştur
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

        val scale = this.resources.displayMetrics.density
        for(item in myColors){
            circleView = ImageView(this)
            binding.cicleLinearLayout.addView(circleView)
            //circleView!!.id = "@+id/circleView ${item.key}" as Int
            circleView?.let {
                it.id = item.key
                it.layoutParams.height=  (94.toFloat() * scale + 0.5f).toInt()
                it.layoutParams.width =  (97.toFloat() * scale + 0.5f).toInt()
                it.setImageResource(R.drawable.daire)
                it.setColorFilter(item.value.get(0).toString().toInt())
                it.setOnClickListener {view ->
                    betouched(view) }
                circleList = listOf(Circle(this,0))
            }

        }
        binding.circleImage.visibility = View.GONE

    }

    fun startAction(view:View){
        randomNumber = (1..4).random()

        binding.colorTextView.text =  myColors[randomNumber]!!.get(1).toString()
        //circleList.get(randomNumber).setOnClickListener { catchedCircle(it) }
        myColors.keys.shuffled()

        for (iv in binding.cicleLinearLayout.children){
            var imageView: ImageView = iv as ImageView
            imageView.setColorFilter(myColors[2]!!.get(0).toString().toInt())

        }
        //circleList.forEach {
        //    it.getIsCorret().toString()
        //}

        //binding.circleImage.setColorFilter(myColors[randomNumber]!!.get(0).toString().toInt())
        binding.startButton.isEnabled= false
        //binding.circleImage.visibility = View.VISIBLE
        number = 0.0

        runnable = object : Runnable {
            override fun run() {

                number = number + 1
                binding.timerText.text = (number).toString()

                handler.postDelayed(this,10)

            }

        }

        handler.post(runnable)
        binding.startButton.isEnabled = false
    }

    fun stop() {
        handler.removeCallbacks(runnable)

    }

    fun catchedCircle (view: View){
        Toast.makeText(this, "Tebrikler", Toast.LENGTH_SHORT).show()
        /*val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setTitle("Result")
        alertDialogBuilder.setMessage("Tebrikler")
        alertDialogBuilder.show()*/
        stop()
        if(bestScore == null){
            bestScore=(number/100)
        }else{
            if(bestScore!! > (number/100)){bestScore=(number/100)}
        }
        binding.scoreTextView.text = "Score: ${bestScore}"
        binding.startButton.isEnabled = true
        binding.timerText.text = "Time: ${number/100}"

    }
    fun betouched (view: View){
        Toast.makeText(this, "Yanlış Cevap", Toast.LENGTH_SHORT).show()
        /*if(imageColor = myColors[randomNumber]!!.get(0).toString().toInt()){
            Toast.makeText(this, "Tebrikler", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, "Yanlış Cevap", Toast.LENGTH_SHORT).show()
        }*/
        stop()

    }
}