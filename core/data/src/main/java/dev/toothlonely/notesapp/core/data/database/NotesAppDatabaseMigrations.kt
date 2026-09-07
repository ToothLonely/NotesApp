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
