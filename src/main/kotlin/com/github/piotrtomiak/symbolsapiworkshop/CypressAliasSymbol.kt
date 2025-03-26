@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.utils.createPsiRangeNavigationItem

data class CypressAliasSymbol(
    val name: String,
    private val declaration: JSLiteralExpression,
) : Symbol, NavigatableSymbol {

    override fun createPointer(): Pointer<CypressAliasSymbol> {
        val name = name
        val declarationPtr = declaration.createSmartPointer()
        return Pointer {
            CypressAliasSymbol(name, declarationPtr.dereference() ?: return@Pointer null)
        }
    }

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
        listOf(MyNavigationTarget(this))

    private class MyNavigationTarget(private val symbol: CypressAliasSymbol) : NavigationTarget {
        override fun createPointer(): Pointer<out NavigationTarget> {
            val symbolPtr = symbol.createPointer()
            return Pointer {
                symbolPtr.dereference()?.let { MyNavigationTarget(it) }
            }
        }

        override fun computePresentation(): TargetPresentation =
            TargetPresentation.builder("Cypress Alias ${symbol.name}")
                .presentation()

        override fun navigationRequest(): NavigationRequest? =
            createPsiRangeNavigationItem(symbol.declaration, 1).navigationRequest()

    }

}
