package com.airbnb.mvrx.todomvrx

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import com.airbnb.mvrx.withState
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

class TasksViewModelTest {

    private val dataSource: TasksDataSource = mock()

    private lateinit var viewModel: TasksViewModel
    private lateinit var tasks: List<Task>

    @Before
    fun setupTasksList() {
        tasks = listOf(task1, task2, task3)
    }

    @Test
    fun refreshTasks_success() {
        // use subject to be able to verify the loading state before any emissions
        val tasksSubject = SingleSubject.create<List<Task>>()
        whenever(dataSource.getTasks()).thenReturn(tasksSubject)

        // given the viewmodel with default state
        viewModel = TasksViewModel(TasksState(), listOf(dataSource))

        // verify that tasks were requested from the data source
        verify(dataSource).getTasks()
        // verify that loading state has changed to true upon subscription
        withState(viewModel) { assertEquals(it.isLoading, true) }

        // new emission from the data source happened
        tasksSubject.onSuccess(tasks)

        // verify that tasks request was successful and the tasks list is present
        withState(viewModel) {
            assertTrue(it.taskRequest is Success)
            assertEquals(it.tasks, tasks)
            assertNull(it.lastEditedTask)
        }

        // verify that loading state has changed to false after the stream is completed
        withState(viewModel) { assertEquals(it.isLoading, false) }
    }

    @Test
    fun refreshTasks_failure() {
        whenever(dataSource.getTasks()).thenReturn(Single.error(Exception("Server not found")))

        // given the viewmodel with default state
        viewModel = TasksViewModel(TasksState(), listOf(dataSource))

        // verify that tasks were requested from the data source
        verify(dataSource).getTasks()

        // verify the tasks request failed and the tasks list is empty
        withState(viewModel) {
            assertTrue(it.taskRequest is Fail)
            assertEquals(it.tasks, emptyList<Task>())
        }
    }

    @Test
    fun upsertTask_insert() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.size, 3)
            assertNull(it.lastEditedTask)
        }

        // insert a new task
        viewModel.upsertTask(task4)

        // verify the last edited task is the new one and the tasks list is updated
        withState(viewModel) {
            assertEquals(it.tasks.size, 4)
            assertEquals(it.tasks.last(), task4)
            assertEquals(it.lastEditedTask, task4.id)
        }

        // verify a new task has inserted to the data source
        verify(dataSource).upsertTask(task4)
    }

    @Test
    fun upsertTask_update() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.size, 3)
            assertNull(it.lastEditedTask)
        }

        // update existing task
        val updatedTask = task2.copy(title = "title2_changed")
        viewModel.upsertTask(updatedTask)

        // verify the last edited task is the updated one and the tasks list is updated
        withState(viewModel) {
            assertEquals(it.tasks.size, 3)
            assertEquals(it.tasks.find { task -> task.id == updatedTask.id }!!.title, updatedTask.title)
            assertEquals(it.lastEditedTask, updatedTask.id)
        }

        // verify the existing task has updated in the data source
        verify(dataSource).upsertTask(updatedTask)
    }

    @Test
    fun setComplete() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.count { task -> task.complete }, 2)
        }

        // set complete for id which is not yet completed
        viewModel.setComplete("task_1", true)

        // verify the last edited task is the completed one and the tasks list is updated
        withState(viewModel) {
            assertTrue(it.tasks.find { task -> task.id == task1.id }!!.complete)
            assertEquals(it.tasks.count { task -> task.complete }, 3)
            assertEquals(it.lastEditedTask, task1.id)
        }

        // verify the task was set to complete on the data source
        verify(dataSource).setComplete("task_1", true)
    }

    @Test
    fun setComplete_noTaskInList() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.count { task -> task.complete }, 2)
        }

        // set complete for id which is not presented in tasks list
        viewModel.setComplete("task_5", true)

        // verify state stays the same
        withState(viewModel) {
            assertEquals(it.tasks.count { task -> task.complete }, 2)
            assertNull(it.lastEditedTask)
        }

        // verify the task was set to complete on the data source
        verify(dataSource).setComplete("task_5", true)
    }

    @Test
    fun setComplete_taskIsAlreadyCompleted() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.count { task -> task.complete }, 2)
        }

        // set complete for id which is already completed
        viewModel.setComplete("task_2", true)

        // verify state stays the same
        withState(viewModel) {
            assertEquals(it.tasks.count { task -> task.complete }, 2)
            assertNull(it.lastEditedTask)
        }

        // verify the task was set to complete on the data source
        verify(dataSource).setComplete("task_2", true)
    }

    @Test
    fun clearCompletedTasks() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.size, 3)
            assertEquals(it.tasks.count { task -> task.complete }, 2)
        }

        // clear completed tasks
        viewModel.clearCompletedTasks()

        // verify the last edited task is null and the tasks list does not contain completed tasks
        withState(viewModel) {
            assertEquals(it.tasks.size, 1)
            assertEquals(it.tasks.count { task -> task.complete }, 0)
            assertNull(it.lastEditedTask)
        }

        // verify the data source has cleared completed tasks
        verify(dataSource, atLeastOnce()).clearCompletedTasks()
    }

    @Test
    fun deleteTask() {
        // make `getTasks` emit nothing to use tasks from the initial state
        whenever(dataSource.getTasks()).thenReturn(Single.never())

        // given the viewmodel with some tasks
        viewModel = TasksViewModel(TasksState(tasks = tasks), listOf(dataSource))

        // verify the initial state
        withState(viewModel) {
            assertEquals(it.tasks.size, 3)
            assertNull(it.lastEditedTask)
        }

        // delete a task
        viewModel.deleteTask(task2.id)

        // verify the last edited task is the deleted one and the tasks list is updated
        withState(viewModel) {
            assertEquals(it.tasks.size, 2)
            assertFalse(task2 in it.tasks)
            assertEquals(it.lastEditedTask, task2.id)
        }

        // verify a task has deleted in the data source by the given id
        verify(dataSource).deleteTask(task2.id)
    }

    companion object {
        private val task1 = Task(title = "title1", description = "description1", id = "task_1")
        private val task2 = Task(title = "title2", description = "description2", id = "task_2", complete = true)
        private val task3 = Task(title = "title3", description = "description3", id = "task_3", complete = true)
        private val task4 = Task(title = "title4", description = "description4", id = "task_4")

        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule()
    }
}