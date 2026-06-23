package com.example.engineroomlog.data.local.converter

import androidx.room.TypeConverter
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.OperationalState

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
}