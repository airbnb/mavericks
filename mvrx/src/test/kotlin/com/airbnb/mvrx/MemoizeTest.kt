package com.airbnb.mvrx

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.EmptyDisposable
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MemoizeTest {
    class NonDataClass(val text: String) : Disposable {
        private var isDisposed = false

        override fun isDisposed() = isDisposed
        override fun dispose() {
            isDisposed = true
        }
    }

    val factory: (String) -> Pair<NonDataClass, Disposable> = { key ->
        val ret = NonDataClass(key)
        ret to ret
    }

    lateinit var memoize: Memoize<NonDataClass>


    @Before
    fun setup() {
        memoize = Memoize<NonDataClass>(factory)
    }

    @Test
    fun testGetsFirstObject() {
        val firstFoo = memoize.get("foo")
        val secondFoo = memoize.get("foo")
        assertEquals("foo", firstFoo.text)
        assertEquals(System.identityHashCode(firstFoo), System.identityHashCode(secondFoo))
    }

    @Test
    fun testNewObjectReplacesFirst() {
        memoize.get("foo")
        val bar = memoize.get("bar")
        assertEquals("bar", bar.text)
    }

    @Test
    fun testDoesntCacheOriginal() {
        val firstFoo = memoize.get("foo")
        memoize.get("bar")
        val secondFoo = memoize.get("foo")
        assertNotEquals(firstFoo, secondFoo)
    }

    @Test
    fun testNewObjectDisposesFirst() {
        val foo = memoize.get("foo")
        val bar = memoize.get("bar")
        assertTrue(foo.isDisposed)
        assertFalse(bar.isDisposed)
    }
}