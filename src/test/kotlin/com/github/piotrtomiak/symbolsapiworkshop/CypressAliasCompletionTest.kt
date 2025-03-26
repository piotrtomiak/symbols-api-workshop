package com.github.piotrtomiak.symbolsapiworkshop

internal class CypressAliasCompletionTest : AbstractCypressTest() {

  fun testAliasCompletionInCypressGet() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias')
          cy.spy("foo", "charAt").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
        })
        it('Test2', () => {
          cy.get('etc').as("form")
          cy.get('@<caret>')
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    val expectedResults = listOf(
      "coolAlias",
      "otherAlias",
      "form"
    ).sorted()

    val actualResults = myFixture.completeBasic().map { it.lookupString }.sorted()

    assertOrderedEquals(actualResults, expectedResults)
  }

  fun testAliasCompletionInCypressGetWithMultipleBeforeEach() {
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
          cy.get('@<caret>')
        })
        describe("SuiteHalf", () => {
          beforeEach(() => {
            cy.get("button").as('i')
            cy.get("div.blah").as('am')
          })
          it('Test1', () => {
            cy.get('etc').as("not")
            cy.get("@not")
          })
          it('Test2', () => {
            cy.get('etc').as("there")
          })
        })
      })
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").as('coolAlias1')
          cy.get("div.blah").as('otherAlias1')
        })
        it('Test1', () => {
          cy.get('etc').as("button1")
          cy.get("@button")
        })
        it('Test2', () => {
          cy.get('etc').as("form1")
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    val expectedResults = listOf(
      "coolAlias",
      "otherAlias",
      "form"
    ).sorted()

    val actualResults = myFixture.completeBasic().map { it.lookupString }.sorted()

    assertOrderedEquals(actualResults, expectedResults)
  }

  fun testAliasCompletionInThisReference() {
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
        it('Test2', function () {
          cy.get('etc').as("form")
          this.o<caret>
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    val expectedResults = listOf(
      "coolAlias",
      "otherAlias",
      "form"
    ).sorted()

    val actualResults = myFixture.completeBasic().map { it.lookupString }.sorted()

    assertContainsElements(actualResults, expectedResults)
  }

  fun testAliasCompletionInThisReferenceTs() {
    // language=TypeScript
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
        it('Test2', function () {
          cy.get('etc').as("form")
          this.o<caret>
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.ts", sourceCode)

    myFixture.checkHighlighting()

    val expectedResults = listOf(
      "coolAlias",
      "otherAlias",
      "form"
    ).sorted()

    val actualResults = myFixture.completeBasic().map { it.lookupString }.sorted()

    assertContainsElements(actualResults, expectedResults)
  }

  fun testAliasCompletionInPageObject() {
    // language=JavaScript
    val sourceCode = """
    export default class HomePage {
      someMethod() {
        cy.intercept("GET", "/some_url").as('somealias')
        cy.wait('@<caret>')
      }
    }""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    val expectedResults = listOf(
      "somealias"
    ).sorted()

    val actualResults = myFixture.completeBasic().map { it.lookupString }.sorted()

    assertContainsElements(actualResults, expectedResults)
  }

  fun testAliasCompletionWhenDeclaredInThen() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get("button").then(() => {
              cy.wrap("bvlah").as("coolAlias")
          })
          cy.get("button").then(() => {
            cy.get("button").then(() => {
                cy.wrap("bvlah").as("otherAlias")
             })
          })
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
        })
        it('Test2', function () {
          cy.get('etc').as("form")
          this.o<caret>
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting()

    val expectedResults = listOf(
      "coolAlias",
      "otherAlias",
      "form"
    ).sorted()

    val actualResults = myFixture.completeBasic().map { it.lookupString }.sorted()

    assertContainsElements(actualResults, expectedResults)
  }

}
