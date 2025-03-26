package com.github.piotrtomiak.symbolsapiworkshop

import com.github.piotrtomiak.symbolsapiworkshop.inspections.UnresolvedCypressAliasInspection

internal class UnresolvedCypressAliasInspectionTest : AbstractCypressTest() {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(UnresolvedCypressAliasInspection())
  }

  fun testInspectionExists() {
    // language=JavaScript
    val sourceCode = """
      describe('Suite', () => {      
        beforeEach(() => {
          cy.get('@<error descr="Could not find otherAlias alias declaration">otherAlias</error>')
          cy.get("div.blah").as('otherAlias')
        })
        it('Test1', () => {
          cy.get('etc').as("button")
          cy.get("@button")
        })
        it('Test2', () => {
          cy.get('@<error descr="Could not find coolAlias alias declaration">coolAlias</error>')
        })
      })""".trimIndent()

    myFixture.configureByText("cy.spec.js", sourceCode)

    myFixture.checkHighlighting(true, false, false)
  }
}
