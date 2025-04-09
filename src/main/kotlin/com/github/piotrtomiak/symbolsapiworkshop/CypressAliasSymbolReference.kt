@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.model.psi.PsiCompletableReference
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class CypressAliasSymbolReference(
    private val element: PsiElement,
    private val textRangeInElement: TextRange
) : PsiSymbolReference, PsiCompletableReference {

    override fun getElement(): PsiElement = element

    override fun getRangeInElement(): TextRange = textRangeInElement

    override fun resolveReference(): Collection<CypressAliasSymbol> {
        if (textRangeInElement.isEmpty) return emptyList()
        val referencedName = textRangeInElement.substring(element.text)
        return getCypressAliasesAvailiableForContext(element)
            .filter { it.cypressAliasName == referencedName }
            .map { CypressAliasSymbol(referencedName, it) }
    }

    override fun getCompletionVariants(): Collection<LookupElement> =
        getCypressAliasesAvailiableForContext(element).mapNotNull { declaration ->
            val lookupElement =
                LookupElementBuilder.create(
                    declaration.cypressAliasName?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                )
                    .withIcon(AllIcons.Nodes.Variable)
                    .withTailText(" cypress alias", true)

            PrioritizedLookupElement.withPriority(
                lookupElement,
                JSLookupPriority.SMART_PRIORITY.priorityValue.toDouble()
            )
        }

}
