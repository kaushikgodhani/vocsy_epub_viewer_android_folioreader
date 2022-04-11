package com.folioreader.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PageTrackerViewModel : ViewModel() {
    private val _currentChapter = MutableLiveData<Int?>()
    private val _currentPage = MutableLiveData<Int?>()

    private val _chapterPage = MutableLiveData<String?>()
    val chapterPage: LiveData<String?>
        get() = _chapterPage

    init {
        _chapterPage.value = "1 - 1"
    }

    fun setCurrentChapter(currentChapter: Int) {
        _currentChapter.value = currentChapter
        updateChapterPage()
    }

    fun setCurrentPage(currentChapter: Int) {
        _currentPage.value = currentChapter
        updateChapterPage()
    }

//    fun setFolioPageInfo(adapterPageCount: Int, currentPage: Int, currentChapter: Int) {
//        _chapterPage.value = "${_currentChapter.value} - $currentPage"
//    }

    private fun updateChapterPage() {
        _chapterPage.value = "${_currentChapter.value} - ${_currentPage.value}"
    }
}