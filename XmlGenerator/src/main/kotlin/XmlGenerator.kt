import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

/**
 * Annotation for specifying a transformer class to convert an integer to a string.
 *
 * @property value The class of the transformer to be used.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlString(val value: KClass<out StringTransformer>)

/**
 * Annotation for specifying an adapter class to adapt an XML tag.
 *
 * @property value The class of the adapter to be used.
 */
@Target(AnnotationTarget.CLASS)
annotation class XmlAdapter(val value: KClass<out XmlAdaptable>)

/**
 * Annotation used to mark a property as an XML attribute.
 *
 * @property XmlAttribute Can only be applied to properties.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlAttribute

/**
 * Annotation used to specify a custom XML element name for a class or property.
 *
 * @property value The custom name to be used for the XML element.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class XmlElementName(
    val value: String
)

/**
 * Annotation used to exclude a property from XML serialization.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Exclude

/**
 * Interface for classes that adapt an XML tag.
 */
interface XmlAdaptable {
    fun adapt(tag: Tag)
}

/**
 * Interface for classes that transform an integer into a string.
 */
interface StringTransformer {
    fun transform(input: Any): String
}

/**
 * Implementation of `StringTransformer` that adds a percentage symbol to the integer.
 */
class AddPercentage : StringTransformer {
    override fun transform(input: Any): String {
        return "$input%"
    }
}



/**
 * Class for generating and manipulating XML structures.
 */
class XmlGenerator {

    /**
     * Translates an object into an XML `Tag`.
     *
     * @param obj The object to be translated into an XML `Tag`.
     * @return The resulting XML `Tag`.
     */
    fun translate(obj: Any): Tag {
        val tagName = obj::class.findAnnotation<XmlElementName>()?.value ?: obj::class.simpleName!!.lowercase()
        val tag = Tag(tagName)

        val propertyOrder = obj::class.primaryConstructor?.parameters?.map { parameter ->
            val property = obj::class.declaredMemberProperties.find { it.name == parameter.name }
            property?.name to property?.getter?.call(obj)
        } ?: emptyList()

        propertyOrder.forEach { (propName, propValue) ->
            if (propName != null) {
                processProperty(tag, obj, propName, propValue)
            }
        }

        adaptXml(obj, tag)
        return tag
    }

    /**
     * Processes a property and adds it to the parent XML tag.
     *
     * @param parentTag The parent XML tag to which the property will be added.
     * @param propName The name of the property.
     * @param propValue The value of the property.
     */
    private fun processProperty(parentTag: Tag, obj: Any, propName: String, propValue: Any?) {
        val property = obj::class.declaredMemberProperties.find { it.name == propName }
        if (property != null && property.findAnnotation<Exclude>() == null) {
            when {
                property.findAnnotation<XmlAttribute>() != null -> {
                    if (propValue != null) {
                        val stringTransformer = property.findAnnotation<XmlString>()?.value?.createInstance()
                        val transformedValue = stringTransformer?.transform(propValue) ?: propValue.toString()
                        parentTag.addAttribute(propName, transformedValue)
                    }
                }
                property.findAnnotation<XmlElementName>() != null -> {
                    val elementName = property.findAnnotation<XmlElementName>()!!.value
                    if (propValue is List<*>) {
                        propValue.forEach { listItem ->
                            if (listItem != null) {
                                val itemTag = Tag(elementName)
                                listItem::class.declaredMemberProperties.forEach { itemProp ->
                                    itemProp.isAccessible = true
                                    val itemName = itemProp.name
                                    var itemValue = itemProp.getter.call(listItem)

                                    if (itemValue != null) {
                                        val stringTransformer = itemProp.findAnnotation<XmlString>()?.value?.createInstance()
                                        if (stringTransformer != null) {
                                            itemValue = stringTransformer.transform(itemValue)
                                        }
                                    }

                                    if (itemProp.findAnnotation<XmlAttribute>() != null) {
                                        if (itemValue != null) {
                                            itemTag.addAttribute(itemName, itemValue.toString())
                                        }
                                    } else {
                                        val childTag = Tag(itemName)
                                        if (itemValue != null) {
                                            childTag.addText(itemValue.toString())
                                        }
                                        itemTag.addChild(childTag)
                                    }
                                }
                                parentTag.addChild(itemTag)
                            }
                        }
                    }
                }
                else -> {
                    val childTag = Tag(propName)
                    if (propValue != null) {
                        childTag.addText(propValue.toString())
                    }
                    parentTag.addChild(childTag)
                }
            }
        }
    }

    /**
     * Adapts an XML tag using the specified object's `XmlAdapter` annotation.
     *
     * @param obj The object whose `XmlAdapter` annotation will be used for adaptation.
     * @param tag The XML tag to be adapted.
     */
    private fun adaptXml(obj: Any, tag: Tag) {
        val adapter = obj::class.findAnnotation<XmlAdapter>()?.value?.createInstance()
        adapter?.adapt(tag)
    }

    /**
     * Writes the XML content to a file.
     *
     * @param xmlContent The XML content to be written to the file.
     * @param fileName The name of the file where the XML content will be written.
     */
    fun xmlFile(xmlContent: Tag, fileName: String) {
        val file = File("$fileName.xml")
        file.writeText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        file.appendText(xmlContent.prettyPrint())
        println("XML file was successfully saved: $fileName.")
    }

    /**
     * Adds a global attribute to all tags with a specific name in the XML structure.
     *
     * @param xmlContent Xml content where the changes will be applied.
     * @param tagName The name of the tag to which the attribute will be added.
     * @param attributeName The name of the attribute to be added.
     * @param value The value of the attribute to be added.
     * @throws IllegalArgumentException If no tag with the specified name is found in the XML structure.
     */
    fun addGlobalAttribute(xmlContent: Tag, tagName: String, attributeName: String, value: String) {
        var tagFound = false

        xmlContent.accept { tag ->
            if (tag.name == tagName) {
                tag.addAttribute(attributeName, value)
                tagFound = true
            }
            true
        }
        if (!tagFound) {
            throw IllegalArgumentException("Tag '$tagName' not found.")
        }
    }

    /**
     * Renames a tag in the XML structure.
     *
     * @param xmlContent Xml content where the changes will be applied.
     * @param tagName The name of the tag to be renamed.
     * @param newTagName The new desired name for the tag.
     * @throws IllegalArgumentException If no tag with the specified name is found in the XML structure.
     */
    fun tagRename(xmlContent: Tag, tagName: String, newTagName: String) {
        var tagFound = false

        xmlContent.accept { tag ->
            if (tag.name == tagName) {
                tag.setTagName(newTagName)
                tagFound = true
            }
            true
        }
        if (!tagFound) {
            throw IllegalArgumentException("Tag '$tagName' not found.")
        }
    }

    /**
     * Renames an attribute in a specific tag in the XML structure.
     *
     * @param xmlContent Xml content where the changes will be applied.
     * @param tagName The name of the tag where the attribute is located.
     * @param attributeName The name of the attribute to be renamed.
     * @param newAttributeName The new desired name for the attribute.
     * @throws IllegalArgumentException If the specified tag is not found or if the specified attribute does not exist in the tag.
     */
    fun attributeRename(xmlContent: Tag, tagName: String, attributeName: String, newAttributeName: String) {
        var tagFound = false

        xmlContent.accept { tag ->
            if (tag.name == tagName) {
                tagFound = true
                val value = tag.attributes[attributeName]?.toString()
                if (value != null) {
                    tag.attributes.remove(attributeName)
                    tag.addAttribute(newAttributeName, value)
                } else {
                    throw IllegalArgumentException("The attribute '$attributeName' does not exist in the tag '$tagName'.")
                }
            }
            true
        }

        if (!tagFound) {
            throw IllegalArgumentException("The tag '$tagName' was not found.")
        }
    }

    /**
     * Removes all occurrences of a specified tag and its descendants from the XML structure.
     *
     * @param xmlContent Xml content where the changes will be applied.
     * @param tagName The name of the tag to be removed.
     * @throws IllegalArgumentException If the specified tag is not found.
     */
    fun removeTagDocument(xmlContent: Tag, tagName: String) {
        var tagFound = false

        xmlContent.accept { tag ->
            val childrenToRemove = mutableListOf<Tag>()
            for (c in tag.children) {
                if (c.name == tagName) {
                    childrenToRemove.addAll(c.children)
                    childrenToRemove.add(c)
                    tagFound = true
                }
            }

            childrenToRemove.forEach { child ->
                tag.removeChild(child)
            }
            true
        }
        if (!tagFound) {
            throw IllegalArgumentException("The tag '$tagName' was not found.")
        }
    }

    /**
     * Removes an attribute from all occurrences of a specified tag in the XML structure.
     *
     * @param xmlContent Xml content where the changes will be applied.
     * @param tagName The name of the tag where the attribute is located.
     * @param attributeName The name of the attribute to be removed.
     * @throws IllegalArgumentException If the specified tag is not found, or if the specified attribute does not exist in the tag.
     */
    fun removeAttributeGlobal(xmlContent: Tag, tagName: String, attributeName: String) {
        var tagFound = false
        var attributeFound = false

        xmlContent.accept { tag ->
            if (tag.name == tagName) {
                tagFound = true
                if (tag.attributes.containsKey(attributeName)){
                    tag.attributes.remove(attributeName)
                    attributeFound = true
                }
            }
            true
        }

        if (!tagFound) {
            throw IllegalArgumentException("The tag '$tagName' was not found.")
        }

        if (!attributeFound) {
            throw IllegalArgumentException("The attribute '$attributeName' was not found in the tag '$tagName'.")
        }
    }

    /**
     * Performs a simplified XPath search in an XML structure, starting from a given path.
     *
     * @param xmlContent Xml content to be used.
     * @param xpath The XPath path to be followed in the search.
     * @throws IllegalArgumentException If the specified path is not found in the XML file.
     */
    fun microXPath(xmlContent: Tag, xpath: String): List<Tag> {
        val path = xpath.split("/")
        val tags: MutableList<Tag> = mutableListOf()
        var depth = 0
        var pathFound = false

        xmlContent.accept { tag ->
            if (tag.name == path[depth] && depth < path.size - 1) {
                depth++
            }
            else if (tag.name == path.last() && depth == path.size - 1) {
                pathFound = true
                tags.add(tag)
            }
            true
        }

        if (!pathFound) {
            throw IllegalArgumentException("The path '$xpath' was not found in the XML content.")
        }

        return tags
    }
}