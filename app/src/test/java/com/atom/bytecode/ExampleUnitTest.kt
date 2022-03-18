package com.atom.bytecode

import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val array1 = arrayOf(1, 2, 3, 4, 5).toIntArray()
        val array2 = arrayOf(5, 6, 7, 8, 9).toIntArray()
        var targetLenght = (array1.size + array2.size) / 2
        val index = targetLenght / 2
        println(" result = ${findIndexMixValue(array1, array2, 9)}")
    }

    fun findIndexMixValue(array1: IntArray, array2: IntArray, index: Int): Int {
        if (array1.isEmpty()) {
            return array2[index]
        } else if (array2.isEmpty()) {
            return array1[index]
        } else if (index == 1) {
            return Math.min(array1[0], array2[0])
        }
        val array_1_size = array1.size
        val array_2_size = array2.size
        val remove_size = Math.min(Math.min(index / 2, array_1_size - 1), array_2_size - 1)
        val resultIndex = index - remove_size -1
        val resultArray: IntArray
        if (array1[remove_size] < array2[remove_size]) {
            resultArray = array1.copyOfRange(remove_size + 1, array_1_size)
            return findIndexMixValue(resultArray, array2, resultIndex)
        } else {
            resultArray = array2.copyOfRange(remove_size + 1, array_2_size)
            return findIndexMixValue(array1, resultArray, resultIndex)
        }
    }
}