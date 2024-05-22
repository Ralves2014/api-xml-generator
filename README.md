# XmlGenerator
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)


## O que é o XmlGenerator?
**XmlGenerator** é uma biblioteca desenvolvida em Kotlin que permite a geração e manipulação de documentos XML de maneira simples e eficiente. O principal objetivo desta biblioteca é facilitar a criação e edição de documentos XML estruturados, proporcionando uma API intuitiva e poderosa para adicionar, remover e manipular entidades e atributos.

## Como funciona?
A biblioteca possui duas classes: **XmlGenerator** e **Tag**.
- **XmlGenerator**: É a classe principal, responsável pela tradução de objetos em estruturas XML e pela sua manipulação;
- **Tag**: Classe de suporte que representa uma entidade XML. Com a classe Tag, é possível adicionar, remover e manipular atributos, texto e tags filhas, permitindo a construção e modificação detalhada de documentos XML.

## Métodos da Classe XmlGenerator
A partir da classe **XmlGenerator** é possível utilizar os seguintes métodos:

| Método | Descrição |
|---|---|
| translate(obj: Any): Tag | Traduz um objeto numa entidade XML (Tag) |
| xmlFile(xmlContent: Tag, fileName: String) | Escrita do conteúdo XML traduzido para um ficheiro com um nome definido pelo utilizador |
| addGlobalAttribute(xmlContent: Tag, tagName: String, attributeName: String, value: String) | Adiciona um atributo global a todas as entidades com um nome específico na estrutura XML |
| tagRename(xmlContent: Tag, tagName: String, newTagName: String) | Renomeia uma entidade na estrutura XML |
| attributeRename(xmlContent: Tag, tagName: String, attributeName: String, newAttributeName: String) | Renomeia um atributo numa entidade específica na estrutura XML |
| removeTagDocument(xmlContent: Tag, tagName: String) | Remove todas as ocorrências de uma entidade específica bem como os seus descendentes da estrutura XML |
| removeAttributeGlobal(xmlContent: Tag, tagName: String, attributeName: String) | Remove um atributo de todas as ocorrências de uma entidade específica na estrutura XML |
| microXPath(xmlContent: Tag, xpath: String): List<Tag> | Realiza uma busca XPath simplificada numa estrutura XML |

## Exemplo de Uso
### A partir de Objetos:


Neste exemplo, vamos detalhar todos os passos para gerar um ficheiro XML.
1. Definir uma classe que representa o conteúdo XML pretendido:
```kotlin
class FUC(
    val codigo: String,
    val nome: String,
    val ects: Double,
    val observacoes: String,
    val avaliacao: List<ComponenteAvaliacao>
)

class ComponenteAvaliacao(
    val nome: String,
    val peso: Int
)
```
2. Definir as anotações desejadas:
```kotlin
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
```
3. Utilizar a anotação `@XmlAdapter` para adaptar a ordem dos atributos:
```kotlin
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
```
4. Inicializar a classe **XmlGenerator** e a classe **FUC**:
```kotlin
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
```
5. Traduzir o objeto para XML:
```kotlin
val xml = xmlGenerator.translate(fuc)
```
7. Utilizar um método da classe **XmlGenerator** para gerar um ficheiro:
```kotlin
xmlGenerator.xmlFile(xml,"test")
```

### DSL Interna
De forma a facilitar a instanciação de modelos XML, foi desenvolvida uma DSL Interna em **Kotlin**.
```kotlin
val xmlGenerator = XmlGenerator()

val xml = dictionaryXml("fuc") {
    attr("codigo", "M4310")
    
    tag("ects") {
        text("6.0")
    }
    
    tag("nome") {
        text("Programação Avançada")
    }
    
    tag("componente") {
        attr("nome", "Quizzes")
        attr("peso", "20%")
    }
    
    tag("componente") {
        attr("nome", "Projeto")
        attr("peso", "80%")
    }
}

xmlGenerator.xmlFile(xml,"test")
```
#### Output
O conteúdo do ficheiro `test.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<fuc codigo="M4310">
    <ects>6.0</ects>
    <nome>Programação Avançada</nome>
    <componente nome="Quizzes" peso="20%"/>
    <componente nome="Projeto" peso="80%"/>
</fuc>
```
## Anotações


