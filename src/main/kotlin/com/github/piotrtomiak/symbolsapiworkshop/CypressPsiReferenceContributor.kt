package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class CypressPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            JSPatterns.jsReferenceExpression(),
            JSReferenceExpressionPsiReferenceProvider()
        )
    }

    private class JSReferenceExpressionPsiReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext
        ): Array<out PsiReference?> =
            if (element is JSReferenceExpression && element.cypressAliasName != null)
                arrayOf(CypressAliasPsiReference(element))
            else
                PsiReference.EMPTY_ARRAY

    }

    private class CypressAliasPsiReference(element: JSReferenceExpression) :
        PsiPolyVariantReferenceBase<JSReferenceExpression>(
            element, element.rangeInElement, true
        ) {

        override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> =
            CypressAliasSymbolReference(element, rangeInElement)
                .resolveReference().firstOrNull()
                ?.let { CypressAliasJSImplicitElement(it) }
                ?.let { arrayOf(PsiElementResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY

    }

}
