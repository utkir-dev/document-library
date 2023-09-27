package com.tiptop.app.common

import android.widget.SearchView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebouncingQueryTextListener(
    lifecycle: Lifecycle,
    private val onDebouncingQueryTextChange: (String?) -> Unit
    ) : SearchView.OnQueryTextListener, DefaultLifecycleObserver {
        var debouncePeriod: Long = 400

        private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

        private var searchJob: Job? = null

        init {
            lifecycle.addObserver(this)
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            searchJob?.cancel()
            searchJob = coroutineScope.launch {
                newText?.let {
                    delay(debouncePeriod)
                    onDebouncingQueryTextChange(newText)
                }
            }
            return false
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        private fun destroy() {
            searchJob?.cancel()
        }
    }