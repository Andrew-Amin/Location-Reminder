package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var remindersListViewModel: RemindersListViewModel


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        FirebaseApp.initializeApp(getApplicationContext()) // to handle [authenticationState] liveData variable which is located inside the [RemindersListViewModel]
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun clearTodosHistory_clearDataSource() {
        //given a nonempty dataSource
        val reminderTodo1 =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val reminderTodo2 =
            ReminderDTO("test2", "testDescription2", "testLocation2", 0.0, 0.0, "id2")

        val fakeDataSource = FakeDataSource()

        fakeDataSource.fakeDb[reminderTodo1.id] = reminderTodo1
        fakeDataSource.fakeDb[reminderTodo2.id] = reminderTodo2

        remindersListViewModel = RemindersListViewModel(getApplicationContext(), fakeDataSource)

        //when call clearTodosHistory()
        remindersListViewModel.clearTodosHistory()

        //then the data source must be empty
        assert(fakeDataSource.fakeDb.isEmpty())

    }

    @Test
    fun loadReminders_showsLoadingIndicator() {

        //given remindersListViewModel with its dataSource

        val fakeDataSource = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(getApplicationContext(), fakeDataSource)

        //when call loadReminders()
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        //then showLoading = false
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminders_loadsDataFromDataSourceToRemindersList() {

        //given a nonempty dataSource & empty remindersList
        val reminderTodo1 =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val reminderTodo2 =
            ReminderDTO("test2", "testDescription2", "testLocation2", 1.0, 1.0, "id2")

        val fakeDataSource = FakeDataSource()

        fakeDataSource.fakeDb[reminderTodo1.id] = reminderTodo1
        fakeDataSource.fakeDb[reminderTodo2.id] = reminderTodo2

        remindersListViewModel = RemindersListViewModel(getApplicationContext(), fakeDataSource)

        //when call loadReminders()
        remindersListViewModel.loadReminders()

        //then the  remindersListViewModel.remindersList is not empty and has the same data
        val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        val firstReminder = remindersList.first()
        assert(remindersList.isNotEmpty())
        assertThat(firstReminder.id, `is`("id1"))
        assertThat(firstReminder.title, `is`("test1"))
        assertThat(firstReminder.description, `is`("testDescription1"))
        assertThat(firstReminder.location, `is`("testLocation1"))
        assertThat(firstReminder.latitude, `is`(0.0))
        assertThat(firstReminder.longitude, `is`(0.0))
    }
}