import kotlin.test.assertEquals
import org.kepler42.database.operations.fetchCommunity
import org.spekframework.spek2.Spek

object Community :
        Spek({
            group("Kotlin Community") {
                test("is fetched") {
                    val id = 1
                    val kotlinCommunity = fetchCommunity(id)
                    assertEquals(expected = "", actual = "")
                }
            }

            group("another group") { test("another test") {} }
        })
