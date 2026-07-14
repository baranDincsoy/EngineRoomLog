package com.example.engineroomlog.data.local.database

import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.OperationalState

// Seeds a sensible starter layout for a new vessel. The user reshapes it afterwards —
// the columns belong to the ship, this is just a starting point.
object TemplateSeeder {

    suspend fun seedSampleLayout(db: EngineRoomDatabase, vesselId: Long) {
        val groupDao = db.parameterGroupDao()
        val paramDao = db.parameterDao()

        val mainEngineId = groupDao.insert(
            ParameterGroupEntity(vesselProfileId = vesselId, name = "Main Engine", displayOrder = 0)
        )
        val generatorId = groupDao.insert(
            ParameterGroupEntity(vesselProfileId = vesselId, name = "Generators", displayOrder = 1)
        )

        val mainEngine = listOf(
            Triple("RPM", "rpm", OperationalState.AT_SEA),
            Triple("L.O. Pressure", "bar", OperationalState.AT_SEA),
            Triple("Jacket CW Outlet", "°C", OperationalState.AT_SEA)
        )
        mainEngine.forEachIndexed { i, (name, unit, state) ->
            paramDao.insert(
                ParameterEntity(
                    groupId = mainEngineId, name = name, unit = unit, state = state,
                    cadence = Cadence.HOURLY, displayOrder = i, isDefault = true
                )
            )
        }

        val generators = listOf(
            Triple("No.1 Gen Voltage", "V", OperationalState.BOTH),
            Triple("No.1 Gen Load", "kW", OperationalState.BOTH)
        )
        generators.forEachIndexed { i, (name, unit, state) ->
            paramDao.insert(
                ParameterEntity(
                    groupId = generatorId, name = name, unit = unit, state = state,
                    cadence = Cadence.HOURLY, displayOrder = i, isDefault = true
                )
            )
        }
    }
}