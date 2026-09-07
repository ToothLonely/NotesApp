package dev.toothlonely.notesapp.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.toothlonely.notesapp.core.data.database.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY is_completed ASC, created_at_millis DESC, id DESC
        """,
    )
    fun observeTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE id = :taskId")
    suspend fun updateCompleted(taskId: Long, isCompleted: Boolean): Int
}
