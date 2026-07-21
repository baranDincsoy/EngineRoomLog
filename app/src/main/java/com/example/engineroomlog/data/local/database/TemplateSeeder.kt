package com.example.engineroomlog.data.local.database

import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.entity.RankPermissionEntity
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.OperationalState
import com.example.engineroomlog.data.local.model.Permission
import com.example.engineroomlog.data.local.model.Ranks


// Seeds a realistic starter layout for a new vessel, modeled on a real engine-room log.
// The user reshapes it afterwards — the columns belong to the ship, this is a starting point.
object TemplateSeeder {

    // name, unit (null = none), operational state
    private data class ParamSpec(
        val name: String,
        val unit: String?,
        val state: OperationalState = OperationalState.AT_SEA
    )

    private val sampleLayout: List<Pair<String, List<ParamSpec>>> = listOf(
        "Main Engine" to listOf(
            ParamSpec("RPM", "rpm"),
            ParamSpec("L.O. Pressure", "bar"),
            ParamSpec("L.O. Temp", "°C"),
            ParamSpec("F.O. Pressure", "bar"),
            ParamSpec("Jacket CW Inlet", "°C"),
            ParamSpec("Jacket CW Outlet", "°C"),
            ParamSpec("Scav. Air Pressure", "bar"),
            ParamSpec("Exh. Temp Cyl 1", "°C"),
            ParamSpec("Exh. Temp Cyl 2", "°C"),
            ParamSpec("Exh. Temp Cyl 3", "°C"),
            ParamSpec("Exh. Temp Cyl 4", "°C"),
            ParamSpec("Exh. Temp Cyl 5", "°C"),
            ParamSpec("Exh. Temp Cyl 6", "°C"),
            ParamSpec("T/C RPM", "rpm"),
            ParamSpec("T/C Exh. Inlet", "°C")
        ),
        "No.1 Generator" to listOf(
            ParamSpec("Load", "kW", OperationalState.BOTH),
            ParamSpec("Voltage", "V", OperationalState.BOTH),
            ParamSpec("Frequency", "Hz", OperationalState.BOTH),
            ParamSpec("L.O. Pressure", "bar", OperationalState.BOTH),
            ParamSpec("FW Temp", "°C", OperationalState.BOTH),
            ParamSpec("SW Temp", "°C", OperationalState.BOTH)
        ),
        "No.2 Generator" to listOf(
            ParamSpec("Load", "kW", OperationalState.BOTH),
            ParamSpec("Voltage", "V", OperationalState.BOTH),
            ParamSpec("Frequency", "Hz", OperationalState.BOTH),
            ParamSpec("L.O. Pressure", "bar", OperationalState.BOTH),
            ParamSpec("FW Temp", "°C", OperationalState.BOTH),
            ParamSpec("SW Temp", "°C", OperationalState.BOTH)
        ),
        "No.3 Generator" to listOf(
            ParamSpec("Load", "kW", OperationalState.BOTH),
            ParamSpec("Voltage", "V", OperationalState.BOTH),
            ParamSpec("Frequency", "Hz", OperationalState.BOTH),
            ParamSpec("L.O. Pressure", "bar", OperationalState.BOTH),
            ParamSpec("FW Temp", "°C", OperationalState.BOTH),
            ParamSpec("SW Temp", "°C", OperationalState.BOTH)
        ),
        "No.4 Generator" to listOf(
            ParamSpec("Load", "kW", OperationalState.BOTH),
            ParamSpec("Voltage", "V", OperationalState.BOTH),
            ParamSpec("Frequency", "Hz", OperationalState.BOTH),
            ParamSpec("L.O. Pressure", "bar", OperationalState.BOTH),
            ParamSpec("FW Temp", "°C", OperationalState.BOTH),
            ParamSpec("SW Temp", "°C", OperationalState.BOTH)
        ),
        "Compressors" to listOf(
            ParamSpec("Main Air Press.", "bar", OperationalState.BOTH),
            ParamSpec("Service Air Press.", "bar", OperationalState.BOTH)
        ),
        "Coolers" to listOf(
            ParamSpec("M/E High Temp Cooler SW Inlet", "°C"),
            ParamSpec("M/E High Temp Cooler FW Inlet", "°C"),
            ParamSpec("M/E High Temp Cooler FW Outlet", "°C"),
            ParamSpec("M/E Low Temp Cooler SW Inlet", "°C"),
            ParamSpec("M/E Low Temp Cooler FW Inlet", "°C"),
            ParamSpec("M/E Low Temp Cooler FW Outlet", "°C"),
            ParamSpec("M/E Oil Cooler Inlet", "°C"),
            ParamSpec("M/E Oil Cooler Outlet", "°C"),
        ),
        "Tanks" to listOf(
            ParamSpec("F.O. Settling Tank", "cm", OperationalState.BOTH),
            ParamSpec("F.O. Service Tank", "cm", OperationalState.BOTH),
            ParamSpec("D.O. Settling Tank", "cm", OperationalState.BOTH),
            ParamSpec("D.O. Service Tank", "cm", OperationalState.BOTH)
        )
    )
    suspend fun seedDefaultPermissions(db: EngineRoomDatabase, vesselId: Long) {
        val dao = db.rankPermissionDao()

        // Seniority tiers — a sensible starting point the chief can reshape later
        val everything = Permission.entries.toList()
        val operational = listOf(
            Permission.RECORD_READINGS, Permission.POST_ENTRY, Permission.VIEW_JOURNAL,
            Permission.EXPORT_PDF, Permission.EDIT_FORM
        )
        val base = listOf(Permission.RECORD_READINGS, Permission.VIEW_JOURNAL)

        val byRank: Map<String, List<Permission>> = mapOf(
            Ranks.CHIEF_ENGINEER to everything,
            Ranks.SECOND_ENGINEER to everything,
            Ranks.THIRD_ENGINEER to operational,
            Ranks.FOURTH_ENGINEER to operational,
            Ranks.ELECTRICAL_OFFICER to operational,
            Ranks.FITTER to base,
            Ranks.MOTORMAN to base,
            Ranks.OILER to base,
            Ranks.WIPER to base
        )

        byRank.forEach { (rank, perms) ->
            perms.forEach { perm ->
                dao.grant(
                    RankPermissionEntity(
                        vesselProfileId = vesselId,
                        rank = rank,
                        permission = perm
                    )
                )
            }
        }
    }



    suspend fun seedSampleLayout(db: EngineRoomDatabase, vesselId: Long) {
        val groupDao = db.parameterGroupDao()
        val paramDao = db.parameterDao()


        sampleLayout.forEachIndexed { groupIndex, (groupName, params) ->
            val groupId = groupDao.insert(
                ParameterGroupEntity(
                    vesselProfileId = vesselId,
                    name = groupName,
                    displayOrder = groupIndex
                )
            )
            params.forEachIndexed { paramIndex, spec ->
                paramDao.insert(
                    ParameterEntity(
                        groupId = groupId,
                        name = spec.name,
                        unit = spec.unit,
                        state = spec.state,
                        cadence = Cadence.HOURLY,
                        displayOrder = paramIndex,
                        isDefault = true
                    )
                )
            }
        }
    }


}