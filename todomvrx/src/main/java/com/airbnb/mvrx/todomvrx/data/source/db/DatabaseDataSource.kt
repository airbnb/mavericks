package com.airbnb.mvrx.todomvrx.data.source.db

import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import com.airbnb.mvrx.todomvrx.util.EspressoIdlingResource
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DatabaseDataSource(
        private val dao: TasksDao,
        private val delayMs: Long = 2000,
        private val scheduler: Scheduler = Schedulers.io()
) : TasksDataSource {
    override fun getTasks(): Single<Tasks> = dao.getTasks()
            .subscribeOn(scheduler)
            .delay(delayMs, TimeUnit.MILLISECONDS, scheduler)

    override fun upsertTask(task: Task): Disposable = fromAction { dao.saveTask(task) }

    override fun setComplete(id: String, complete: Boolean): Disposable = fromAction { dao.setComplete(id, complete) }

    override fun clearCompletedTasks(): Disposable = fromAction { dao.clearCompletedTasks() }

    override fun deleteTask(id: String): Disposable = fromAction { dao.deleteTask(id) }

    private fun fromAction(action: () -> Unit): Disposable = Completable.fromAction(action)
            .subscribeOn(scheduler)
            .delay(delayMs, TimeUnit.MILLISECONDS)
            .doOnSubscribe { EspressoIdlingResource.increment() }
            .doOnComplete { EspressoIdlingResource.decrement() }
            .subscribe()
}
