@file:Suppress("unused")

package utils

import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate")
class Bits() {
    private var bits = mutableListOf(0)

    constructor(value: Int) : this() {
        bits[0] = value
    }

    private fun checkCapacity(length: Int) {
        if (length >= bits.size) bits.add(0)
    }

    /** @param index the index of the bit
     * @return whether the bit is set
     * @throws Exception if index < 0
     */
    operator fun get(index: Int): Boolean {
        val word = index ushr 5
        return if (word >= bits.size) false else bits[word] and (1 shl (index and 0x1F)) != 0
    }

    /** Returns the bit at the given index and clears it in one go.
     * @param index the index of the bit
     * @return whether the bit was set before invocation
     * @throws Exception if index < 0
     */
    fun getAndClear(index: Int): Boolean {
        val word = index ushr 5
        if (word >= bits.size) return false
        val oldBits = bits[word]
        bits[word] = bits[word] and (1 shl (index and 0x1F)).inv()
        return bits[word] != oldBits
    }

    /** Returns the bit at the given index and sets it in one go.
     * @param index the index of the bit
     * @return whether the bit was set before invocation
     * @throws Exception if index < 0
     */
    fun getAndSet(index: Int): Boolean {
        val word = index ushr 5
        checkCapacity(word)
        val oldBits = bits[word]
        bits[word] = bits[word] or (1 shl (index and 0x1F))
        return bits[word] == oldBits
    }

    /** @param index the index of the bit to set
     * @throws Exception if index < 0
     */
    fun set(index: Int) : Bits {
        val word = index ushr 5
        checkCapacity(word)
        bits[word] = bits[word] or (1 shl (index and 0x1F))

        return this
    }

    /** @param index the index of the bit to flip
     */
    fun flip(index: Int) : Bits {
        val word = index ushr 5
        checkCapacity(word)
        bits[word] = bits[word] xor (1 shl (index and 0x1F))

        return this
    }

    /** @param index the index of the bit to clear
     * @throws Exception if index < 0
     */
    fun clear(index: Int) : Bits {
        val word = index ushr 5
        if (word >= bits.size) return this
        bits[word] = bits[word] and (1 shl (index and 0x1F)).inv()
        return this
    }

    /** Clears the entire bitset  */
    fun clear() : Bits {
        bits.clear()
        bits.add(0)
        return this
    }

    /** @return the number of bits currently stored, **not** the highset set bit!
     */
    fun numBits(): Int {
        return bits.size shl 5
    }

    /** Returns the "logical size" of this bitset: the index of the highest set bit in the bitset plus one. Returns zero if the
     * bitset contains no set bits.
     *
     * @return the logical size of this bitset
     */
    fun length(): Int {
        val bits = bits
        for (word in bits.indices.reversed()) {
            val bitsAtWord = bits[word]
            if (bitsAtWord != 0) {
                for (bit in 63 downTo 0) {
                    if (bitsAtWord and (1 shl (bit and 0x1F)) != 0) {
                        return (word shl 5) + bit + 1
                    }
                }
            }
        }
        return 0
    }

    /** @return true if this bitset contains no bits that are set to true
     */
    val isEmpty: Boolean
        get() {
            bits.forEach {
                if(it != 0) return false
            }

            return true
        }
    val isNotEmpty: Boolean
        get() = !isEmpty

    /** Returns the index of the first bit that is set to true that occurs on or after the specified starting index. If no such bit
     * exists then -1 is returned.  */
    fun nextSetBit(fromIndex: Int): Int {
        val bits = bits
        var word = fromIndex ushr 5
        val bitsLength = bits.size
        if (word >= bitsLength) return -1
        var bitsAtWord = bits[word]
        if (bitsAtWord != 0) {
            for (i in (fromIndex and 0x1F)..63) {
                if (bitsAtWord and (1 shl (i and 0x1F)) != 0) {
                    return (word shl 5) + i
                }
            }
        }
        word++
        while (word < bitsLength) {
            if (word != 0) {
                bitsAtWord = bits[word]
                if (bitsAtWord != 0) {
                    for (i in 0..63) {
                        if (bitsAtWord and (1 shl (i and 0x1F)) != 0) {
                            return (word shl 5) + i
                        }
                    }
                }
            }
            word++
        }
        return -1
    }

    /** Returns the index of the first bit that is set to false that occurs on or after the specified starting index.  */
    fun nextClearBit(fromIndex: Int): Int {
        val bits = bits
        var word = fromIndex ushr 5
        val bitsLength = bits.size
        if (word >= bitsLength) return bits.size shl 5
        var bitsAtWord = bits[word]
        for (i in (fromIndex and 0x1F)..63) {
            if (bitsAtWord and (1 shl (i and 0x1F)) == 0) {
                return (word shl 5) + i
            }
        }
        word++
        while (word < bitsLength) {
            if (word == 0) {
                return word shl 5
            }
            bitsAtWord = bits[word]
            for (i in 0..63) {
                if (bitsAtWord and (1 shl (i and 0x1F)) == 0) {
                    return (word shl 5) + i
                }
            }
            word++
        }
        return bits.size shl 5
    }

    /** Performs a logical **AND** of this target bit set with the argument bit set. This bit set is modified so that each bit in
     * it has the value true if and only if it both initially had the value true and the corresponding bit in the bit set argument
     * also had the value true.
     * @param other a bit set
     */
    fun and(other: Bits) : Bits {
        val commonWords: Int = min(bits.size, other.bits.size)
        var i = 0
        while (commonWords > i) {
            bits[i] = bits[i] and other.bits[i]
            i++
        }
        if (bits.size > commonWords) {
            var j = commonWords
            val s = bits.size
            while (s > j) {
                bits[j] = 0
                j++
            }
        }

        return this
    }

    /** Clears all of the bits in this bit set whose corresponding bit is set in the specified bit set.
     *
     * @param other a bit set
     */
    fun andNot(other: Bits) : Bits {
        var i = 0
        val j = bits.size
        val k = other.bits.size
        while (i < j && i < k) {
            bits[i] = bits[i] and other.bits[i].inv()
            i++
        }

        return this
    }

    private fun bitwiseOperation(other: Bits, operation: (index: Int) -> Unit) {
        val commonWords: Int = min(bits.size, other.bits.size)
        var i = 0
        while (commonWords > i) {
            operation(i)
            i++
        }
        if (commonWords < other.bits.size) {
            checkCapacity(other.bits.size)
            var j = commonWords
            val s = other.bits.size
            while (s > j) {
                bits[j] = other.bits[j]
                j++
            }
        }
    }

    /** Performs a logical **OR** of this bit set with the bit set argument. This bit set is modified so that a bit in it has the
     * value true if and only if it either already had the value true or the corresponding bit in the bit set argument has the
     * value true.
     * @param other a bit set
     */
    fun or(other: Bits) : Bits {
        bitwiseOperation(other) { index -> bits[index] = bits[index] or other.bits[index] }

        return this
    }

    /** Performs a logical **XOR** of this bit set with the bit set argument. This bit set is modified so that a bit in it has
     * the value true if and only if one of the following statements holds:
     *
     *  * The bit initially has the value true, and the corresponding bit in the argument has the value false.
     *  * The bit initially has the value false, and the corresponding bit in the argument has the value true.
     *
     * @param other
     */
    fun xor(other: Bits) : Bits {
        bitwiseOperation(other) { index -> bits[index] = bits[index] xor other.bits[index] }

        return this
    }

    /** Returns true if the specified BitSet has any bits set to true that are also set to true in this BitSet.
     *
     * @param other a bit set
     * @return boolean indicating whether this bit set intersects the specified bit set
     */
    fun intersects(other: Bits): Boolean {
        val bits = bits
        val otherBits = other.bits
        for (i in min(bits.size, otherBits.size) - 1 downTo 0) {
            if (bits[i] and otherBits[i] != 0) {
                return true
            }
        }
        return false
    }

    /** Returns true if this bit set is a super set of the specified set, i.e. it has all bits set to true that are also set to true
     * in the specified BitSet.
     *
     * @param other a bit set
     * @return boolean indicating whether this bit set is a super set of the specified set
     */
    fun containsAll(other: Bits): Boolean {
        val bits = bits
        val otherBits = other.bits
        val otherBitsLength = otherBits.size
        val bitsLength = bits.size
        for (i in bitsLength until otherBitsLength) {
            if (otherBits[i] != 0) {
                return false
            }
        }
        for (i in min(bitsLength, otherBitsLength) - 1 downTo 0) {
            if (bits[i] and otherBits[i] != otherBits[i]) {
                return false
            }
        }
        return true
    }

    fun getSetBits() : List<Int> {
        val output = mutableListOf<Int>()
        var nextSetBit = nextSetBit(0)
        while(nextSetBit != -1) {
            output.add(nextSetBit)
            nextSetBit = nextSetBit(nextSetBit + 1)
        }
//        toString().forEachIndexed { index, c -> if(c == '1') output.add(index) }
        return output
    }

    override fun hashCode(): Int {
        val word = length() ushr 5
        var hash = 0
        var i = 0
        while (word >= i) {
            hash = 127 * hash + (bits[i] xor (bits[i] ushr 16))
            i++
        }
        return hash
    }

    override operator fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            this::class == other::class -> {
                val otherBits = (other as Bits).bits
                val commonWords: Int = min(bits.size, otherBits.size)
                var i = 0
                while (commonWords > i) {
                    if (bits[i] != otherBits[i]) return false
                    i++
                }

                if (bits.size == otherBits.size) true else length() == other.length()
            }
            else -> false
        }
    }

    operator fun plus(other: Bits) : Bits {
        or(other)
        return this
    }
    operator fun minus(other: Bits) : Bits {
        xor(other)
        return this
    }

    @ExperimentalUnsignedTypes
    override fun toString(): String {
        var output = ""
        bits.forEachIndexed { index, value ->
            val valueString = value.toUInt().toString(2)
            var paddingString = ""

            if(index != bits.size -1 && valueString.length < 32) for(i in 0 until (32 - valueString.length)) paddingString += "0"
            output = paddingString + valueString +  output
        }
        return output
    }

}

fun Any.toBits() : Bits {
    return when (this) {
        is Long -> Bits(this.toInt())
        is Int -> Bits(this)
        is Short -> Bits(this.toInt())
        is Float -> Bits(this.toInt())
        is Double -> Bits(this.toInt())
        is Byte -> Bits(this.toInt())
        is Boolean -> if(this) Bits(1) else Bits(0)
        is Char -> Bits(this.toInt())
        is String -> Bits(this.toInt())
        else -> Bits()
    }
}
