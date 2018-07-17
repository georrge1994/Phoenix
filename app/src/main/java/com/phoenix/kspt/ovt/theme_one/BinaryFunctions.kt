package com.phoenix.kspt.ovt.theme_one

import android.content.Context
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.theme_one.BinaryFunctionsInterface.OperationsCode
import com.phoenix.kspt.utils.FROM_ADDITIONAL_TO_INVERSION
import com.phoenix.kspt.utils.FROM_DIRECT_TO_DECIMAL
import com.phoenix.kspt.utils.FROM_INVERSION_TO_DIRECT
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by darkt on 3/26/2018.
 */
class BinaryFunctions {
    companion object : BinaryFunctionsInterface {
        const val BITSET_OVERFLOW_BIT = 10
        const val BITSET_SIGN_BIT = 9
        private const val BITSET_INTEGER_BITS_BEGIN = 8
        private const val BITSET_INTEGER_BITS_END = 3
        private const val BITSET_FLOAT_BITS_BEGIN = 2
        private const val BITSET_FLOAT_BITS_END = 0
        private val CYCLIC_TRANSFER: BitSet = getNumberFromString("0.000000.001")
        private val MINUS_ONE: BitSet = getNumberFromString("0.111111.111")

        /*** return BitSet ***/
        override fun getNumberFromString(numberString: String): BitSet {
            if (isNotValid(numberString))
                return BitSet()

            val clearNumber = preparationString(numberString)
            val numberSet = BitSet()

            // sign
            if (clearNumber.length > BITSET_SIGN_BIT)
                numberSet[BITSET_SIGN_BIT] = clearNumber[BITSET_SIGN_BIT] == '1'

            // integer
            if (clearNumber.length > BITSET_INTEGER_BITS_END)
                for (i in BITSET_INTEGER_BITS_END..BITSET_INTEGER_BITS_BEGIN)
                    numberSet[i] = clearNumber[i] == '1'

            // float
            if (clearNumber.length > BITSET_FLOAT_BITS_END)
                for (i in BITSET_FLOAT_BITS_END..BITSET_FLOAT_BITS_BEGIN)
                    numberSet[i] = clearNumber[i] == '1'

            return numberSet
        }


        /**** return CustomNumber ***/
        override fun getCustomNumberFromString(numberString: String, comment: String): CustomNumber {
            val customNumber = CustomNumber()
            customNumber.numberString = numberString
            customNumber.comment = comment
            if (isValid(numberString))
                customNumber.numberSet = getNumberFromString(numberString)

            return customNumber
        }


        override fun getDirectCode(number: Double, letter: String, context: Context): CustomNumber {
            val numberBitSet = BitSet()

            // sign
            if (number < 0)
                numberBitSet[BITSET_SIGN_BIT] = true

            // int
            var i = BITSET_INTEGER_BITS_BEGIN
            var integer = Math.abs(number.toInt())
            var mask = 32
            do {
                numberBitSet[i] = mask <= integer
                if (mask <= integer)
                    integer -= mask

                mask /= 2
                i--
            } while (integer > 0)

            // float
            i = BITSET_FLOAT_BITS_BEGIN
            var float = Math.abs(number - number.toInt())
            do {
                float *= 2
                if (float >= 1) {
                    float -= 1
                    numberBitSet[i--] = true
                } else {
                    numberBitSet[i--] = false
                }

            } while (float != 0.0)

            val customNumber = CustomNumber(numberBitSet)
            customNumber.comment = letter + " " + context.getString(R.string.direct)

            return customNumber
        }

        override fun getInversionCode(number: CustomNumber, letter: String, context: Context): CustomNumber {
            val clone = number.clone()

            if (clone.isNegative()) {
                val numberCloneSet: BitSet = clone.numberSet
                numberCloneSet.flip(0, BITSET_SIGN_BIT)
                clone.numberSet = numberCloneSet
            }

            clone.comment = letter + " " + context.getString(com.phoenix.kspt.R.string.inversion)
            return clone
        }

        override fun getAdditionalCode(number: CustomNumber, letter: String, context: Context): CustomNumber {
            val clone = number.clone()
            clone.comment = letter + " " + context.getString(R.string.additional)

            return if (clone.isNegative()) {
                val inversion = getInversionCode(clone, letter, context)
                val resultSet = sum_without_save_overflow_bit(inversion.numberSet, CYCLIC_TRANSFER)
                val customNumber = CustomNumber(resultSet)
                customNumber.comment = letter + " " + context.getString(R.string.additional)
                customNumber

            } else
                clone
        }

        override fun getInversionCodeFromAdditional(numberSet: BitSet): CustomNumber {
            return CustomNumber(sum_without_save_overflow_bit(numberSet, MINUS_ONE))
        }

        /*** other ***/
        override fun isValid(numberString: String): Boolean {
            return !isNotValid(numberString)
        }

        fun isNotValid(numberString: String): Boolean {
            val parts = numberString.split('.')
            return parts.size != 3 ||           // overflow/sign + integer + float
                    (parts[0].length != 2 && parts[0].length != 1) ||     // overflow bit + sign bit
                    parts[1].length != 6 ||     // 6 bits in integer part
                    parts[2].length != 3        // 3 bits in float part
        }

        override fun getNumberInString(customNumber: CustomNumber): String {
            val numberSet = customNumber.numberSet

            if (customNumber.isEmpty())
                return ""

            var numberString = ""

            for (i in BITSET_SIGN_BIT downTo BITSET_FLOAT_BITS_END) {
                numberString += if (numberSet[i])
                    "1"
                else
                    "0"

                if (i == BITSET_SIGN_BIT || i == BITSET_INTEGER_BITS_END)
                    numberString += "."
            }

            return numberString
        }

        private fun preparationString(inputString: String): String {
            val str = inputString.replace(".", "")

            var stringBuilder = StringBuilder(str)
            stringBuilder = stringBuilder.reverse()

            return stringBuilder.toString()
        }

        override fun getOperationsStack(a: Double, b: Double, operationCode: OperationsCode, context: Context): ArrayList<CustomNumber> {

            val directA = getDirectCode(a, context.getString(R.string.letter_a), context)
            val directMinusA = getDirectCode(-a, context.getString(R.string.letter_a), context)
            val inversionMinusA = getInversionCode(directMinusA, context.getString(R.string.letter_a), context)
            val additionalMinusA = getAdditionalCode(directMinusA, context.getString(R.string.letter_a), context)

            val directB = getDirectCode(b, context.getString(R.string.letter_b), context)
            val directMinusB = getDirectCode(-b, context.getString(R.string.letter_b), context)
            val inversionMinusB = getInversionCode(directMinusB, context.getString(R.string.letter_b), context)
            val additionalMinusB = getAdditionalCode(directMinusB, context.getString(R.string.letter_b), context)

            val stack = when (operationCode) {
                OperationsCode.A_PLUS_B -> { number1_plus_number2(directA, directB) }
                OperationsCode.A_MINUS_B_ADDITIONAL -> number1_minus_number2_additional(directA, additionalMinusB)
                OperationsCode.A_MINUS_B_INVERSION -> number1_minus_number2_inversion(directA, inversionMinusB)
                OperationsCode.B_MINUS_A_ADDITIONAL -> number1_minus_number2_additional(directB, additionalMinusA)
                OperationsCode.B_MINUS_A_INVERSION -> number1_minus_number2_inversion(directB, inversionMinusA)
            }

            // added decimal string
            val sum = when (operationCode) {
                OperationsCode.A_PLUS_B -> a + b
                OperationsCode.A_MINUS_B_ADDITIONAL, OperationsCode.A_MINUS_B_INVERSION -> a - b
                OperationsCode.B_MINUS_A_ADDITIONAL, OperationsCode.B_MINUS_A_INVERSION -> b - a
            }
            stack.add(CustomNumber(sum.toString(), FROM_DIRECT_TO_DECIMAL, context.getString(R.string.decimal)))

            // reset overflow bits
            for (item in stack)
                item.numberSet[BITSET_OVERFLOW_BIT] = false

            return stack
        }


        /********************************************* back-end functions with clear names ********************************/

        override fun number1_plus_number2(a_direct: CustomNumber, b_direct: CustomNumber): ArrayList<CustomNumber> {
            val operationsStack = ArrayList<CustomNumber>()

            operationsStack.add(a_direct)                              // A direct
            operationsStack.add(b_direct)                              // B direct
            // s = a + b (only if a and b have equal signs)
            val s = sum_over_all_bits(a_direct.numberSet, b_direct.numberSet)
            operationsStack.add(CustomNumber(s))                        // S direct

            return operationsStack
        }

        override fun number1_minus_number2_inversion(number1: CustomNumber, number2: CustomNumber): ArrayList<CustomNumber> {
            val operationsStack = ArrayList<CustomNumber>()

            operationsStack.add(number1)
            operationsStack.add(number2)

            val sum1 = sum_over_all_bits(number1.numberSet, number2.numberSet)
            operationsStack.add(CustomNumber(sum1))

            if (sum1[BITSET_OVERFLOW_BIT]) {                         // + 0.0000000.001
                operationsStack.add(CustomNumber(CYCLIC_TRANSFER))

                var result = sum1.clone() as BitSet
                result[BITSET_OVERFLOW_BIT] = false                 // reset overflow-flag
                result = sum_with_save_overflow_and_sign_bits(result, CYCLIC_TRANSFER)
                operationsStack.add(CustomNumber(result))
            }

            if (sum1[BITSET_SIGN_BIT]) {
                val result = sum1.clone() as BitSet
                result.flip(0, BITSET_SIGN_BIT)                     // from inversion to direct
                val customNumber = CustomNumber(result)
                customNumber.typeOperation = FROM_INVERSION_TO_DIRECT
                operationsStack.add(customNumber)
            }

            return operationsStack
        }

        override fun number1_minus_number2_additional(number1: CustomNumber, number2: CustomNumber): ArrayList<CustomNumber> {
            val operationsStack = ArrayList<CustomNumber>()

            operationsStack.add(number1)
            operationsStack.add(number2)

            val tempResult = sum_over_all_bits(number1.numberSet, number2.numberSet)
            operationsStack.add(CustomNumber(tempResult))

            // if number1 >= number2
            if (tempResult[BITSET_SIGN_BIT]) {
                val inversionCode = getInversionCodeFromAdditional(tempResult)
                inversionCode.typeOperation = FROM_ADDITIONAL_TO_INVERSION
                operationsStack.add(inversionCode)                                      // inversion code

                val directCode = inversionCode.numberSet.clone() as BitSet
                directCode.flip(0, BITSET_SIGN_BIT)
                val customNumber = CustomNumber(directCode)
                customNumber.typeOperation = FROM_INVERSION_TO_DIRECT
                operationsStack.add(customNumber)                                       // direct code
            }

            return operationsStack
        }

        override fun sum_without_save_overflow_bit(number1: BitSet, number2: BitSet): BitSet {
            val resultBitset = sum_with_save_overflow_and_sign_bits(number1, number2)
            resultBitset[BITSET_OVERFLOW_BIT] = false
            return resultBitset
        }

        override fun sum_with_save_overflow_and_sign_bits(numberSet1: BitSet, numberSet2: BitSet): BitSet {

            val sum = BitSet()
            var overflow = false

            for (i in 0 until BITSET_SIGN_BIT) {
                var counter = 0

                if (numberSet1[i])
                    counter++

                if (numberSet2[i])
                    counter++

                if (overflow)
                    counter++


                when (counter) {
                    0 -> {
                        sum[i] = false
                        overflow = false
                    }
                    1 -> {
                        sum[i] = true
                        overflow = false
                    }
                    2 -> {
                        sum[i] = false
                        overflow = true
                    }
                    3 -> {
                        sum[i] = true
                        overflow = true
                    }
                }
            }

            sum[BITSET_SIGN_BIT] = numberSet1[BITSET_SIGN_BIT]
            sum[BITSET_OVERFLOW_BIT] = overflow
            return sum
        }

        override fun sum_over_all_bits(numberSet1: BitSet, numberSet2: BitSet): BitSet {
            val sum = BitSet()
            var overflow = false

            for (i in 0..BITSET_OVERFLOW_BIT) {
                var counter = 0

                if (numberSet1[i])
                    counter++

                if (numberSet2[i])
                    counter++

                if (overflow)
                    counter++


                when (counter) {
                    0 -> {
                        sum[i] = false
                        overflow = false
                    }
                    1 -> {
                        sum[i] = true
                        overflow = false
                    }
                    2 -> {
                        sum[i] = false
                        overflow = true
                    }
                    3 -> {
                        sum[i] = true
                        overflow = true
                    }
                }
            }

            return sum
        }

    }
}