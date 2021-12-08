package com.ejavinas.robotmessfixer

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ScrollView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.ejavinas.robotmessfixer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.input.observe(this, {
            Log.d(TAG, "Input: $it")
            binding.input.text = it
            binding.inputScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        })

        viewModel.output.observe(this, {
            Log.d(TAG, "Output: $it")
            binding.output.text = it
            binding.outputScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refresh() {
        viewModel.runScenarios()
    }

    companion object {
        const val TAG = "MainActivity"
    }

}