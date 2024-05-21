import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

@XmlAdapter(FUCAdapter::class)
class FUC(
    @XmlAttribute
    val codigo: String,
    val nome: String,
    val ects: Double,
    @Exclude
    val observacoes: String,
    @XmlElementName("componente")
    val avaliacao: List<ComponenteAvaliacao>
)

@XmlElementName("componente")
class ComponenteAvaliacao(
    @XmlAttribute
    val nome: String,
    @XmlAttribute
    @XmlString(AddPercentage::class)
    val peso: Int
)

class FUCAdapter : XmlAdaptable {
    override fun adapt(tagFuc: Tag) {
        val orderedAttributes = linkedMapOf<String, String>()

        tagFuc.attributes["codigo"]?.let { orderedAttributes["codigo"] = it }
        tagFuc.attributes["ects"]?.let { orderedAttributes["ects"] = it }
        tagFuc.attributes["nome"]?.let { orderedAttributes["nome"] = it }

        tagFuc.attributes.clear()
        tagFuc.attributes.putAll(orderedAttributes)

        val orderedChildren = mutableListOf<Tag>()

        tagFuc.children.find { it.name == "ects" }?.let { orderedChildren.add(it) }
        tagFuc.children.find { it.name == "nome" }?.let { orderedChildren.add(it) }

        tagFuc.children.filterNot { it.name == "ects" || it.name == "nome" }.forEach {
            orderedChildren.add(it)
        }

        tagFuc.children.clear()
        tagFuc.children.addAll(orderedChildren)
    }
}

class XmlGeneratorTest {
    @Test
    fun testTranslate() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        val content = xml.prettyPrint()

        val example = File("example.xml")
        val contentOfExample = example.readText()

        assertEquals(contentOfExample, content)
    }

    @Test
    fun testXmlFile() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.xmlFile(xml,"test")

        val ouput = File("test.xml")
        val expectedOutput = """
            <?xml version="1.0" encoding="UTF-8"?>
            <fuc codigo="M4310">
                <ects>6.0</ects>
                <nome>Programação Avançada</nome>
                <componente nome="Quizzes" peso="20%"/>
                <componente nome="Projeto" peso="80%"/>
            </fuc>
        """.trimIndent()

        assertEquals(expectedOutput,ouput.readText())
    }

    @Test
    fun testAddGlobalAttribute() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.addGlobalAttribute(xml,"componente","avaliacao", "presencial")

        xml.accept { tag ->
            for (c in tag.children) {
                if (c.name == "componente") {
                    assertEquals(3, c.attributes.size)
                    assertTrue(c.attributes.containsKey("avaliacao"))
                }
            }
            true
        }

        assertThrows<IllegalArgumentException> {
            xmlGenerator.addGlobalAttribute(xml,"componete", "avaliacao", "presencial")
        }
    }

    @Test
    fun testTagRename() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.tagRename(xml,"nome","uc")

        var tagFound = false
        xml.accept { tag ->
            for (c in tag.children) {
                if (c.name == "uc") {
                    tagFound = true
                }
            }
            true
        }
        assertTrue(tagFound)

        assertThrows<IllegalArgumentException> {
            xmlGenerator.tagRename(xml,"fuc7", "uc")
        }
    }

    @Test
    fun testAttributeRename() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.attributeRename(xml,"componente", "nome", "metodo")

        xml.accept { tag ->
            for (c in tag.children) {
                if (c.name == "componente") {
                    assertTrue(c.attributes.containsKey("metodo"))
                    assertFalse(c.attributes.containsKey("nome"))
                }
            }
            true
        }

        assertThrows<IllegalArgumentException> {
            xmlGenerator.attributeRename(xml,"componete", "nome", "metodo")
        }

        assertThrows<IllegalArgumentException> {
            xmlGenerator.attributeRename(xml,"componente", "nom", "metodo")
        }
    }

    @Test
    fun testRemoveTagDocument() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.removeTagDocument(xml,"nome")

        var tagRemoved = true
        xml.accept { tag ->
            for (c in tag.children) {
                if (c.name == "nome") {
                    tagRemoved = false
                }
            }
            true
        }

        assertTrue(tagRemoved)

        assertThrows<IllegalArgumentException> {
            xmlGenerator.removeTagDocument(xml,"componete")
        }
    }

    @Test
    fun testRemoveAttributeGlobal() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.removeAttributeGlobal(xml,"componente","nome")

        xml.accept { tag ->
            for (c in tag.children) {
                if (c.name == "componente") {
                    assertFalse(c.attributes.containsKey("nome"))
                    assertTrue(c.attributes.containsKey("peso"))
                }
            }
            true
        }

        assertThrows<IllegalArgumentException> {
            xmlGenerator.removeAttributeGlobal(xml,"componete", "nome")
        }

        assertThrows<IllegalArgumentException> {
            xmlGenerator.removeAttributeGlobal(xml,"componente", "nom")
        }
    }

    @Test
    fun testMicroXPath() {
        val xmlGenerator = XmlGenerator()
        val fuc = FUC(
            "M4310",
            "Programação Avançada",
            6.0,
            "la la...",
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        val lt = xmlGenerator.microXPath(xml, "fuc/componente")

        lt.forEach { t ->
            assert(t.name.toString() == "componente")
        }

        assertThrows<IllegalArgumentException> {
            xmlGenerator.microXPath(xml, "fuc/avaliacao/componente")
        }
    }
}