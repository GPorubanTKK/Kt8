/**
 * Takes two bytes (8 bits each) and combines them into one short (16 bits)
 * twoBytesToShort(0x07, 0xD0) = 0x07D0
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param top8 The byte representing the top 8 bits of the short
 * @param bottom8 The byte representing the bottom 8 bits of the short
 * @return A short in the formula of (top8 * 256) + bottom8
 * */
internal fun twoBytesToShort(top8: UByte, bottom8: UByte) = ((top8 * 256u) + bottom8).toUShort()