@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.parentsOfType
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.api.RenameValidationResult
import com.intellij.refactoring.rename.api.RenameValidator
import com.intellij.util.asSafely
import com.intellij.webSymbols.utils.createPsiRangeNavigationItem

data class CypressAliasSymbol(
    val name: String,
    internal val declaration: JSLiteralExpression,
) : Symbol, SearchTarget, RenameTarget, NavigatableSymbol {

    override val usageHandler: UsageHandler
        get() = UsageHandler.createEmptyUsageHandler(name)

    override val targetName: String
        get() = name

    override val maximalSearchScope: SearchScope =
        computeScopeOfAliasDeclaration(declaration)?.let { LocalSearchScope(it) }
            ?: LocalSearchScope.EMPTY

    val type: JSType? by lazy {
        declaration.parentsOfType<JSCallExpression>()
            .firstOrNull()
            ?.takeIf { it.isCypressChainedCall }
            ?.qualifierType
            ?.substitute()
            ?.asSafely<JSGenericTypeImpl>()
            ?.arguments
            ?.firstOrNull()
    }

    override fun presentation(): TargetPresentation =
        TargetPresentation.builder("Cypress Alias $name")
            .icon(AllIcons.Nodes.Alias)
            .presentation()

    override fun createPointer(): Pointer<CypressAliasSymbol> {
        val name = name
        val declarationPtr = declaration.createSmartPointer()
        return Pointer {
            CypressAliasSymbol(name, declarationPtr.dereference() ?: return@Pointer null)
        }
    }

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
        listOf(MyNavigationTarget(this))

    override fun validator(): RenameValidator = MyRenameValidator

    private object MyRenameValidator : RenameValidator {
        override fun validate(newName: String): RenameValidationResult =
            if (newName.contains(" "))
                RenameValidationResult.invalid("Spaces are not allowed")
            else if (newName.contains("-"))
                RenameValidationResult.warn("Better not to use dashes")
            else
                RenameValidationResult.ok()
    }

    private class MyNavigationTarget(private val symbol: CypressAliasSymbol) : NavigationTarget {
        override fun createPointer(): Pointer<out NavigationTarget> {
            val symbolPtr = symbol.createPointer()
            return Pointer {
                symbolPtr.dereference()?.let { MyNavigationTarget(it) }
            }
        }

        override fun computePresentation(): TargetPresentation =
            symbol.presentation()

        override fun navigationRequest(): NavigationRequest? =
            createPsiRangeNavigationItem(symbol.declaration, 1).navigationRequest()

    }

}
