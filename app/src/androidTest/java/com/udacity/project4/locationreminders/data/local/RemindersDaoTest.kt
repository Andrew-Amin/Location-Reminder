package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.MainCoroutineRule
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context?>()!!.applicationContext,
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders() = runBlockingTest {

        //given a nonempty database
        val reminderTodo1 =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val reminderTodo2 =
            ReminderDTO("test2", "testDescription2", "testLocation2", 0.0, 0.0, "id2")
        val reminderTodo3 =
            ReminderDTO("test3", "testDescription3", "testLocation3", 0.0, 0.0, "id3")
        database.reminderDao().saveReminder(reminderTodo1)
        database.reminderDao().saveReminder(reminderTodo2)
        database.reminderDao().saveReminder(reminderTodo3)

        //when call getReminders()
        val loadedList = database.reminderDao().getReminders()

        // then we get a list of the same
        assertThat(loadedList, notNullValue())
        assertThat(loadedList.size, `is`(3))

        val reminderTodo = database.reminderDao().getReminderById("id2")

        assertThat(reminderTodo as ReminderDTO, notNullValue())
        assertThat(reminderTodo.title, `is`(reminderTodo2.title))
        assertThat(reminderTodo.description, `is`(reminderTodo2.description))
        assertThat(reminderTodo.location, `is`(reminderTodo2.location))
        assertThat(reminderTodo.latitude, `is`(reminderTodo2.latitude))
        assertThat(reminderTodo.longitude, `is`(reminderTodo2.longitude))
    }


    @Test
    fun saveReminder_andGetItByItsId() = runBlockingTest {
        //given a new reminder to save into the database
        val id = "id1"
        val reminderTodo =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, id)
        database.reminderDao().saveReminder(reminderTodo)

        // when load it by id from the database
        val loadedData = database.reminderDao().getReminderById(id)

        // then we get the same data
        assert(loadedData != null)
        loadedData?.let {
            assertThat(loadedData.title, `is`(reminderTodo.title))
            assertThat(loadedData.description, `is`(reminderTodo.description))
            assertThat(loadedData.location, `is`(reminderTodo.location))
            assertThat(loadedData.latitude, `is`(reminderTodo.latitude))
            assertThat(loadedData.longitude, `is`(reminderTodo.longitude))
        }
    }

    @Test
    fun getReminderByIdThatDoesNotExist_returnNull() = runBlockingTest {
        //given a new reminder to save into the database
        val reminderTodo =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        database.reminderDao().saveReminder(reminderTodo)

        // when load it by id from the database
        val loadedData = database.reminderDao().getReminderById("id")

        // then we get the same data
        assert(loadedData == null)
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        //given a nonempty database
        val reminderTodo1 =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val reminderTodo2 =
            ReminderDTO("test2", "testDescription2", "testLocation2", 0.0, 0.0, "id2")
        val reminderTodo3 =
            ReminderDTO("test3", "testDescription3", "testLocation3", 0.0, 0.0, "id3")
        database.reminderDao().saveReminder(reminderTodo1)
        database.reminderDao().saveReminder(reminderTodo2)
        database.reminderDao().saveReminder(reminderTodo3)

        database.reminderDao().deleteAllReminders()

        //when call getReminders()
        val loadedList = database.reminderDao().getReminders()

        // then we get an empty list
        assertThat(loadedList, notNullValue())
        assertThat(loadedList.size, `is`(0))
    }


}