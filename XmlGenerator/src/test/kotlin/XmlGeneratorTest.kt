import org.junit.jupiter.api.Test

class XmlGeneratorTest {
    @Test
    fun testTranslate() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )

        val xml = xmlGenerator.translate(fuc)
    }
}