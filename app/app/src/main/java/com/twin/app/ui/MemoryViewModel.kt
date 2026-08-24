package com.twin.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twin.app.TwinApp
import com.twin.app.data.MemoryFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MemoryViewModel : ViewModel() {
    private val app get() = TwinApp.instance

    private val _files = MutableStateFlow<List<MemoryFile>>(emptyList())
    val files: StateFlow<List<MemoryFile>> = _files

    private val _path = MutableStateFlow<String?>(null)
    val path: StateFlow<String?> = _path

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    fun setDraft(text: String) {
        _draft.value = text
    }

    fun refresh() {
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _files.value = app.api.listMemory()
                _status.value = null
            } catch (e: Exception) {
                _status.value = e.message ?: "load failed"
            }
            _busy.value = false
        }
    }

    fun open(path: String) {
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _draft.value = app.api.getMemory(path)
                _path.value = path
                _status.value = null
            } catch (e: Exception) {
                _status.value = e.message ?: "open failed"
            }
            _busy.value = false
        }
    }

    fun closeEditor() {
        _path.value = null
        _draft.value = ""
    }

    fun save() {
        val path = _path.value ?: return
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                app.api.putMemory(path, _draft.value)
                _files.value = app.api.listMemory()
                _status.value = "saved"
            } catch (e: Exception) {
                _status.value = e.message ?: "save failed"
            }
            _busy.value = false
        }
    }

    fun create(raw: String) {
        var path = raw.trim().trimStart('/')
        if (path.isBlank()) return
        if (!path.endsWith(".md")) path += ".md"
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val title = path.removeSuffix(".md")
                app.api.putMemory(path, "# $title\n\n")
                _draft.value = app.api.getMemory(path)
                _path.value = path
                _files.value = app.api.listMemory()
                _status.value = "created"
            } catch (e: Exception) {
                _status.value = e.message ?: "create failed"
            }
            _busy.value = false
        }
    }

    fun deleteCurrent() {
        val path = _path.value ?: return
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                app.api.deleteMemory(path)
                _path.value = null
                _draft.value = ""
                _files.value = app.api.listMemory()
                _status.value = "deleted"
            } catch (e: Exception) {
                _status.value = e.message ?: "delete failed"
            }
            _busy.value = false
        }
    }
}
