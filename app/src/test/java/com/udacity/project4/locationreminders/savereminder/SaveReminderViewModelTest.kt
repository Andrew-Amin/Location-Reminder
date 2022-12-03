package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun onClear_returnsNullLivedata() {
        //when call onClear
        saveReminderViewModel.onClear()

        val reminderTitle = saveReminderViewModel.reminderTitle.getOrAwaitValue()
        val reminderDescription = saveReminderViewModel.reminderDescription.getOrAwaitValue()
        val reminderSelectedLocationStr =
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val selectedPOI = saveReminderViewModel.selectedPOI.getOrAwaitValue()
        val latitude = saveReminderViewModel.latitude.getOrAwaitValue()
        val longitude = saveReminderViewModel.longitude.getOrAwaitValue()

        // then all the viewModel liveDate vars should be null
        assertThat(reminderTitle, nullValue())
        assertThat(reminderDescription, nullValue())
        assertThat(reminderSelectedLocationStr, nullValue())
        assertThat(selectedPOI, nullValue())
        assertThat(latitude, nullValue())
        assertThat(longitude, nullValue())

    }

    @Test
    fun validateEnteredData_returnsTrue_withValidTitleAndLocation() {
        //given a new ReminderDataItem
        val todo = ReminderDataItem("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")

        //when call validateEnteredData() with valid title and location
        val result = saveReminderViewModel.validateEnteredData(todo)

        assertThat(result, `is`(true))
    }

    @Test
    fun validateEnteredData_returnsFalseAndNotNullShowSnackBarInt_withInvalidTitle() {
        //given a new ReminderDataItem
        val todo = ReminderDataItem("", "testDescription1", "testLocation1", 0.0, 0.0, "id1")

        //when call validateEnteredData() with invalid (null or empty) title
        val result = saveReminderViewModel.validateEnteredData(todo)
        val showSnackBarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        //then result must be ture and showSnackBarInt != null
        assertThat(result, `is`(false))
        assertThat(showSnackBarInt, not(nullValue()))
    }

    @Test
    fun validateEnteredData_returnsFalseAndNotNullShowSnackBarInt_withInvalidLocation() {
        //given a new ReminderDataItem
        val todo = ReminderDataItem("test1", "testDescription1", null, 0.0, 0.0, "id1")

        //when call validateEnteredData() with invalid (null or empty) location
        val result = saveReminderViewModel.validateEnteredData(todo)
        val showSnackBarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        //then result must be ture and showSnackBarInt != null
        assertThat(result, `is`(false))
        assertThat(showSnackBarInt, not(nullValue()))
    }

    @Test
    fun saveReminder_showsLoadingIndicator() {
        //given a new ReminderDataItem
        val todo = ReminderDataItem("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")

        //when call saveReminder()
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(todo)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        //then showLoading = false
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun saveReminder_addsToDataSource() = mainCoroutineRule.runBlockingTest {
        //given a new ReminderDataItem and empty data source
        val todo = ReminderDataItem("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        val result = fakeDataSource.getReminders() as Result.Success

        assertThat(result.data.size, IsEqual(0))

        //when call saveReminder()
        saveReminderViewModel.saveReminder(todo)

        //then the data source size must be increased by one
        val resultAfterSaving = fakeDataSource.getReminders() as Result.Success

        assertThat(resultAfterSaving.data.size, IsEqual(1))

    }

    @Test
    fun saveReminder_showsToast() {
        //given a new ReminderDataItem
        val todo = ReminderDataItem("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")

        //when call saveReminder()
        saveReminderViewModel.saveReminder(todo)

        //then showToast = "Reminder Saved !", after saving
        val result = saveReminderViewModel.showToast.getOrAwaitValue()
        assertThat(result, not(nullValue()))
        assertThat(result, `is`("Reminder Saved !"))

    }

    @Test
    fun saveReminder_navigatesBack() {
        //given a new ReminderDataItem
        val todo = ReminderDataItem("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")

        //when call saveReminder()
        saveReminderViewModel.saveReminder(todo)


        //then navigationCommand = NavigationCommand.Back , after saving
        val result = saveReminderViewModel.navigationCommand.getOrAwaitValue()
        assertThat(result, not(nullValue()))
        Assert.assertEquals(NavigationCommand.Back, result)
    }


}