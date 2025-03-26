@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

internal class CypressAliasSymbolDeclarationProvider : PsiSymbolDeclarationProvider {
    override fun getDeclarations(
        element: PsiElement,
        offsetInElement: Int
    ): Collection<CypressAliasSymbolDeclaration> =
        if (element is JSLiteralExpression && element.isCypressAliasDeclaration)
            listOf(CypressAliasSymbolDeclaration(element))
        else
            emptyList()
}

internal data class CypressAliasSymbolDeclaration(private val element: JSLiteralExpression) : PsiSymbolDeclaration {
    override fun getDeclaringElement(): PsiElement =
        element

    override fun getRangeInDeclaringElement(): TextRange =
        TextRange(1, element.text.lastIndex)

    override fun getSymbol(): Symbol =
        CypressAliasSymbol(rangeInDeclaringElement.substring(element.text), element)
}
