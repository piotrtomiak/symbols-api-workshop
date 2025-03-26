@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

internal open class CypressAliasSymbolReference(
    private val element: PsiElement,
    private val range: TextRange
) : PsiSymbolReference {

    override fun getElement(): PsiElement = element
    override fun getRangeInElement(): TextRange = range

    override fun resolveReference(): Collection<CypressAliasSymbol> {
        if (rangeInElement.isEmpty) return mutableListOf()
        val referencedName = rangeInElement.substring(element.text)
        return getCypressAliasesAvailiableForContext(element)
            .filter { it.cypressAliasName == referencedName }
            .map { CypressAliasSymbol(referencedName, it) }
    }

}
