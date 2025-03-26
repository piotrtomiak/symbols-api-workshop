package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.model.psi.impl.referencesAt
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.asSafely

internal class CypressAliasRenameTest : AbstractCypressTest() {

  fun testRenameFromGetUsage() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@coolAlias")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@cool<caret>Alias')
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    myFixture.renameCypressAliasSymbol("newName")

    // language=JavaScript
    val expectedResult = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('newName')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@newName")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@newName')
        })
      })""".trimIndent()

    myFixture.checkResult(expectedResult)
  }

  fun testRenameFromThisUsage() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@coolAlias")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          this.cool<caret>Alias
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    myFixture.renameCypressAliasSymbol("newName")

    // language=JavaScript
    val expectedResult = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('newName')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@newName")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          this.newName
        })
      })""".trimIndent()

    myFixture.checkResult(expectedResult)
  }

  fun testRenameFromDeclaration() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('cool<caret>Alias')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@coolAlias")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@coolAlias')
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    myFixture.renameCypressAliasSymbol("newName")

    // language=JavaScript
    val expectedResult = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('newName')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@newName")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@newName')
        })
      })""".trimIndent()

    myFixture.checkResult(expectedResult)
  }

  fun testRenameWithComplexScope() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@otherAlias')
        })
        describe("SuiteHalf", () => {
          beforeEach(() => {
            cy.get("button").as('i')
            cy.get("div.blah").as('am')
          })
          it('Test1', () => {
            cy.get('etc').as("not")
            cy.get("@not")
            cy.get("@other<caret>Alias")
          })
          it('Test2', () => {
            cy.get('etc').as("there")
          })
        })
      })
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
          cy.get("@otherAlias")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    myFixture.renameCypressAliasSymbol("newName")

    // language=JavaScript
    val expectedResult = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.get("div.blah").as('newName')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@newName')
        })
        describe("SuiteHalf", () => {
          beforeEach(() => {
            cy.get("button").as('i')
            cy.get("div.blah").as('am')
          })
          it('Test1', () => {
            cy.get('etc').as("not")
            cy.get("@not")
            cy.get("@newName")
          })
          it('Test2', () => {
            cy.get('etc').as("there")
          })
        })
      })
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
          cy.get("@otherAlias")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
        })
      })""".trimIndent()

    myFixture.checkResult(expectedResult)
  }

  fun testAliasRenamenInPageObject() {
    // language=JavaScript
    val sourceCode = """
    export default class HomePage {
      someMethod() {
        cy.intercept("GET", "/some_url").as('somealias')
        cy.wait('@some<caret>alias')
      }
    }""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    myFixture.renameCypressAliasSymbol("newName")

    // language=JavaScript
    val expectedResult = """
    export default class HomePage {
      someMethod() {
        cy.intercept("GET", "/some_url").as('newName')
        cy.wait('@newName')
      }
    }""".trimIndent()

    myFixture.checkResult(expectedResult)
  }

  private fun CodeInsightTestFixture.renameCypressAliasSymbol(newName: String) =
    renameTarget(
      file.referencesAt(caretOffset).firstOrNull()?.resolveReference()?.firstOrNull()?.asSafely<RenameTarget>()
      ?: throw AssertionError("No cypress alias symbol found at the carret"),
      newName
    )
}
