package com.example.engineroomlog.data.local.converter

import androidx.room.TypeConverter
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.CrewRole
import com.example.engineroomlog.data.local.model.EntryStatus
import com.example.engineroomlog.data.local.model.OperationalState
import com.example.engineroomlog.data.local.model.Permission

class Converters {

    @TypeConverter
    fun fromOperationalState(value: OperationalState): String = value.name

    @TypeConverter
    fun toOperationalState(value: String): OperationalState =
        OperationalState.valueOf(value)

    @TypeConverter
    fun fromCadence(value: Cadence): String = value.name

    @TypeConverter
    fun toCadence(value: String): Cadence = Cadence.valueOf(value)

    @TypeConverter
    fun fromCrewRole(value: CrewRole): String = value.name

    @TypeConverter
    fun toCrewRole(value: String): CrewRole = CrewRole.valueOf(value)

    @TypeConverter
    fun fromEntryStatus(value: EntryStatus): String = value.name

    @TypeConverter
    fun toEntryStatus(value: String): EntryStatus = EntryStatus.valueOf(value)

    @TypeConverter
    fun fromPermission(value: Permission): String = value.name

    @TypeConverter
    fun toPermission(value: String): Permission = Permission.valueOf(value)
}