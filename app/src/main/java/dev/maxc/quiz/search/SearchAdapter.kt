package dev.maxc.quiz.search

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.maxc.quiz.R
import dev.maxc.quiz.util.Question
import android.view.LayoutInflater
import android.widget.Filter


/**
 * @author Max Carter
 */
class SearchAdapter(var fullList: ArrayList<Question>) : RecyclerView.Adapter<SearchAdapter.SearchHolder>() {
    var basicList: ArrayList<Question>? = null
    val MAX_ITEMS_TO_DISPLAY = 50
    var clickListener: OnItemClickListener? = null

    init {
        basicList = ArrayList()
    }

    class SearchHolder(itemView: View, clickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var questionTitle: TextView? = null
        var questionTopic: TextView? = null

        init {
            questionTitle = itemView.findViewById(R.id.questionTitle)
            questionTopic = itemView.findViewById(R.id.questionTopic)

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(adapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return SearchHolder(v, clickListener!!)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        val currentQuestion: Question? = basicList?.get(position)
        holder.questionTitle?.text = currentQuestion?.question
        holder.questionTopic?.text = currentQuestion?.tag
    }

    override fun getItemCount(): Int = basicList!!.size

    fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Question>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList = fullList
            } else {
                val filterPattern = constraint.toString().toLowerCase()

                for (question in fullList) {
                    if (question.question.toLowerCase().contains(filterPattern) or question.answer.toLowerCase().contains(filterPattern) && filteredList.size <= MAX_ITEMS_TO_DISPLAY) {
                        filteredList.add(question)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList.take(MAX_ITEMS_TO_DISPLAY).shuffled()

            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            basicList?.clear()
            @Suppress("UNCHECKED_CAST")
            basicList?.addAll(results.values as List<Question>)

            notifyDataSetChanged()
        }
    }

}