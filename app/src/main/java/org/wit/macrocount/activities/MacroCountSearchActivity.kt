package org.wit.macrocount.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import org.wit.macrocount.adapters.MacroCountAdapter
import org.wit.macrocount.adapters.MacroCountListener
import org.wit.macrocount.databinding.ActivityMacrocountSearchBinding
import org.wit.macrocount.main.MainApp
import org.wit.macrocount.models.MacroCountModel
import org.wit.macrocount.models.UserRepo
import timber.log.Timber
import android.app.AlertDialog


class MacroCountSearchActivity : AppCompatActivity(), MacroCountListener {

    private lateinit var app: MainApp
    private lateinit var binding: ActivityMacrocountSearchBinding
    private lateinit var adapter: MacroCountAdapter
    private lateinit var userRepo: UserRepo
    private lateinit var userMacros: List<MacroCountModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMacrocountSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        app = application as MainApp

        userRepo = UserRepo(applicationContext)
        val currentUserId = userRepo.userId

        val layoutManager = LinearLayoutManager(this)
        binding.macroSearchRecyclerView.layoutManager = layoutManager

        if (currentUserId != null) {
            userMacros = app.macroCounts.findByUserId(currentUserId.toLong())
            adapter = MacroCountAdapter(userMacros, this)
        }

        binding.macroSearchRecyclerView.adapter = adapter

        binding.macroSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.macroSearchView.clearFocus()
                if (query != null) {
                    val filteredMacros = userMacros.filter { it.title.contains(query, ignoreCase = true) }
                    adapter.updateData(filteredMacros)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    val filteredMacros = userMacros.filter { it.title.contains(newText, ignoreCase = true) }
                    adapter.updateData(filteredMacros)
                }
                return false
            }
        })

        binding.filterIcon.setOnClickListener {
            showFilterDialog(binding.filterIcon)
        }


    }

    override fun onMacroCountClick(macroCount: MacroCountModel) {
        // Handle item click here
        Timber.i("Selected item: $macroCount")
        val resultIntent = Intent()
        resultIntent.putExtra("macrocount_copy", macroCount)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onMacroDeleteClick(macroCount: MacroCountModel) {
        Timber.i("delete click")
    }

    private fun showFilterDialog(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Filter Options")

        val filterOptions = arrayOf("Option 1", "Option 2", "Option 3")
        val checkedItems = booleanArrayOf(false, false, false)

        builder.setMultiChoiceItems(
            filterOptions, checkedItems
        ) { dialog, which, isChecked ->

        }

        builder.setPositiveButton("Apply") { dialog, which ->

        }

        builder.setNegativeButton("Cancel") { dialog, which ->

        }

        val dialog = builder.create()
        dialog.show()
    }
}