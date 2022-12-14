package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
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

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun addNewReminder_withValidData_addReminderToListAndShowSuccessToast() {
        // given - Start up RemindersActivity screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click on the add reminder to navigate to add reminder screen
        onView(withId(R.id.addReminderFAB)).perform(click())

        // set reminder title and description
        onView(withId(R.id.reminderTitle)).perform(
            replaceText("testTitle1")
        )
        onView(withId(R.id.reminderDescription)).perform(
            replaceText("testDescription1")
        )

        // click on the selectLocation textView to navigate to select location screen
        onView(withId(R.id.selectLocation)).perform(click())

        // select a random location
        onView(withId(R.id.map_fragment)).perform(click())

        // clinic the save button
        onView(withId(R.id.saveLocation_btn)).perform(click())

        // on the saveReminder Screen click on the save button
        onView(withId(R.id.saveReminder)).perform(click())


        //then - a recycleView entry should be added with the recently saved reminder
        onView(withId(R.id.reminderssRecyclerView)).check(
            matches(hasDescendant(withText("testTitle1")))
        )

        onView(withId(R.id.reminderssRecyclerView)).check(
            matches(hasDescendant(withText("testDescription1")))
        )
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("testTitle1")))
        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText("testDescription1")))

        // that reminder saved toast message should be shown to the user
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(not(getActivity(getApplicationContext())?.window?.decorView))
        ).check(
            matches(isDisplayed())
        )
        activityScenario.close()
    }

    @Test
    fun addNewReminder_withInvalidData_showSnackBar() {
        // given - Start up RemindersActivity screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click on the add reminder to navigate to add reminder screen
        onView(withId(R.id.addReminderFAB)).perform(click())

        // set reminder title and description

        onView(withId(R.id.reminderDescription)).perform(
            replaceText("testDescription1")
        )

        // click on the selectLocation textView to navigate to select location screen
        onView(withId(R.id.selectLocation)).perform(click())

        // select a random location
        onView(withId(R.id.map_fragment)).perform(click())

        // clinic the save button
        onView(withId(R.id.saveLocation_btn)).perform(click())

        // on the saveReminder Screen click on the save button
        onView(withId(R.id.saveReminder)).perform(click())


        //then - the snackBar should be displayed with err_enter_title string
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
        activityScenario.close()
    }


}
