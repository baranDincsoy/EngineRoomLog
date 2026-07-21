package com.example.engineroomlog.data.local.model

// Discrete, code-defined capabilities. Grows as modules are added (STCW, work orders).
// Each value maps to something the code actually gates on.
enum class Permission {
    RECORD_READINGS,    // enter values into an entry
    POST_ENTRY,         // sign off / lock an entry
    EDIT_FORM,          // manage groups & parameters
    MANAGE_CREW,        // add/remove crew, reset passwords
    MANAGE_FLEET,       // connect device to fleet, sync
    VIEW_JOURNAL,       // view the journal
    EXPORT_PDF,         // create & upload PDFs
    CREATE_WORK_ORDER   // reserved: work-order module (not built yet)
}