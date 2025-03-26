@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.startOffset
import com.intellij.webSymbols.utils.createPsiRangeNavigationItem
import java.util.*

internal class CypressAliasSymbol(
    val name: String,
    val sourceElement: JSLiteralExpression,
) : Symbol, NavigatableSymbol {

    override fun createPointer(): Pointer<CypressAliasSymbol> {
        val name = name
        val sourceElementPtr = sourceElement.createSmartPointer()
        return Pointer {
            sourceElementPtr.dereference()?.let { CypressAliasSymbol(name, it) }
        }
    }

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
        listOf(MyNavigationTarget(this))

    override fun equals(other: Any?): Boolean =
        other === this ||
                other is CypressAliasSymbol
                && other.name == name
                && other.sourceElement == sourceElement

    override fun hashCode(): Int =
        Objects.hash(name, sourceElement)

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

        fun getNavigationItem(): NavigationItem? =
            createPsiRangeNavigationItem(symbol.sourceElement, 1) as? NavigationItem

        override fun navigationRequest(): NavigationRequest? =
            getNavigationItem()?.navigationRequest()
    }

}
