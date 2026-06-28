package com.example.engineroomlog.data.local.model

enum class EntryStatus {
    COLLECTING, // oiler is still entering values
    SUBMITTED,  // oiler finalized; awaiting engineer review
    POSTED      // engineer posted to journal; locked
}