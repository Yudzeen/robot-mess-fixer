package com.ejavinas.robotmessfixer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grouping_exam.DataSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainViewModel: ViewModel() {

    private val disposables = CompositeDisposable()

    private val _input = MutableLiveData<String>()
    val input: LiveData<String> = _input

    private val _output = MutableLiveData<String>()
    val output: LiveData<String> = _output

    init {
        runScenarios()
    }

    fun runScenarios() {
        val robotMessFixer = RobotMessFixer()
        val inputStringBuilder = StringBuilder()
        val outputStringBuilder = StringBuilder()
        var day = 0

        DataSource
            .emit()
            .map {
                day++
                _input.postValue(inputStringBuilder.append("Day $day: $it\n").toString())
                robotMessFixer.fixMess(it)
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _output.value = outputStringBuilder.append("Day $day: $it\n").toString()
            }
            .bind()
    }

    private fun Disposable.bind() {
        disposables.add(this)
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

}