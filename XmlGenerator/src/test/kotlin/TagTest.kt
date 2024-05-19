import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TagTest {
    @Test
    fun testAddChild() {
        val t = Tag("plano")
        val c = Tag("curso")
        val f = Tag("fuc")

        assertTrue(t.children.isEmpty())
        assertTrue(c.children.isEmpty())
        assertTrue(f.children.isEmpty())

        t.addChild(c)
        assertEquals(1, t.children.size)
        assertEquals(c, t.children[0])

        t.addChild(f)
        assertEquals(2, t.children.size)
        assertEquals(f, t.children[1])
    }

    @Test
    fun testRemoveChild() {
        val t = Tag("plano")
        val c = Tag("curso")
        val f = Tag("fuc")

        assertTrue(t.children.isEmpty())
        assertThrows<IllegalArgumentException> {
            t.removeChild(c)
        }

        t.addChild(c)
        t.removeChild(c)
        assertTrue(t.children.isEmpty())

        t.addChild(c)
        t.addChild(f)
        t.removeChild(c)
        assertEquals(1, t.children.size)
        assertEquals(f, t.children[0])
    }

    @Test
    fun testaddAttribute() {
        val c = Tag("curso")
        val f = Tag("fuc")

        assertTrue(f.attributes.isEmpty())
        f.addAttribute("codigo", "M4310")

        assertTrue(f.attributes.containsKey("codigo"))
        assertEquals("M4310", f.attributes["codigo"])

        c.addText("Mestrado em Engenharia Informática")
        assertThrows<IllegalArgumentException> {
            c.addAttribute("codigo", "M4310")
        }
    }

    @Test
    fun testRemoveAttribute() {
        val f = Tag("fuc")

        assertTrue(f.attributes.isEmpty())
        f.addAttribute("codigo", "M4310")
        f.removeAttribute("codigo")

        assertFalse(f.attributes.containsKey("codigo"))

        assertThrows<IllegalArgumentException> {
            f.removeAttribute("peso")
        }
    }

    @Test
    fun testAddText() {
        val f = Tag("fuc")
        val c = Tag("curso")

        c.addText("Mestrado em Engenharia Informática")
        assertEquals("Mestrado em Engenharia Informática", c.text.toString())

        f.addAttribute("codigo", "M4310")
        assertThrows<IllegalArgumentException> {
            f.addText("Mestrado em Engenharia Informática")
        }
    }

    @Test
    fun testAccept() {
        val plano = Tag("plano")

        val curso = Tag("curso")
        curso.addText("Mestrado em Engenharia Informática")
        plano.addChild(curso)

        val fuc1 = Tag("fuc")
        fuc1.addAttribute("codigo", "M4310")

        val nome1 = Tag("nome")
        nome1.addText("Programação Avançada")
        fuc1.addChild(nome1)

        val ects1 = Tag("ects")
        ects1.addText("6.0")
        fuc1.addChild(ects1)

        val avaliacao1 = Tag("avaliacao")
        val componente1 = Tag("componente")
        componente1.addAttribute("nome", "Quizzes")
        componente1.addAttribute("peso", "20%")
        val componente2 = Tag("componente")
        componente2.addAttribute("nome", "Projeto")
        componente2.addAttribute("peso", "80%")
        avaliacao1.addChild(componente1)
        avaliacao1.addChild(componente2)
        fuc1.addChild(avaliacao1)

        plano.addChild(fuc1)

        val str = StringBuilder()

        plano.accept { tag ->
            str.append(tag.name + "/")
            true
        }
        assertEquals("plano/curso/fuc/nome/ects/avaliacao/componente/componente/", str.toString())
    }

    @Test
    fun testValidateTagName() {
        val plano = Tag("plano")
        plano.validateTagName()

        assertThrows<IllegalArgumentException> {
            val invalidTag = Tag("123_invalid_tag")
            invalidTag.validateTagName()
        }

        assertThrows<IllegalArgumentException> {
            val invalidTag2 = Tag("invalid<name")
            invalidTag2.validateTagName()
        }

        assertThrows<IllegalArgumentException> {
            val invalidTag3 = Tag("xmlInvalidTag")
            invalidTag3.validateTagName()
        }
    }

    @Test
    fun testValidateAttributeName() {
        val componente = Tag("componente")
        componente.addAttribute("nome", "Quizzes")

        componente.validateAttributeName("nome")

        assertThrows<IllegalArgumentException> {
            val componente2 = Tag("componente")
            componente2.addAttribute("123invalidAttr", "Quizzes")

            componente2.validateAttributeName("123invalidAttr")
        }

        assertThrows<IllegalArgumentException> {
            val componente3 = Tag("componente")
            componente3.addAttribute("invalid Attr", "Quizzes")

            componente3.validateAttributeName("invalid Attr")
        }

        assertThrows<IllegalArgumentException> {
            val componente4 = Tag("componente")
            componente4.addAttribute("inva@lidAttr", "Quizzes")

            componente4.validateAttributeName("inva@lidAttr")
        }
    }
}