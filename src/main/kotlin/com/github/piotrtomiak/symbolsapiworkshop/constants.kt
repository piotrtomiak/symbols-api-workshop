package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSQualifiedExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression

internal const val CY_TYPE_TEXT = "Cypress.cy&CyEventEmitter"

internal const val CYPRESS_CHAINABLE = "Cypress.Chainable"

internal const val CY_CHAINABLE_PREFIX = "Cypress.Chainable<"

internal const val CY_SINON_SPY_AGENT_PREFIX = "Cypress.SinonSpyAgent<"

val JSCallExpression.isCypressCallExpression: Boolean
  get() {
    var topmostQualifier: JSExpression? = (methodExpression as? JSQualifiedExpression)?.qualifier ?: return false
    while (topmostQualifier is JSQualifiedExpression) {
      val qualifier = topmostQualifier.qualifier
      if (qualifier == null) break
      topmostQualifier = qualifier
    }
    return topmostQualifier is JSReferenceExpression && topmostQualifier.referenceName == "cy" &&
           qualifierType?.resolvedTypeText == CY_TYPE_TEXT
  }
