package dev.maxc.quiz

import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.SearchView
import dev.maxc.quiz.bank.Bank
import dev.maxc.quiz.search.SearchAdapter

/**
 * @author Max Carter
 */
class SearchActivity : AppCompatActivity() {
    private var searchBar: SearchView? = null
    private var listView: RecyclerView? = null
    private var fabMenu: FloatingActionButton? = null

    private var searchAdapter: SearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_search)

        searchBar = findViewById(R.id.searchBar)
        listView = findViewById(R.id.searchList)
        fabMenu = findViewById(R.id.fabMenu)

        fabMenu?.setOnClickListener {
            startActivity(Intent(this@SearchActivity, MenuActivity::class.java))
        }

        makeSearchList()

        searchBar?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchAdapter!!.getFilter().filter(newText)
                return false
            }
        })

    }

    private fun makeSearchList() {
        val recyclerView: RecyclerView = findViewById(R.id.searchList)
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)

        val bank = Bank(this)
        searchAdapter = SearchAdapter(bank.allQuestions())

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = searchAdapter

        searchAdapter?.setOnClickListener(object : SearchAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                startActivity(Intent(this@SearchActivity, MainActivity::class.java).apply {
                    putExtra("custom", 1)
                    putExtra("custom-question", searchAdapter?.basicList?.get(position)?.question)
                    putExtra("custom-answer", searchAdapter?.basicList?.get(position)?.answer)
                    putExtra("custom-tag", searchAdapter?.basicList?.get(position)?.tag)
                })
            }
        })
    }

}
