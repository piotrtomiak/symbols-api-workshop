@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class CypressAliasSymbolReference(
    private val element: PsiElement,
    private val textRangeInElement: TextRange
) : PsiSymbolReference {

    override fun getElement(): PsiElement = element

    override fun getRangeInElement(): TextRange = textRangeInElement

    override fun resolveReference(): Collection<CypressAliasSymbol> {
        if (textRangeInElement.isEmpty) return emptyList()
        val referencedName = textRangeInElement.substring(element.text)
        return getCypressAliasesAvailiableForContext(element)
            .filter { it.cypressAliasName == referencedName }
            .map { CypressAliasSymbol(referencedName, it) }
    }

}
