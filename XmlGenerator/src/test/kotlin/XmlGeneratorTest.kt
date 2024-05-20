import org.junit.jupiter.api.Test

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
        println(xml.prettyPrint())
        //xmlGenerator.xmlFile(xml,"test")

        val m = xmlGenerator.microXPath(xml, "fuc/componente")

        val c = ComponenteAvaliacao("Quizzes", 20)
        val xml2 = xmlGenerator.translate(c)
        println(xml2.prettyPrint())

        xmlGenerator.addGlobalAttribute(xml2, "componente", "teste", "aquii")

        println(xml2.prettyPrint())
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

    val m = xmlGenerator.microXPath(xml, "fuc/componente")

    val c = ComponenteAvaliacao("Quizzes", 20)
    val xml2 = xmlGenerator.translate(c)
    println(xml2.prettyPrint())

    xmlGenerator.addGlobalAttribute(xml2, "componente", "teste", "aquii")

    println(xml2.prettyPrint())

}