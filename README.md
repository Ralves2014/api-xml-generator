# XmlGenerator

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

## Anotações
