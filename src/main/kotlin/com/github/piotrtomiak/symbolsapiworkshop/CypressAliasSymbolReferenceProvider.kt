@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange

class CypressAliasSymbolReferenceProvider : PsiSymbolReferenceProvider {

    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints,
    ): Collection<PsiSymbolReference> =
        when {
            element is JSLiteralExpression && element.isCypressGetOrWaitAliasUsage ->
                listOf(CypressAliasSymbolReference(element, TextRange(2, element.text.lastIndex)))

            element is JSReferenceExpression && element.qualifier is JSThisExpression ->
                element.referenceNameElement
                    ?.textRangeInParent
                    ?.let { listOf(CypressAliasSymbolReference(element, it)) }
                    ?: emptyList()

            else -> emptyList()
        }

    override fun getSearchRequests(
        project: Project,
        target: Symbol
    ): Collection<SearchRequest> = emptyList()
}
