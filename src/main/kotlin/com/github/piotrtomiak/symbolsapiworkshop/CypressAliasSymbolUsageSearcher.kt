@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.find.usages.api.PsiUsage
import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.model.Pointer
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.model.search.LeafOccurrence
import com.intellij.model.search.LeafOccurrenceMapper
import com.intellij.model.search.SearchContext
import com.intellij.model.search.SearchService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.walkUp
import com.intellij.util.Query
import com.intellij.util.asSafely

class CypressAliasSymbolUsageSearcher : UsageSearcher {

    override fun collectSearchRequests(parameters: UsageSearchParameters): Collection<Query<out Usage>> =
        parameters.target.asSafely<CypressAliasSymbol>()
            ?.let { buildCypressSymbolUsagesQueries(it, parameters.project, parameters.searchScope) }
            ?: emptyList()

}

internal fun buildCypressSymbolUsagesQueries(
    symbol: CypressAliasSymbol,
    project: Project,
    searchScope: SearchScope
) = listOf(
    SearchService.getInstance()
        .searchWord(project, symbol.name)
        .inContexts(
            SearchContext.inCodeHosts(),
            SearchContext.inCode(),
            SearchContext.inStrings(),
        )
        .inScope(searchScope)
        .buildQuery(
            LeafOccurrenceMapper.withPointer(
                symbol.createPointer(),
                ::findReferencesToSymbol
            )
        )
)

private fun findReferencesToSymbol(symbol: CypressAliasSymbol, leafOccurrence: LeafOccurrence): Collection<PsiUsage> {
    val symbolReferenceService = PsiSymbolReferenceService.getService()
    for ((element, offsetInElement) in walkUp(
        leafOccurrence.start,
        leafOccurrence.offsetInStart,
        leafOccurrence.scope
    )) {
        if (element !is PsiExternalReferenceHost)
            continue

        val declarations = CypressAliasSymbolDeclarationProvider()
            .getDeclarations(element, offsetInElement)
        if (declarations.isNotEmpty()) {
            return declarations
                .filter { it.symbol == symbol }
                .map {
                    CypressSymbolPsiUsage(
                        it.declaringElement.containingFile,
                        it.absoluteRange,
                        true
                    )
                }
        }

        val foundReferences = symbolReferenceService
            .getReferences(element, PsiSymbolReferenceHints.offsetHint(offsetInElement))
            .asSequence()
            .filterIsInstance<CypressAliasSymbolReference>()
            .filter { it.rangeInElement.containsOffset(offsetInElement) }
            .filter { it.resolvesTo(symbol) }
            .toList()

        return foundReferences.map { CypressSymbolPsiUsage(it.element.containingFile, it.absoluteRange, false) }
    }
    return emptyList()
}


private data class CypressSymbolPsiUsage(
    override val file: PsiFile,
    override val range: TextRange,
    override val declaration: Boolean
) : PsiUsage {
    override fun createPointer(): Pointer<out PsiUsage> {
        val declaration = declaration
        val ptr = SmartPointerManager.getInstance(file.project).createSmartPsiFileRangePointer(file, range)
        return Pointer {
            val element = ptr.element ?: return@Pointer null
            val range = ptr.range?.let(TextRange::create) ?: return@Pointer null
            CypressSymbolPsiUsage(element, range, declaration)
        }
    }
}
