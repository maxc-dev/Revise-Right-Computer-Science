package dev.maxc.quiz.bank

import dev.maxc.quiz.util.Question
import android.content.Context
import dev.maxc.quiz.R

/**
 * @author Max Carter
 */
class Bank(private val context: Context) {

    private var jsonString = ""

    init {
        collectData()
    }

    fun collectData() {
        val inStream = context.resources.openRawResource(R.raw.bank)
        jsonString = inStream.bufferedReader().use { it.readText() }
        inStream.close()
        jsonString = jsonString.replace("\n", "")
    }

    private fun tagExists(tag: String) = jsonString.contains("[$tag]")

    private fun tagEmpty(tag: String) = jsonString.contains("\"[$tag]\":{}")

    fun questions(tag: String) : ArrayList<Question>? {
        return when {
            tagEmpty(tag) -> null
            !tagExists(tag) -> null
            else -> {
                val questions = ArrayList<Question>()

                val condensed = jsonString.substringAfter("[$tag]\":{").substringBefore("}")
                for (text in condensed.split("\",\"")) {
                    questions.add(Question(
                        text.substringBefore("\":\"").replace("\"", ""),
                        text.substringAfter("\":\"").replace("\"", ""),
                        tag
                        )
                    )
                }

                questions
            }
        }
    }

    private fun questionsInTag(tag: String) = questions(tag)?.size

    fun questionsInTags(tags: ArrayList<String>) : Int {
        var count = 0
        tags.forEach { count+= questionsInTag(it)!! }
        return count
    }

    fun tags() : ArrayList<String> {
        val tags = ArrayList<String>()

        val matches = Regex("\\[[A-Za-z0-9 ]{1,64}\\]").findAll(jsonString)
        matches.forEach { matchResult ->
            val tag = matchResult.value.replace("[", "").replace("]", "")
            if (!tagEmpty(tag)) {
                tags.add(tag)
            }
        }

        return tags
    }

}