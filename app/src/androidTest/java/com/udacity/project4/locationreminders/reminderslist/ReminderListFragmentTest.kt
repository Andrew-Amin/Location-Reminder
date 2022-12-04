package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {


    lateinit var viewModel: RemindersListViewModel
    private lateinit var repo: RemindersLocalRepository
    private lateinit var remindersDao: RemindersDao

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun initViewModel() = mainCoroutineRule.runBlockingTest {

        remindersDao = LocalDB.createRemindersDao(getApplicationContext())

        remindersDao.deleteAllReminders()
        repo = RemindersLocalRepository(
            remindersDao,
            Dispatchers.Main
        )
        viewModel = RemindersListViewModel(getApplicationContext(), repo)
    }

    @Test
    fun addReminderFAB_navigateToAddReminder() {
        //given - on ReminderActivity
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //when - click on the addReminderFAB (+) button
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //then - verify that we navigate to the add reminder screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun isListViewVisible_onFragmentLaunch() {
        // GIVEN - On ReminderListFragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView)).check(
            matches(isDisplayed())
        )

    }

//    @Test
//    fun openTheActivityShowLoadingIndicator() {
//        // GIVEN - On ReminderListFragment
//        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        onView(withId(R.id.progressBar)).check(
//            matches(isDisplayed())
//        )
//    }


    @Test
    fun addNewReminder_addNewRecycleViewEntryWithTheSameData() = mainCoroutineRule.runBlockingTest {
        //given - add new reminder to the datasource

        val newReminder =
            ReminderDTO("test1", "testDescription1", "testLocation1", 0.0, 0.0, "id1")
        repo.saveReminder(newReminder)

        //when - call _viewModel.loadReminders() on ReminderListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            viewModel.loadReminders()
        }

        //then - a recycleView entry should be added with the recently saved reminder

        onView(withId(R.id.reminderssRecyclerView)).check(
            matches(hasDescendant(withText("test1")))
        )
        onView(withId(R.id.reminderssRecyclerView)).check(
            matches(hasDescendant(withText("testLocation1")))
        )
        onView(withId(R.id.reminderssRecyclerView)).check(
            matches(hasDescendant(withText("testDescription1")))
        )
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("test1")))
        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.location)).check(matches(withText("testLocation1")))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText("testDescription1")))
    }

    @Test
    fun showNoDataImage_withEmptyList() {
        //given - empty reminders list
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //when - call _viewModel.loadReminders() on ReminderListFragment
        scenario.onFragment {
            viewModel.loadReminders()
        }

        //then - the noDataTextView should be displayed
        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
    }

}