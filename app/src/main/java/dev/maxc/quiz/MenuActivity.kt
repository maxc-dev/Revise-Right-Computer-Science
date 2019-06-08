package dev.maxc.quiz

import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import dev.maxc.quiz.bank.Bank
import java.util.*
import dev.maxc.quiz.deport.Deporter
import dev.maxc.quiz.deport.Destination
import dev.maxc.quiz.pref.SharedPref

/**
 * @author Max Carter
 */
class MenuActivity : AppCompatActivity() {
    private var topicDisplay: TextView? = null
    private var questionCountPicker: NumberPicker? = null
    private var launchButton: Button? = null
    private var listTopicsButton: Button? = null
    private var fabSearch: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_menu)

        val pref = applicationContext.getSharedPreferences(SharedPref.sharedPreferencesName, MODE_PRIVATE)
        val edit = pref.edit()

        //if the user is in a new version, give them an update
        if (pref.getString(SharedPref.lastAppVersionUsed, null) != BuildConfig.VERSION_NAME) {
            edit.putString(SharedPref.lastAppVersionUsed, BuildConfig.VERSION_NAME)
            edit.apply()

            val inStream = resources.openRawResource(R.raw.patch_notes)
            val update = inStream.bufferedReader().use { it.readText() }
            inStream.close()

            val builder = AlertDialog.Builder(this@MenuActivity)
            builder.setTitle("What's New? (v${BuildConfig.VERSION_NAME})")
            builder.setMessage(update)
            builder.setPositiveButton("OK") { _, _ -> }
            builder.setNeutralButton("More") { _, _ -> Deporter.deport(this, Destination.MARKET) }

            val dialog = builder.create()
            dialog.show()
        }

        questionCountPicker = findViewById(R.id.questionCount)
        topicDisplay = findViewById(R.id.topicDisplay)
        launchButton = findViewById(R.id.launchButton)
        listTopicsButton = findViewById(R.id.listTopics)
        fabSearch = findViewById(R.id.fabSearch)

        fabSearch?.setOnClickListener {
            startActivity(Intent(this@MenuActivity, SearchActivity::class.java))
        }

        val bank = Bank(this)
        val tagList = bank.tags()
        val selected = BooleanArray(tagList.size) { false }
        val tagSequence = Array<CharSequence>(tagList.size) { i -> tagList[i] }
        val emptyTagList = ArrayList<String>()
        topicDisplay(emptyTagList, bank)

        val builder = AlertDialog.Builder(this@MenuActivity)
        builder.setTitle(R.string.select_topics)

        listTopicsButton?.setOnClickListener {
            builder.setMultiChoiceItems(tagSequence, selected) { _, indexSelected, isChecked ->
                selected[indexSelected] = isChecked
                if (isChecked && !emptyTagList.contains(tagSequence[indexSelected])) {
                    emptyTagList.add(tagSequence[indexSelected].toString())
                } else if (!isChecked && emptyTagList.contains(tagSequence[indexSelected])) {
                    emptyTagList.remove(tagSequence[indexSelected].toString())
                }

                if (emptyTagList.isNotEmpty()) {
                    topicDisplay?.text = emptyTagList.joinToString()
                }
            }.setPositiveButton("DONE") { _, _ -> topicDisplay(emptyTagList, bank) }.setCancelable(false)

            val dialog = builder.create()
            dialog.show()
        }

        launchButton?.setOnClickListener {
            var count = questionCountPicker!!.value

            load(emptyTagList.joinToString(separator = "}{"), count)
        }
    }

    private fun updateNumberPicker(min: Int, max: Int) {
        questionCountPicker?.minValue = min
        questionCountPicker?.maxValue = max
        questionCountPicker?.value = max/2
    }

    private fun toggleSwitch(isChecked: Boolean) {
        questionCountPicker?.visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
    }

    private fun topicDisplay(list: ArrayList<String>, bank: Bank) {
        if (list.isNotEmpty()) {
            topicDisplay?.text = list.joinToString()
            updateNumberPicker(list.size, bank.questionsInTags(list))
            launchButton?.isEnabled = true
            questionCountPicker?.visibility = View.VISIBLE
        } else {
            questionCountPicker?.visibility = View.INVISIBLE
            topicDisplay?.text = getString(R.string.no_topics_selected)
            launchButton?.isEnabled = false
            toggleSwitch(false)
        }
    }

    private fun load(tag: String, count: Int) {
        startActivity(Intent(this@MenuActivity, MainActivity::class.java).apply {
            putExtra("tags", tag)
        }.apply {
            putExtra("count", count)
        })
    }

    override fun onBackPressed() { }
}
