package dev.maxc.quiz

import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.TextView
import dev.maxc.quiz.deport.Deporter
import dev.maxc.quiz.deport.Destination

/**
 * @author Max Carter
 */
class DoneActivity : AppCompatActivity() {

    private var reviewButton: Button? = null
    private var donateButton: Button? = null
    private var mainMenuButton: Button? = null
    private var scoreDisplay: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_done)

        reviewButton = findViewById(R.id.reviewButton)
        mainMenuButton = findViewById(R.id.mainMenuButton)
        donateButton = findViewById(R.id.donateButton)
        scoreDisplay = findViewById(R.id.scoreDisplay)

        val bundle: Bundle? = intent.extras
        val score = bundle!!.getInt("score")

        //0.33 chance of showing a support message
        if ((1..4).random() == 2) {
            val builder = AlertDialog.Builder(this@DoneActivity)
            builder.setTitle(R.string.show_support)
            builder.setMessage("Thank you for using this app, would you consider leaving a positive review?")
            builder.setPositiveButton("Review") { _, _ -> Deporter.deport(this, Destination.MARKET) }
            builder.setNegativeButton("Donate") { _, _ -> Deporter.deport(this, Destination.PAYPAL) }

            val dialog = builder.create()
            dialog.show()
        }

        scoreDisplay?.text = "$score%"

        reviewButton?.setOnClickListener {
            Deporter.deport(this, Destination.MARKET)
        }

        donateButton?.setOnClickListener {
            Deporter.deport(this, Destination.PAYPAL)
        }

        mainMenuButton?.setOnClickListener {
            startActivity(Intent(this@DoneActivity, Splash::class.java))
        }
    }

}
