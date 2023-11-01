package org.wit.macrocount.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.macrocount.R
import org.wit.macrocount.adapters.MacroCountAdapter
import org.wit.macrocount.adapters.MacroCountListener
import org.wit.macrocount.databinding.ActivityMacrocountListBinding
import org.wit.macrocount.main.MainApp
import org.wit.macrocount.models.MacroCountModel
import org.wit.macrocount.models.UserModel
import org.wit.macrocount.models.UserRepo
import timber.log.Timber
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class MacroCountListActivity : AppCompatActivity(), MacroCountListener {

    private lateinit var app: MainApp
    private lateinit var binding: ActivityMacrocountListBinding
    private lateinit var adapter: MacroCountAdapter
    private lateinit var userRepo: UserRepo
    //var userMacrosToday:
    //private var currentUser: UserModel? = null
    //private var currentUserId: String? = null
    //var search = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMacrocountListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        userRepo = UserRepo(applicationContext)
        val currentUserId = userRepo.userId

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        val userToday = app.days.findByUserDate(currentUserId!!.toLong(), LocalDate.now())
        val userTodayMacros = userToday?.userMacroIds?.mapNotNull { app.macroCounts.findById(it.toLong()) }

        val userTodayMacrosList = userTodayMacros?.toMutableList() ?: mutableListOf()

        Timber.i("userTodayMacros onCreate: $userTodayMacrosList")

        adapter = MacroCountAdapter(userTodayMacrosList, this)

        binding.recyclerView.adapter = adapter

//        var currentUserMacros = currentUserId?.let { app.macroCounts.findByUserId(it.toLong()) }
//        Timber.i("findByCurrentUser() at onCreate: $currentUserMacros")

        val fab: FloatingActionButton = findViewById(R.id.list_fab)

        fab.setOnClickListener {
            val launcherIntent = Intent(this, MacroCountActivity::class.java)
            getResult.launch(launcherIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.data_icon -> {
                val launcherIntent = Intent(this, MacroChartsActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        when (item.itemId) {
            R.id.item_profile -> {
                val launcherIntent = Intent(this, UserProfileActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {

                userRepo = UserRepo(applicationContext)
                val currentUserId = userRepo.userId

                if (currentUserId != null) {
                    val newMacroCounts = app.macroCounts.findByUserId(currentUserId.toLong())
                    adapter.updateData(newMacroCounts)
                    adapter.notifyDataSetChanged()
                }

            }
        }

    override fun onMacroCountClick(macroCount: MacroCountModel) {
        val launcherIntent = Intent(this, MacroCountActivity::class.java)
        launcherIntent.putExtra("macrocount_edit", macroCount)
        getClickResult.launch(launcherIntent)
    }

    override fun onMacroDeleteClick(macroCount: MacroCountModel) {
        val currentUserId = userRepo.userId

        if (currentUserId != null) {
            val userMacroCounts = app.macroCounts.findByUserId(currentUserId.toLong()).toMutableList()

            val position = userMacroCounts.indexOfFirst { it.id == macroCount.id }

            if (position != -1) {
                userMacroCounts.removeAt(position)
                app.macroCounts.delete(macroCount)
                adapter.updateData(userMacroCounts)
                adapter.notifyItemRemoved(position)
            }
        }
    }

    private val getClickResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                userRepo = UserRepo(applicationContext)

                val currentUserId = userRepo.userId

                if (currentUserId != null) {
                    adapter.updateData(app.macroCounts.findByUserId(currentUserId.toLong()))
                }

                adapter.notifyDataSetChanged()
            }
        }
}