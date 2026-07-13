package com.example.engineroomlog.ui.pdflist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class PdfItem(val file: File, val dayLabel: String)

class PdfListViewModel(application: Application) : AndroidViewModel(application) {

    private val _pdfs = MutableStateFlow<List<PdfItem>>(emptyList())
    val pdfs: StateFlow<List<PdfItem>> = _pdfs.asStateFlow()

    init { refresh() }

    fun refresh() {
        val dir = File(getApplication<Application>().filesDir, "journals")
        _pdfs.value = (dir.listFiles { f -> f.extension == "pdf" } ?: emptyArray())
            .sortedByDescending { it.name }          // newest day first
            .map { f ->
                // journal_2026-07-08.pdf -> 08.07.2026
                val day = f.nameWithoutExtension.removePrefix("journal_")
                val label = day.split("-").let {
                    if (it.size == 3) "${it[2]}.${it[1]}.${it[0]}" else day
                }
                PdfItem(f, label)
            }
    }
}