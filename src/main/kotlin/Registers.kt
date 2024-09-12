/**
 * Represents a holder of a single numeric value
 * @author RandomLonelyDev
 * @since 1.0.0
 * */
internal abstract class Register<in T : Register<T, V>, V : Comparable<V>> {

    /**
     * The private mutable variable
     * */
    abstract var pValue: V

    /**
     * Set the value of the register by a raw value
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param value The value to set the register to
     * */
    open fun setByValue(value: V) { pValue = value }

    /**
     * Set the value of the register by copying the value of another value
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param other The register to use
     * */
    open fun setByRegister(other: T) { pValue = other.pValue }

    /**
     * Get the register's value
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    open fun getValue(): V = pValue
}

/**
 * An eight-bit register
 * @author RandomLonelyDev
 * @since 1.0.0
 * */
internal class EightBitRegister(initialValue: UByte = 0u) : Register<EightBitRegister, UByte>() {
    override var pValue: UByte = initialValue
    fun setByValue(value: UInt) { setByValue(value.toUByte()) }
}

/**
 * A sixteen-bit register
 * @author RandomLonelyDev
 * @since 1.0.0
 * */
internal class SixteenBitRegister(initialValue: UShort = 0u) : Register<SixteenBitRegister, UShort>() {
    override var pValue: UShort = initialValue
}

internal class BitSpecificRegister : Register<BitSpecificRegister, BitSpecificValue>() {
    private val specificValue = BitSpecificValue()

    fun setByValue(value: UByte) = specificValue.setByByte(value)
    override fun setByValue(value: BitSpecificValue) { specificValue.setByByte(value.toByte()) }
    override fun setByRegister(other: BitSpecificRegister) = specificValue.setByByte(other.pValue.toByte())

    fun setBit(pos: BitSpecificValue.Position, value: Boolean) = specificValue.setBit(pos, value)
    fun getBit(pos: BitSpecificValue.Position) = specificValue.getBit(pos)

    override var pValue: BitSpecificValue
        get() = specificValue
        set(value) = specificValue.setByByte(value.toByte())
    fun getAsByte() = specificValue.toByte()
}