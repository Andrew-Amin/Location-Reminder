package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var repo: RemindersLocalRepository
    private lateinit var database: RemindersDatabase


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()

        repo = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() = database.close()

    @Test
    fun getReminders() = mainCoroutineRule.runBlockingTest {
        //given a nonempty database
        val reminderTodo1 =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val reminderTodo2 =
            ReminderDTO("test2", "testDescription2", "testLocation2", 0.0, 0.0, "id2")

        database.reminderDao().saveReminder(reminderTodo1)
        database.reminderDao().saveReminder(reminderTodo2)

        //when call getReminders()
        val result = repo.getReminders() as Result.Success
        val loadedList = result.data

        // then we get a list of the same
        assertThat(loadedList, notNullValue())
        assertThat(loadedList.size, `is`(2))

        val loadedReminderTodo1 = (repo.getReminder("id1") as Result.Success).data

        assertThat(loadedReminderTodo1, notNullValue())
        assertThat(loadedReminderTodo1.title, `is`(reminderTodo1.title))
        assertThat(
            loadedReminderTodo1.description,
            `is`(reminderTodo1.description)
        )
        assertThat(loadedReminderTodo1.location, `is`(reminderTodo1.location))
        assertThat(loadedReminderTodo1.latitude, `is`(reminderTodo1.latitude))
        assertThat(loadedReminderTodo1.longitude, `is`(reminderTodo1.longitude))


        val loadedReminderTodo2 = (repo.getReminder("id2") as Result.Success).data

        assertThat(loadedReminderTodo2, notNullValue())
        assertThat(loadedReminderTodo2.title, `is`(reminderTodo2.title))
        assertThat(
            loadedReminderTodo2.description,
            `is`(reminderTodo2.description)
        )
        assertThat(loadedReminderTodo2.location, `is`(reminderTodo2.location))
        assertThat(loadedReminderTodo2.latitude, `is`(reminderTodo2.latitude))
        assertThat(loadedReminderTodo2.longitude, `is`(reminderTodo2.longitude))


    }

    @Test
    fun saveReminder_getReminder() = mainCoroutineRule.runBlockingTest {
        //given a new reminder to save into the database
        val id = "id1"
        val reminderTodo =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, id)
        repo.saveReminder(reminderTodo)

        // when load it by id from the database
        val loadedData = (repo.getReminder(id) as Result.Success).data


        // then we get the same data
        assertThat(loadedData , notNullValue())
        assertThat(loadedData.title, `is`(reminderTodo.title))
        assertThat(loadedData.description, `is`(reminderTodo.description))
        assertThat(loadedData.location, `is`(reminderTodo.location))
        assertThat(loadedData.latitude, `is`(reminderTodo.latitude))
        assertThat(loadedData.longitude, `is`(reminderTodo.longitude))

    }

    @Test
    fun getReminderWithIdThatDoesNotExist_returnNotFound() = runBlockingTest {
        //given a new reminder to save into the database
        val reminderTodo =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        database.reminderDao().saveReminder(reminderTodo)

        // when load it by id from the database
        val loadedData = repo.getReminder("id")

        // then we get the same data
        assert(loadedData is Result.Error)
        assertThat((loadedData as Result.Error).message , `is`("Reminder not found!"))
    }


    @Test
    fun deleteAllReminders() = mainCoroutineRule.runBlockingTest {
        //given a nonempty database
        val reminderTodo1 =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val reminderTodo2 =
            ReminderDTO("test2", "testDescription2", "testLocation2", 0.0, 0.0, "id2")
        val reminderTodo3 =
            ReminderDTO("test3", "testDescription3", "testLocation3", 0.0, 0.0, "id3")
        repo.saveReminder(reminderTodo1)
        repo.saveReminder(reminderTodo2)
        repo.saveReminder(reminderTodo3)

        repo.deleteAllReminders()

        //when call getReminders()
        val loadedList = (repo.getReminders() as Result.Success).data

        // then we get an empty list
        assertThat(loadedList, notNullValue())
        assertThat(loadedList.size, `is`(0))
    }


}