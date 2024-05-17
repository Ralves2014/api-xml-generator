/**
 * Esta classe representa uma entidade(tag) XML.
 *
 * @property name O nome da tag.
 * @property attributes Um mapa de atributos da tag, onde a chave é o nome do atributo e o valor é o seu valor.
 * @property children Uma lista de filhos da tag.
 * @property text O texto contido dentro da tag.
 */
class Tag(var name: String) {
    val attributes: MutableMap<String, String> = mutableMapOf()
    val children: MutableList<Tag> = mutableListOf()
    var text: StringBuilder = StringBuilder()

    /**
     * Adiciona um filho à tag.
     *
     * @param tag O filho a ser adicionado.
     */
    fun addChild(tag: Tag) {
        children.add(tag)
    }

    /**
     * Remove um filho da tag.
     *
     * @param tag O filho a ser removido.
     */
    fun removeChild(tag: Tag) {
        if (!children.contains(tag)) {
            throw IllegalArgumentException("A tag introduzida não é filha desta tag.")
        }
        children.remove(tag)
    }

    /**
     * Adiciona um atributo à tag.
     *
     * @param name O nome do atributo.
     * @param value O valor do atributo.
     */
    fun addAttribute(name: String, value: String) {
        if (this.text.isNotEmpty()) {
            throw IllegalArgumentException("Não é possível adicionar atributos a uma tag com texto.")
        }
        attributes[name] = value
    }

    /**
     * Remove um atributo da tag.
     *
     * @param name O nome do atributo que vai ser removido.
     */
    fun removeAttribute(name: String) {
        if (!attributes.containsKey(name)) {
            throw IllegalArgumentException("O atributo com o nome '$name' não existe nesta tag.")
        }
        attributes.remove(name)
    }

    /**
     * Adiciona texto à tag.
     *
     * @param str O texto a ser adicionado.
     */
    fun addText(str: String) {
        if (attributes.isNotEmpty()) {
            throw IllegalArgumentException("Não é possível adicionar texto a uma tag com atributos.")
        }
        text.append(str)
    }

    /**
     * Aceita um visitante para percorrer a hierarquia de tags XML.
     *
     * @param visitor O visitante aceita uma tag XML como parâmetro
     * e retorna um valor booleano indicando se a visita deve continuar
     * nos filhos da tag visitada.
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
     * Define um novo nome para a tag.
     *
     * @param newname O novo nome a ser atribuído à tag.
     */
    fun setTagname(newname: String) {
        this.name = newname
    }


    /**
     * Retorna uma representação em formato de string do documento XML, com formatação adequada.
     *
     * @return Uma string contendo o documento XML formatado.
     */
    fun prettyPrint(): String {
        val str = StringBuilder()
        //str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")

        if (this.children.isNotEmpty()) {
            val indentation = 0
            childrensIterator(this, str, indentation)
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
     * Itera recursivamente sobre as tags para gerar a representação formatada do documento XML.
     *
     * @param tag A tag atual sendo processada.
     * @param str O StringBuilder onde a representação XML está sendo construída.
     * @param indentation O nível de indentação para a formatação correta.
     */
    fun childrensIterator(tag: Tag, str: StringBuilder, indentation: Int) {
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
                childrensIterator(child, str, indentation + 1)
            }
            str.append("$indent</${tag.name}>\n")
        }
    }
}