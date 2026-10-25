package com.planningpoker.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PlanningPokerDatabase::class.java,
    )

    @Test
    fun migrate1To2KeepsSprintsAndAddsTasksTable() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL("INSERT INTO sprints (id, name, created_at) VALUES (1, 'Sprint 41', 1000)")
            db.execSQL("INSERT INTO sprints (id, name, created_at) VALUES (2, 'Sprint 42', 2000)")
        }

        // Validates the migrated schema against the exported 2.json.
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        db.query("SELECT id, name, created_at FROM sprints ORDER BY id").use { cursor ->
            val rows = buildList {
                while (cursor.moveToNext()) {
                    add(Triple(cursor.getLong(0), cursor.getString(1), cursor.getLong(2)))
                }
            }
            assertEquals(
                listOf(Triple(1L, "Sprint 41", 1000L), Triple(2L, "Sprint 42", 2000L)),
                rows,
            )
        }
        db.query("SELECT COUNT(*) FROM tasks").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
