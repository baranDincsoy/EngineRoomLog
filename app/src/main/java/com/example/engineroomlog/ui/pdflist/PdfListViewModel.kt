package com.example.engineroomlog.ui.pdflist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.core.sync.JournalUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PdfItem(val file: File, val dayLabel: String, val uploaded: Boolean = false)

class PdfListViewModel(application: Application) : AndroidViewModel(application) {

    private val _pdfs = MutableStateFlow<List<PdfItem>>(emptyList())
    val pdfs: StateFlow<List<PdfItem>> = _pdfs.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val dir = File(getApplication<Application>().filesDir, "journals")
            val localFiles = (dir.listFiles { f -> f.extension == "pdf" } ?: emptyArray())
                .sortedByDescending { it.name }

            // Guarded remote lookup — offline returns cache, never throws
            val remote = try {
                JournalUploader.remoteJournalNames(getApplication())
            } catch (e: Exception) {
                emptySet()
            }
            _pdfs.value = localFiles.map { f ->
                val day = f.nameWithoutExtension.removePrefix("journal_")
                val label = day.split("-").let {
                    if (it.size == 3) "${it[2]}.${it[1]}.${it[0]}" else day
                }
                PdfItem(f, label, uploaded = f.name in remote)
            }
        }
    }
}