package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource() : ReminderDataSource {



    //    TODO: Create a fake data source to act as a double to the real data source

    val fakeDb: LinkedHashMap<String ,ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) Result.Error("error") else Result.Success(fakeDb.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        fakeDb[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val selectedReminder = fakeDb[id]

        return if (selectedReminder == null)
            Result.Error("Reminder not found!")
        else
            Result.Success(selectedReminder)
    }

    override suspend fun deleteAllReminders() {
        fakeDb.clear()
    }


}