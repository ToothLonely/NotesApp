package dev.toothlonely.notesapp.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal val notesMigration1To2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE notes ADD COLUMN updated_at_millis INTEGER NOT NULL DEFAULT 0",
        )
        connection.execSQL(
            "UPDATE notes SET updated_at_millis = created_at_millis",
        )
    }
}

internal val notesMigration2To3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE notes ADD COLUMN image_file_name TEXT",
        )
    }
}

internal val notesMigration3To4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                is_completed INTEGER NOT NULL,
                created_at_millis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_tasks_is_completed_created_at_millis
            ON tasks (is_completed, created_at_millis)
            """.trimIndent(),
        )
    }
}

internal val notesMigration4To5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE tasks ADD COLUMN updated_at_millis INTEGER NOT NULL DEFAULT 0",
        )
        connection.execSQL(
            "UPDATE tasks SET updated_at_millis = created_at_millis",
        )
    }
}
