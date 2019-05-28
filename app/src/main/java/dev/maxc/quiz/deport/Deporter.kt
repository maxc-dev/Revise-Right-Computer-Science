package dev.maxc.quiz.deport

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * @author Max Carter
 */
object Deporter {

    fun deport(context: Context, link: Destination) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))

}