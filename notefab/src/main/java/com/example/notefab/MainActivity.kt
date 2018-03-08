package com.example.notefab

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var listNotes = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        loadQuery("%")


        fab.setOnClickListener {
            val intent = Intent(this@MainActivity,
                    NoteActivity::class.java)
            startActivity(intent)

            }
        lvNotes.onItemClickListener = AdapterView.OnItemClickListener{
            _, _, position, _ ->
            Toast.makeText(this, "Click on " +
                    listNotes[position].title, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView: SearchView = menu!!.findItem(R.id.searchNote).actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                Toast.makeText(applicationContext, query, Toast.LENGTH_LONG).show()
                loadQuery("%$query%")
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchView.setOnCloseListener {
            loadQuery("%")
            false
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onResume() {
        super.onResume()
        loadQueryAll()
    }
    fun loadQueryAll() {
        var dbManager = NoteDbManager(this)
        val cursor = dbManager.queryAll()
        listNotes.clear()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val title = cursor.getString(cursor.getColumnIndex("Title"))
                val content = cursor.getString(cursor.getColumnIndex("Content"))
                listNotes.add(Note(id, title, content))
            } while (cursor.moveToNext())
        }
        val notesAdapter = NotesAdapter(this, listNotes)
        lvNotes.adapter = notesAdapter
    }
    inner class NotesAdapter(context: Context, private var notesList: ArrayList<Note>) : BaseAdapter() {
        private var context: Context? = context
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val view: View?
            val vh: ViewHolder
            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.note, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
                Log.i("JSA", "set Tag for ViewHolder, position: " + position)
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }
            val mNote = notesList[position]
            vh.tvTitle.text = mNote.title
            vh.tvContent.text = mNote.content
            vh.ivEdit.setOnClickListener {
                updateNote(mNote)
            }
            vh.ivDelete.setOnClickListener {
                val dbManager = NoteDbManager(this.context!!)
                val selectionArgs = arrayOf(mNote.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
            }
            return view
        }
        override fun getItem(position: Int): Any {
            return notesList[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getCount(): Int {
            return notesList.size
        }
    }
    private fun updateNote(note: Note) {
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("MainActId", note.id)
        intent.putExtra("MainActTitle", note.title)
        intent.putExtra("MainActContent", note.content)
        startActivity(intent)
    }
    private class ViewHolder(view: View?) {
        val tvTitle: TextView = view?.findViewById(R.id.tvTitle) as TextView
        val tvContent: TextView = view?.findViewById(R.id.tvContent) as TextView
        val ivEdit: ImageView = view?.findViewById(R.id.ivEdit) as ImageView
        val ivDelete: ImageView = view?.findViewById(R.id.ivDelete) as ImageView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.searchNote -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun loadQuery(title: String) {

        var dbManager = NoteDbManager(this)
        val projections = arrayOf("Id", "Title", "Content")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.query(projections, "Title like ?", selectionArgs, "Title")
        listNotes.clear()
        if (cursor.moveToFirst()) {

            do {
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val title = cursor.getString(cursor.getColumnIndex("Title"))
                val content = cursor.getString(cursor.getColumnIndex("Content"))

                listNotes.add(Note(id, title, content))

            } while (cursor.moveToNext())
        }

        var notesAdapter = NotesAdapter(this, listNotes)
        lvNotes.adapter = notesAdapter
    }
}
