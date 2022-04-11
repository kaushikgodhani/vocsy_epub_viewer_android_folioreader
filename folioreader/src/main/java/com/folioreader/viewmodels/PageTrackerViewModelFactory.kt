package com.folioreader.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PageTrackerViewModelFactory : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PageTrackerViewModel::class.java)) {
            return PageTrackerViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}