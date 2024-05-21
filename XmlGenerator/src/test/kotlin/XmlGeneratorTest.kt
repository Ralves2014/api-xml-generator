import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

@XmlAdapter(FUCAdapter::class)
class FUC(
    val codigo: String,
    val nome: String,
    val ects: Double,
    val avaliacao: List<ComponenteAvaliacao>
)

@XmlAdapter(ComponenteAvaliacaoAdapter::class)
class ComponenteAvaliacao(
    val nome: String,
    @XmlString(AddPercentage::class)
    val peso: Int
)

class FUCAdapter : XmlAdaptable {
    override fun adapt(tag: Tag) {
        tag.children.find { it.name == "codigo" }?.let {
            tag.attributes["codigo"] = it.text.toString()
            tag.children.remove(it)
        }

        val aTag = tag.children.find { it.name == "avaliacao" }
        if (aTag != null) {
            aTag.children.forEach { componentTag ->
                tag.addChild(componentTag)
            }
            tag.children.remove(aTag)
        }

        //val ectsIndex = tag.children.indexOfFirst { it.name == "ects" }
    }
}

class ComponenteAvaliacaoAdapter : XmlAdaptable {
    override fun adapt(tag: Tag) {
        val childrenToRemove = mutableListOf<Tag>()
        tag.setTagName("componente")

        tag.children.forEach { childTag ->
            when (childTag.name) {
                "peso" -> {
                    val pesoValue = childTag.text.toString().toInt()
                    val transformedPeso = AddPercentage().transform(pesoValue)
                    tag.addAttribute("peso", transformedPeso)
                    childrenToRemove.add(childTag)
                }
                "nome" -> {
                    tag.addAttribute("nome", childTag.text.toString())
                    childrenToRemove.add(childTag)
                }
            }
        }

        childrenToRemove.forEach { tag.removeChild(it) }
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
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        val content = xml.prettyPrint()

        // temos de alterar a ordem
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
            listOf(
                ComponenteAvaliacao("Quizzes", 20),
                ComponenteAvaliacao("Projeto", 80)
            )
        )
        val xml = xmlGenerator.translate(fuc)
        xmlGenerator.xmlFile(xml,"test")

        val ouput = File("test.xml")
        val expectedOutput = """
            
        """.trimIndent()

        // so falta alterar a ordem

        assertEquals(expectedOutput,ouput.readText())
    }

    @Test
    fun testAddGlobalAttribute() {
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

fun main() {
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
    println(xml.prettyPrint())
    xmlGenerator.xmlFile(xml,"test")

}