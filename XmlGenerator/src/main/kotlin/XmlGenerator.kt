import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

@Target(AnnotationTarget.PROPERTY)
annotation class XmlString(val value: KClass<out StringTransformer>)

@Target(AnnotationTarget.CLASS)
annotation class XmlAdapter(val value: KClass<out XmlAdaptable>)

interface XmlAdaptable {
    fun adapt(tag: Tag)
}

interface StringTransformer {
    fun transform(input: Int): String
}

class AddPercentage : StringTransformer {
    override fun transform(input: Int): String {
        return "$input%"
    }
}

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

class XmlGenerator {



    fun translate(obj: Any): Tag {
        val tagName = obj::class.simpleName!!.lowercase()
        val tag = Tag(tagName)

        val propertyOrder = obj::class.primaryConstructor?.parameters?.map { parameter ->
            val property = obj::class.declaredMemberProperties.find { it.name == parameter.name }
            property?.name to property?.getter?.call(obj)
        } ?: emptyList()

        propertyOrder.forEach { (propName, propValue) ->
            if (propName != null) {
                processProperty(tag, propName, propValue)
            }
        }

        adaptXml(obj, tag)
        return tag
    }

    private fun processProperty(parentTag: Tag, propName: String, propValue: Any?) {
        when (propValue) {
            is List<*> -> {
                propValue.forEach { listItem ->
                    if (listItem != null) {
                        val itemTag = Tag(listItem::class.simpleName.toString().lowercase())

                        listItem::class.declaredMemberProperties.forEach { itemProp ->
                            itemProp.isAccessible = true
                            val itemName = itemProp.name
                            val itemValue = itemProp.getter.call(listItem)

                            val childTag = Tag(itemName)

                            if (itemValue != null) {
                                childTag.text.append(itemValue.toString())
                            }

                            processProperty(itemTag, itemName, itemValue)
                            adaptXml(listItem,itemTag)
                        }
                        parentTag.addChild(itemTag)
                    }
                }
            }
            else -> {
                val childTag = Tag(propName)
                if (propValue != null) {
                    childTag.text.append(propValue.toString())
                }
                parentTag.addChild(childTag)
            }
        }
    }

    private fun adaptXml(obj: Any, tag: Tag) {
        val adapter = obj::class.findAnnotation<XmlAdapter>()?.value?.createInstance()
        adapter?.adapt(tag)
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

    val c = ComponenteAvaliacao("Quizzes", 20)
    val xml2 = xmlGenerator.translate(c)
    println(xml2.prettyPrint())

    //blindar a tag
    //    </jd">  <?xml> nao pode acontecer
    // microx path e para ser implementado?
    // temos de fazer os testes
    // dsl interna
}