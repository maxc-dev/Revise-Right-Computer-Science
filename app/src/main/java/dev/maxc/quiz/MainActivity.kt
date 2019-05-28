package dev.maxc.quiz

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.design.widget.FloatingActionButton
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import dev.maxc.quiz.bank.Bank
import dev.maxc.quiz.util.Question
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import android.view.animation.Animation


/**
 * @author Max Carter
 */
class MainActivity : AppCompatActivity() {

    private var x1: Float? = null
    private var x2: Float? = null

    private var displayQuestion: TextView? = null
    private var displayAnswer: TextView? = null
    private var tagDisplay: TextView? = null
    private var swipeHelp: TextView? = null
    private var swipeDirection: TextView? = null
    private var revealAnswer: Button? = null
    private var progressBar: ProgressBar? = null
    private var fabTTS: FloatingActionButton? = null
    private var fabSwiper: FloatingActionButton? = null

    private var textToSpeech: TextToSpeech? = null

    private var currentQuestion: Question? = null

    private var questions: ArrayList<Question>? = null
    private var questionsCorrect = 0
    private var questionsAsked = 0

    private val fadeIn = AlphaAnimation(0.0f, 1.0f).apply { duration = 320 }
    private val fadeOut = AlphaAnimation(1.0f, 0.0f).apply { duration = 200 }

    private var swipeRightCorrect = true

    private val sharedPreferencesName = "revise-right-prefs"
    private val swipeRightCorrectPreference = "swipe_right_correct"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        displayQuestion = findViewById(R.id.questionDisplay)
        displayAnswer = findViewById(R.id.answerDisplay)
        revealAnswer = findViewById(R.id.revealButton)
        progressBar = findViewById(R.id.progressBarQuestions)
        tagDisplay = findViewById(R.id.tagDisplay)
        swipeHelp = findViewById(R.id.swipeHelp)
        fabTTS = findViewById(R.id.fabTTS)
        fabSwiper = findViewById(R.id.fabSwiper)
        swipeDirection = findViewById(R.id.swiperText)

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.UK
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(s: String) {
                        fabTTS?.setImageResource(R.drawable.volume_off)
                    }

                    override fun onDone(s: String) {
                        fabTTS?.setImageResource(R.drawable.volume_on)
                    }

                    override fun onError(s: String) {
                        fabTTS?.setImageResource(R.drawable.volume_on)
                    }
                })
            }
        })

        fabTTS?.setOnClickListener {
            if (currentQuestion != null) {
                if (textToSpeech!!.isSpeaking) {
                    textToSpeech?.stop()
                    fabTTS?.setImageResource(R.drawable.volume_on)
                } else {
                    speak(if (displayAnswer?.visibility == View.VISIBLE) currentQuestion!!.answer else currentQuestion!!.question)
                }
            }
        }

        val preferences = applicationContext.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val edit = preferences.edit()

        swipeRightCorrect = preferences.getBoolean(swipeRightCorrectPreference, true)
        swiperPreference(swipeRightCorrect)

        fabSwiper?.setOnClickListener {
            swipeRightCorrect = !swipeRightCorrect
            swiperPreference(swipeRightCorrect)
            edit.putBoolean(swipeRightCorrectPreference, swipeRightCorrect)
            edit.apply()
        }

        revealAnswer?.setOnClickListener {
            revealAnswer?.visibility = View.INVISIBLE
            displayAnswer?.visibility = View.VISIBLE
            displayAnswer?.startAnimation(fadeIn)
            swipeHelp?.visibility = View.VISIBLE
            swipeHelp?.startAnimation(fadeIn)
        }

        val bundle: Bundle? = intent.extras
        val tag = bundle!!.getString("tags")
        val count = bundle.getInt("count")

        val tagList = tag.split("}{")

        val bank = Bank(this)
        questions = ArrayList()

        val each = count/tagList.size

        if (count == 0) {
            tagList.shuffled().forEach { tag ->
                bank.questions(tag)!!.shuffled().forEach {
                    questions?.add(it)
                }
            }
        } else {
            tagList.forEach { tag ->
                val sampleQuestionsTag = bank.questions(tag)
                repeat(each) {
                    if (sampleQuestionsTag!!.isNotEmpty()) {
                        val questionSample = sampleQuestionsTag.random()
                        questions?.add(questionSample)
                        sampleQuestionsTag.remove(questionSample)
                    } else {
                        //idk if this works
                        return@forEach
                    }
                }
            }
        }

        questions?.shuffle()
        progressBar?.max = questions!!.size

        askQuestion()
    }

    private fun swiperPreference(swipeRight: Boolean) {
        swiperText?.text = if (swipeRight) "Right" else "Left"
        swipeHelp?.text = Swipe.help(swipeRight)
        Toast.makeText(this, Swipe.help(swipeRight), Toast.LENGTH_LONG).show()
    }

    private fun speak(text: String = "") = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)

    override fun onDestroy() {
        super.onDestroy()

        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    private fun askQuestion() {
        if (questions!!.isEmpty()) {
            //no more questions
            val score = (questionsCorrect*100)/questionsAsked
            startActivity(Intent(this@MainActivity, DoneActivity::class.java).apply { putExtra("score" , score) })
            return
        }
        questionsAsked++
        fabTTS?.setImageResource(R.drawable.volume_on)

        //select random question to use
        if (questions!!.size == 1) {
            currentQuestion = questions!![0]
        } else {
            var nextQuestion = currentQuestion
            while (nextQuestion == currentQuestion) {
                nextQuestion = questions!!.random()
            }
            currentQuestion = nextQuestion
        }

        //fade in new question
        displayQuestion?.text = currentQuestion?.question
        displayQuestion?.startAnimation(fadeIn)
        tagDisplay?.text = currentQuestion?.tag
        tagDisplay?.startAnimation(fadeIn)
        revealAnswer?.visibility = View.VISIBLE
        revealAnswer?.startAnimation(fadeIn)

        //fade out the old question
        displayAnswer?.startAnimation(fadeOut)
        swipeHelp?.startAnimation(fadeOut)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) { }

            override fun onAnimationEnd(animation: Animation) {
                swipeHelp?.visibility = View.INVISIBLE
                displayAnswer?.visibility = View.INVISIBLE
                displayAnswer?.text = currentQuestion?.answer
            }

            override fun onAnimationRepeat(animation: Animation) { }
        })
    }

    override fun onTouchEvent(touchEvent: MotionEvent) : Boolean {
        if (revealAnswer?.visibility == View.VISIBLE) {
            return false
        }

        if (touchEvent.action == MotionEvent.ACTION_DOWN) {
            x1 = touchEvent.x

        } else if (touchEvent.action == MotionEvent.ACTION_UP) {
            x2 = touchEvent.x

            if (Math.abs(x1!!.toInt() - x2!!.toInt()) < 360) {
                return false
            }

            if (x1!! < x2!!) {
                swipe(true)
            } else {
                swipe(false)
            }
        }

        return false
    }

    private fun swipe(right: Boolean) {
        textToSpeech?.stop()
        if (right == swipeRightCorrect && questions!!.isNotEmpty()) {
            questionsCorrect++
            progressBar?.progress = questionsCorrect
            questions?.remove(currentQuestion)
        }
        askQuestion()
    }

    object Swipe {

        fun direction(direction: Boolean) : String = if (direction) "right" else "left"

        fun help(direction: Boolean) : String = "Swipe ${direction(direction)} if you are correct, otherwise swipe ${direction(!direction)}."

    }

}
