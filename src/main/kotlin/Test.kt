fun main() {
    for(i in 0..255) for(j in 0..255) {
        val v1 = BitSpecificValue.of(i.toUByte())
        val v2 = BitSpecificValue.of(j.toUByte())
        val decAnswer = i + j
        val (bsvAnswer, hasCarry, signedOverflow) = v1.plus(v2)
        if(!hasCarry) require(bsvAnswer.toByte() == decAnswer.toUByte()) { "Error. $decAnswer should equal the addition but result was ${bsvAnswer.toByte()}" }
        else require((bsvAnswer.toByte() + 256u) == decAnswer.toUInt()) { "Error. ${decAnswer - 256} should equal the addition (with carry) but result was ${bsvAnswer.toByte()}" }
    }
}