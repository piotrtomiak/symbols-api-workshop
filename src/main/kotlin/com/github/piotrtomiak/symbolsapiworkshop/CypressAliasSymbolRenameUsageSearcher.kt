package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.find.usages.api.PsiUsage
import com.intellij.model.Pointer
import com.intellij.refactoring.rename.api.*
import com.intellij.util.Query
import com.intellij.util.asSafely

class CypressAliasSymbolRenameUsageSearcher : RenameUsageSearcher {

    override fun collectSearchRequests(parameters: RenameUsageSearchParameters): Collection<@JvmWildcard Query<out RenameUsage>> =
        parameters.target.asSafely<CypressAliasSymbol>()?.let { symbol ->
            buildCypressSymbolUsagesQueries(symbol, parameters.project, parameters.searchScope)
                .map { query ->
                    query.mapping { psiUsage: PsiUsage ->
                        CypressAliasSymbolRenameUsage(symbol, PsiRenameUsage.defaultPsiRenameUsage(psiUsage))
                    }
                }
        }
            ?: emptyList()

    private class CypressAliasSymbolRenameUsage(
        private val symbol: CypressAliasSymbol,
        private val defaultPsiRenameUsage: PsiRenameUsage,
    ) : PsiRenameUsage by defaultPsiRenameUsage, PsiModifiableRenameUsage {

        override fun createPointer(): Pointer<CypressAliasSymbolRenameUsage> {
            val symbolPtr = symbol.createPointer()
            val defaultPsiRenameUsagePtr = defaultPsiRenameUsage.createPointer()
            return Pointer {
                val symbol = symbolPtr.dereference() ?: return@Pointer null
                val defaultPsiRenameUsage = defaultPsiRenameUsagePtr.dereference() ?: return@Pointer null
                CypressAliasSymbolRenameUsage(symbol, defaultPsiRenameUsage)
            }
        }
    }
}
