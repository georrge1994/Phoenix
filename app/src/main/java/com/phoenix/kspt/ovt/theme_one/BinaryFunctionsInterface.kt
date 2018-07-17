package com.phoenix.kspt.ovt.theme_one

import android.content.Context
import com.phoenix.kspt.ovt.models.CustomNumber
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by darkt on 3/26/2018.
 */

interface BinaryFunctionsInterface {
    /**
     *  allowed operation
     * */
    enum class OperationsCode { A_PLUS_B, A_MINUS_B_ADDITIONAL, A_MINUS_B_INVERSION, B_MINUS_A_ADDITIONAL, B_MINUS_A_INVERSION }

    /**
     * Method for conversation a string with binary number to bitset format
     * */
    fun getNumberFromString(numberString: String): BitSet

    /**
     * Returned the object of number
     * @numberString - binary number with sign (exp. 0.000000.000)
     * @comment - comment for number
     */
    fun getCustomNumberFromString(numberString: String, comment: String): CustomNumber

    /**
     * Return a direct binary code.
     * @number - decimal number (exp. 15.1)
     * @letter - letter used for creating comment
     */
    fun getDirectCode(number: Double, letter: String, context: Context): CustomNumber

    /**
     * Return a inversion binary code.
     * @number - direct binary code
     * @letter - letter used for creating comment
     */
    fun getInversionCode(number: CustomNumber, letter: String, context: Context): CustomNumber

    /**
     * Return additional code.
     * @number - direct binary code
     * @letter - letter used for creating comment
     */
    fun getAdditionalCode(number: CustomNumber, letter: String, context: Context): CustomNumber

    /**
     * Return inversion customNumber from additional Bitset code (user in theme # 1, task # 6 and #7)
     * @number - direct binary code
     */
    fun getInversionCodeFromAdditional(numberSet: BitSet): CustomNumber

    /**
     * Number in string format is good validation
     * @numberString - number in string format
     */
    fun isValid(numberString: String): Boolean

    /**
     * Return string number from bitset
     */
    fun getNumberInString(customNumber: CustomNumber): String

    /**
     * Return a full stack operations.
     * @a - number # 1
     * @b - number # 2
     * @operationCode - one of the operations
     */
    fun getOperationsStack(a: Double, b: Double, operationCode: OperationsCode, context: Context): ArrayList<CustomNumber>

    /********************************************* back-end functions with clear names ********************************/
    /**
     * Return a correct answers for: summary in direction codes
     * @a_direct - number # 1
     * @b_direct - number # 2
     * @ArrayList<CustomNumber> - stack of the correct steps
     */
    fun number1_plus_number2(a_direct: CustomNumber, b_direct: CustomNumber): ArrayList<CustomNumber>

    /**
     * Return a correct answers for: summary in inversion codes
     * @a_direct - number # 1
     * @b_direct - number # 2
     * @ArrayList<CustomNumber> - stack of the correct steps
     */
    fun number1_minus_number2_inversion(number1: CustomNumber, number2: CustomNumber): ArrayList<CustomNumber>

    /**
     * Return a correct answers for: summary in additional codes
     * @a_direct - number # 1
     * @b_direct - number # 2
     * @ArrayList<CustomNumber> - stack of the correct steps
     */
    fun number1_minus_number2_additional(number1: CustomNumber, number2: CustomNumber): ArrayList<CustomNumber>

    /**
     * Summation numbers without saving bit of the overflow
     */
    fun sum_without_save_overflow_bit(number1: BitSet, number2: BitSet): BitSet

    /**
     * Summation numbers with saving bit of the overflow
     */
    fun sum_with_save_overflow_and_sign_bits(numberSet1: BitSet, numberSet2: BitSet): BitSet

    /**
     * Summation bits of numbers in row
     */
    fun sum_over_all_bits(numberSet1: BitSet, numberSet2: BitSet): BitSet
}