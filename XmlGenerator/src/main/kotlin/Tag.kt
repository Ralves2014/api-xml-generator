/**
 * This class represents an XML tag entity.
 *
 * @property name The name of the tag.
 * @property attributes A map of the tag's attributes, where the key is the attribute name and the value is its value.
 * @property children A list of child tags.
 * @property text The text contained within the tag.
 */
class Tag(var name: String) {
    val attributes: MutableMap<String, String> = mutableMapOf()
    val children: MutableList<Tag> = mutableListOf()
    var text: StringBuilder = StringBuilder()

    init {
        validateTagName(name)
    }

    /**
     * Validates the name of an XML tag.
     *
     * @param name The name of the tag to validate.
     * @throws IllegalArgumentException if the tag name is invalid. The tag name must start with a letter or underscore, and cannot start with 'xml'.
     */
    private fun validateTagName(name: String) {
        val regex = Regex("^[A-Za-z_][\\w\\-.]*$")
        if (!regex.matches(name) || name.startsWith("xml", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid attribute name: '$name'. Attribute names must start with a letter or underscore, and cannot start with 'xml'.")
        }
    }

    /**
     * Adds a child to the tag.
     *
     * @param tag The child to be added.
     */
    fun addChild(tag: Tag) {
        children.add(tag)
    }

    /**
     * Removes a child from the tag.
     *
     * @param tag The child to be removed.
     */
    fun removeChild(tag: Tag) {
        if (!children.contains(tag)) {
            throw IllegalArgumentException("The introduced tag is not a child of this tag.")
        }
        children.remove(tag)
    }

    /**
     * Adds an attribute to the tag.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    fun addAttribute(name: String, value: String) {
        if (this.text.isNotEmpty()) {
            throw IllegalArgumentException("Cannot add attributes to a tag with text content.")
        }
        validateAttributeName(name)
        attributes[name] = value
    }

    /**
     * Validates the name of an attribute.
     *
     * @param name The name of the attribute to validate.
     */
    private fun validateAttributeName(name: String) {
        val regex = Regex("^[A-Za-z_][\\w\\-.]*$")
        if (!regex.matches(name)) {
            throw IllegalArgumentException("Invalid attribute name: '$name'. Attribute names must start with a letter or underscore.")
        }
    }

    /**
     * Removes an attribute from the tag.
     *
     * @param name The name of the attribute to be removed.
     */
    fun removeAttribute(name: String) {
        if (!attributes.containsKey(name)) {
            throw IllegalArgumentException("The attribute with name '$name' does not exist in this tag.")
        }
        attributes.remove(name)
    }

    /**
     * Adds text to the tag.
     *
     * @param str The text to be added.
     */
    fun addText(str: String) {
        if (attributes.isNotEmpty()) {
            throw IllegalArgumentException("Cannot add text to a tag with attributes.")
        }
        text.append(str)
    }

    /**
     * Accepts a visitor to traverse the hierarchy of XML tags.
     *
     * @param visitor The visitor accepts an XML tag as a parameter
     * and returns a boolean value indicating whether the visit should continue
     * to the children of the visited tag.
     */
    fun accept(visitor: (Tag) -> Boolean) {
        if (this.children.isEmpty()){
            visitor(this)
        }
        else if (visitor(this)){
            children.forEach {
                it.accept(visitor)
            }
        }
    }

    /**
     * Sets a new name for the tag.
     *
     * @param name The new name to be assigned to the tag.
     */
    fun setTagName(name: String) {
        this.name = name
    }


    /**
     * Returns a string representation of the XML document with proper formatting.
     *
     * @return A string containing the formatted XML document.
     */
    fun prettyPrint(): String {
        val str = StringBuilder()
        //str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")

        if (this.children.isNotEmpty()) {
            val indentation = 0
            childrenIterator(this, str, indentation)
        } else {
            //str.append("<${this.name}></${this.name}>")
            str.append("<${this.name}")

            for ((attrName, attrValue) in this.attributes) {
                str.append(" $attrName=\"$attrValue\"")
            }

            if (this.text.isNotEmpty()) {
                str.append(">${this.text}</${this.name}>\n")
            } else {
                str.append("/>\n")
            }
        }
        return str.toString()
    }

    /**
     * Recursively iterates over the tags to generate the formatted representation of the XML document.
     *
     * @param tag The current tag being processed.
     * @param str The StringBuilder where the XML representation is being built.
     * @param indentation The indentation level for proper formatting.
     */
    private fun childrenIterator(tag: Tag, str: StringBuilder, indentation: Int) {
        val indent = " ".repeat(indentation * 4)
        str.append("$indent<${tag.name}")

        for ((attrName, attrValue) in tag.attributes) {
            str.append(" $attrName=\"$attrValue\"")
        }

        if (tag.children.isEmpty() && tag.text.isNotEmpty()) {
            str.append(">${tag.text}</${tag.name}>\n")
        } else if (tag.children.isEmpty() && tag.text.isEmpty()) {
            str.append("/>\n")
        } else {
            str.append(">\n")

            if (tag.text.isNotEmpty()) {
                str.append(" ".repeat((indentation + 1) * 4))
                str.append("${tag.text}\n")
            }

            for (child in tag.children) {
                childrenIterator(child, str, indentation + 1)
            }
            str.append("$indent</${tag.name}>\n")
        }
    }
}