/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airbnb.mvrx.todomvrx.data.source

import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import io.reactivex.Single
import io.reactivex.disposables.Disposable

interface TasksDataSource {
    fun getTasks(): Single<Tasks>

    fun saveTask(task: Task): Disposable

    fun setComplete(id: String, complete: Boolean): Disposable

    fun clearCompletedTasks(): Disposable

    fun deleteTask(id: String): Disposable
}
