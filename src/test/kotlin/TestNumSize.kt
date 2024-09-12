import org.junit.jupiter.api.Test

class TestNumSize {
    @Test
    fun test() {
        assert("xDEAD".determineNumSize("b", "x") == 16)
        assert("xAD".determineNumSize("b", "x") == 8)

    }
}