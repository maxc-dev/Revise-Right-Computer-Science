package dev.maxc.quiz

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.TextView
import kotlin.math.roundToInt

class DoneActivity : AppCompatActivity() {

    var showSupportButton: Button? = null
    var mainMenuButton: Button? = null
    var scoreDisplay: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_done)

        showSupportButton = findViewById(R.id.showSupportButton)
        mainMenuButton = findViewById(R.id.mainMenuButton)
        scoreDisplay = findViewById(R.id.scoreDisplay)

        val bundle: Bundle? = intent.extras
        val score = bundle!!.getInt("score")

        val builder = AlertDialog.Builder(this@DoneActivity)
        builder.setTitle(R.string.show_support)
        builder.setMessage("Designing all the components of this app, including writing all the (1000+) questions/answers for you, has taken a very long time. Any support is appreciated since I do this for free, whether it's a positive rating or a small donation. Thank you for using this app, and good luck with your exams! :)")
        builder.setPositiveButton("Hell Yeah") { _, _ -> url() }

        val dialog = builder.create()
        dialog.show()

        scoreDisplay?.text = "$score%"

        showSupportButton?.setOnClickListener {
            url()
        }

        mainMenuButton?.setOnClickListener {
            startActivity(Intent(this@DoneActivity, Splash::class.java))
        }
    }

    private fun url(url: String = "https://www.paypal.me/mxcrtr") = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

}
