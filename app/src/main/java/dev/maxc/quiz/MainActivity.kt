package dev.maxc.quiz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.design.widget.FloatingActionButton
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import dev.maxc.quiz.bank.Bank
import dev.maxc.quiz.util.Question
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Max Carter
 */
class MainActivity : AppCompatActivity() {

    private var x1: Float? = null
    private var x2: Float? = null

    private var displayQuestion: TextView? = null
    private var displayAnswer: TextView? = null
    private var tagDisplay: TextView? = null
    private var revealAnswer: Button? = null
    private var progressBar: ProgressBar? = null
    private var fabTTS: FloatingActionButton? = null

    private var textToSpeech: TextToSpeech? = null

    private var currentQuestion: Question? = null

    private var questions: ArrayList<Question>? = null
    private var questionsCorrect = 0
    private var questionsAsked = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayQuestion = findViewById(R.id.questionDisplay)
        displayAnswer = findViewById(R.id.answerDisplay)
        revealAnswer = findViewById(R.id.revealButton)
        progressBar = findViewById(R.id.progressBarQuestions)
        tagDisplay = findViewById(R.id.tagDisplay)
        fabTTS = findViewById(R.id.fabTTS)

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.UK
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(s: String) {}

                    override fun onDone(s: String) {}

                    override fun onError(s: String) {}
                })
            }
        })

        fabTTS?.setOnClickListener {
            if (currentQuestion != null) {
                speak(if (displayAnswer?.visibility == View.VISIBLE) currentQuestion!!.answer else currentQuestion!!.question)
            }
        }

        revealAnswer?.setOnClickListener {
            revealAnswer?.visibility = View.INVISIBLE
            displayAnswer?.visibility = View.VISIBLE
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
                        println(" -- -- > EMPTY $tag --- $it")
                        return@forEach
                    }
                }
            }
        }

        questions?.shuffle()
        progressBar?.max = questions!!.size

        askQuestion()
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

        displayQuestion?.text = currentQuestion?.question
        tagDisplay?.text = currentQuestion?.tag
        revealAnswer?.visibility = View.VISIBLE
        displayAnswer?.visibility = View.INVISIBLE
        displayAnswer?.text = currentQuestion?.answer
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
        if (right && questions!!.isNotEmpty()) {
            questionsCorrect++
            progressBar?.progress = questionsCorrect
            questions?.remove(currentQuestion)
        }
        askQuestion()
    }

}
