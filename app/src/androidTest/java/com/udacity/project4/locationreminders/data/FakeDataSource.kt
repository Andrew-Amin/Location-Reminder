package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private val reminderDao: RemindersDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) Result.Error("error")
        else
            Result.Success(reminderDao.getReminders())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) =
        reminderDao.saveReminder(reminder)


    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError)
            return Result.Error("An Exception has been occurred")


            val selectedReminder = reminderDao.getReminderById(id)
       return if (selectedReminder == null)
                Result.Error("Reminder not found!")
            else
                Result.Success(selectedReminder)


    }

    override suspend fun deleteAllReminders()  {
        reminderDao.deleteAllReminders()
    }


}