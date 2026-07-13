package com.example.engineroomlog.core.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.model.EntryStatus
import com.example.engineroomlog.ui.journal.JournalRow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalPdfExporter(private val context: Context) {

    // A4 landscape in PostScript points (1/72 inch)
    private val pageWidth = 842
    private val pageHeight = 595
    private val margin = 36f
    private val rowH = 18f

    private val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
    private val headerPaint = Paint().apply { textSize = 9f; isFakeBoldText = true }
    private val cellPaint = Paint().apply { textSize = 9f }
    private val notePaint = Paint().apply { textSize = 8f; color = Color.DKGRAY }
    private val linePaint = Paint().apply { strokeWidth = 0.5f; color = Color.GRAY }

    companion object {
        // Single source of truth for the file location; ViewModel asks this too
        fun fileFor(context: Context, dayStartMillis: Long): File {
            val dir = File(context.filesDir, "journals")
            val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dayStartMillis))
            return File(dir, "journal_$day.pdf")
        }
    }

    fun export(
        dayStartMillis: Long,
        vesselName: String,
        parameters: List<Pair<String, ParameterEntity>>,   // group name + parameter
        rows: List<JournalRow>
    ): File {

        val doc = PdfDocument()
        val dateLabel = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(dayStartMillis))
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val stampFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        val tableW = pageWidth - 2 * margin
        val timeColW = 50f
        val minParamColW = 90f
        val colsPerPage = ((tableW - timeColW) / minParamColW).toInt().coerceAtLeast(1)
        val paramChunks = if (parameters.isEmpty()) listOf(emptyList())
        else parameters.chunked(colsPerPage)

        // A journal page is written once; never silently rewritten
        val existing = fileFor(context, dayStartMillis)
        if (existing.exists()) return existing

        var pageNo = 0
        fun newPage(): Pair<PdfDocument.Page, Canvas> {
            pageNo++
            val page = doc.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create()
            )
            val c = page.canvas
            c.drawText("Engine Room Log — $vesselName", margin, margin, titlePaint)
            c.drawText(
                dateLabel,
                pageWidth - margin - titlePaint.measureText(dateLabel),
                margin,
                titlePaint
            )
            return page to c
        }

        // --- Value table: one page set per column chunk, rows paginate downward ---
// --- Value table: one page set per column chunk, rows paginate downward ---
        for (chunk in paramChunks) {
            val paramColW = if (chunk.isEmpty()) 0f else (tableW - timeColW) / chunk.size
            var rowIndex = 0
            do {
                val (page, c) = newPage()
                var y = margin + 30f

                // Header: measured two-line labels, no truncation
// Group band above the column headers: label only on the group's first column
                var x = margin + timeColW
                var prevGroup: String? = null
                chunk.forEach { (groupName, _) ->
                    if (groupName != prevGroup) {
                        c.drawText(groupName, x + 2f, y, headerPaint)
                        prevGroup = groupName
                    }
                    x += paramColW
                }
                y += 12f

                x = margin
                c.drawText("Time", x + 2f, y, headerPaint)
                x += timeColW
                chunk.forEach { (_, p) ->
                    val label = p.name + (p.unit?.let { " ($it)" } ?: "")
                    val avail = paramColW - 4f
                    if (headerPaint.measureText(label) <= avail) {
                        c.drawText(label, x + 2f, y, headerPaint)
                    } else {
                        val cut = headerPaint.breakText(p.name, true, avail, null)
                        val line1 = p.name.take(cut)
                        val line2 = (p.name.drop(cut) + (p.unit?.let { " ($it)" } ?: "")).trim()
                        c.drawText(line1, x + 2f, y, headerPaint)
                        c.drawText(
                            if (headerPaint.measureText(line2) <= avail) line2
                            else line2.take(headerPaint.breakText(line2, true, avail, null)),
                            x + 2f, y + 10f, headerPaint
                        )
                    }
                    x += paramColW
                }
                y += 16f
                c.drawLine(margin, y, margin + tableW, y, linePaint)
                y += rowH

                // Data rows: time + one value cell per parameter
                while (rowIndex < rows.size && y < pageHeight - margin) {
                    val row = rows[rowIndex]
                    var cx = margin
                    c.drawText(timeFmt.format(Date(row.timestamp)), cx + 2f, y, cellPaint)
                    cx += timeColW
                    chunk.forEach { (_, p) ->
                        c.drawText(row.values[p.id] ?: "—", cx + 2f, y, cellPaint)
                        cx += paramColW
                    }
                    y += rowH
                    rowIndex++
                }
                doc.finishPage(page)
            } while (rowIndex < rows.size)
        }

        // --- Signatures & remarks: the accountability section ---
        var idx = 0
        do {
            val (page, c) = newPage()
            var y = margin + 30f
            c.drawText("Signatures & remarks", margin, y, headerPaint)
            y += rowH

            while (idx < rows.size && y < pageHeight - margin - rowH) {
                val row = rows[idx]
                val signed = if (row.status == EntryStatus.POSTED)
                    "Posted by ${row.postedByName ?: "—"}" +
                            (row.postedAt?.let { " at ${stampFmt.format(Date(it))}" } ?: "")
                else "UNSIGNED"
                c.drawText(
                    "${timeFmt.format(Date(row.timestamp))}   Collected by ${row.collectedByName}   ·   $signed",
                    margin, y, cellPaint
                )
                y += 12f
                row.remarks?.let {
                    c.drawText("Remarks: $it", margin + 40f, y, notePaint)
                    y += 12f
                }
                y += 4f
                idx++
            }
            doc.finishPage(page)
        } while (idx < rows.size)

        val file = fileFor(context, dayStartMillis)
        file.parentFile?.mkdirs()
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        file.setReadOnly()
        return file
    }
}