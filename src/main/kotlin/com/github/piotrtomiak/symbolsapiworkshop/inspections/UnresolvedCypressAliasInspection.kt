@file:Suppress("UnstableApiUsage")

package com.github.piotrtomiak.symbolsapiworkshop.inspections

import com.github.piotrtomiak.symbolsapiworkshop.CypressAliasSymbolReference
import com.github.piotrtomiak.symbolsapiworkshop.MyBundle
import com.github.piotrtomiak.symbolsapiworkshop.cypressAliasName
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor

class UnresolvedCypressAliasInspection : LocalInspectionTool() {
    override fun getDisplayName(): String = MyBundle.message("cy.codeInsight.inspection.unresolvedAlias.name")

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : JSElementVisitor() {
            override fun visitJSLiteralExpression(node: JSLiteralExpression) {
                PsiSymbolReferenceService.getService()
                    .getReferences(node, CypressAliasSymbolReference::class.java)
                    .filter { it.resolveReference().isEmpty() }
                    .forEach { registerUnresolvedReferenceProblem(holder, node) }
            }

        }

    private fun registerUnresolvedReferenceProblem(holder: ProblemsHolder, node: JSLiteralExpression) {
        holder.registerProblem(
            node,
            MyBundle.message("cy.codeInsight.inspection.unresolvedAlias.message", node.cypressAliasName!!),
            ProblemHighlightType.GENERIC_ERROR,
            TextRange(2, node.text.lastIndex),
        )
    }

}
