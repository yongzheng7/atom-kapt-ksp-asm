package com.atom.compiler.test.core.test

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.atom.compiler.test.core.TeeOutputStream
import okio.Buffer
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.OutputStream
import java.io.PrintStream

@RunWith(JUnit4::class)
class StreamUtilTests {

    @Test
    fun `TeeOutputStream prints to all streams`() {
        val buf1 = Buffer()
        val buf2 = Buffer()

        val s = "test test \ntest\n"

        PrintStream(
            TeeOutputStream(
                PrintStream(buf1.outputStream()),
                buf2.outputStream()
            )
        ).print(s)
        print("1 ->"+buf1.readUtf8())
        print("2 ->"+buf2.readUtf8())
        print("3")
    }

    @Test
    fun `TeeOutPutStream flushes all streams`() {
        val str1 = mock<OutputStream>()
        val str2 = mock<OutputStream>()
        println("str1 = ${str1} , str2 = ${str2}")
        TeeOutputStream(str1, str2).flush()

        verify(str1).flush()
        verify(str2).flush()
    }

    @Test
    fun `TeeOutPutStream closes all streams`() {
        val str1 = mock<OutputStream>()
        val str2 = mock<OutputStream>()

        TeeOutputStream(str1, str2).close()

        verify(str1).close()
        verify(str2).close()
    }
}