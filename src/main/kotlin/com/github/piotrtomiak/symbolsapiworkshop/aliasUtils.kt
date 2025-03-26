package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.util.asSafely

private val ALIAS_METHODS = listOf(
  "get",
  "wait"
)

private val CY_THEN_REGEX = Regex("cy(?=\\.).*\\.then")

private val JSCallExpression.methodName: String?
  get() = (methodExpression as? JSReferenceExpression)?.referenceName

internal val JSCallExpression.isCypressChainedCall: Boolean
  get() = qualifierType?.resolvedTypeText?.startsWith(CY_CHAINABLE_PREFIX) ?: false

internal val JSLiteralExpression.isCypressAliasDeclaration: Boolean
  get() =
    isQuotedLiteral &&
    parentsOfType<JSCallExpression>().firstOrNull().let {
      when {
        it == null -> false

        // for example: cy.get("foo").as("bar")
        it.isCypressChainedCall
        && it.methodName == "as" -> true

        // for example: cy.interceptGql('**/graphql', ['UserQuery'])
        this.parent is JSArrayLiteralExpression
        && it.isCypressCallExpression
        && it.methodName == "interceptGql" -> true

        // for example: cy.spy(obj, 'foo').as('bar')
        it.qualifierType?.resolvedTypeText?.startsWith(CY_SINON_SPY_AGENT_PREFIX) == true
        && it.methodName == "as" -> true

        else -> false
      }
    }

internal val JSLiteralExpression.isCypressGetOrWaitAliasUsage: Boolean
  get() =
    isQuotedLiteral &&
    this.text.drop(1).startsWith("@") &&
    parentsOfType<JSCallExpression>().firstOrNull()?.let {
      it.isCypressCallExpression && ALIAS_METHODS.contains((it.methodExpression as? JSReferenceExpression)?.referenceName)
    } ?: false

internal val PsiElement.cypressAliasName: String?
  get() = when {
    this is JSLiteralExpression && this.isCypressAliasDeclaration -> this.text.drop(1).dropLast(1)
    this is JSLiteralExpression -> this.text.drop(1).dropLast(1).drop(1)
    this is JSReferenceExpression && qualifier is JSThisExpression -> referenceName
    this is JSThisExpression -> (this.parent as? JSReferenceExpression)?.referenceName
    else -> null
  }

private val PsiFile.allCypressAliasDeclarations: List<JSLiteralExpression>
  get() = CachedValuesManager.getCachedValue(this) {
    create(findChildrenOfType(this, JSLiteralExpression::class.java).filter { it.isCypressAliasDeclaration }, this)
  }

internal fun computeScopeOfAliasDeclaration(declaration: JSLiteralExpression): PsiElement? {
  val blockScope = declaration.parentsOfType<JSFunction>().firstOrNull()?.block ?: return null

  val callee = tryFindCallee(blockScope) ?: return blockScope

  val methodExpression = callee.methodExpression?.text ?: return callee

  return when {
    methodExpression == "beforeEach" ->
      callee.parent.asSafely<JSExpressionStatement>()?.parent.asSafely<JSBlockStatement>()

    else -> callee
  }
}

private tailrec fun tryFindCallee(block: JSBlockStatement): JSCallExpression? {
  val callee = block.parent.asSafely<JSFunctionExpression>()?.parent?.asSafely<JSArgumentList>()?.parent?.asSafely<JSCallExpression>()
               ?: return null

  return when (callee.methodExpression?.text?.matches(CY_THEN_REGEX)) {
    true -> tryFindCallee(callee.parent.asSafely<JSExpressionStatement>()?.parent.asSafely<JSBlockStatement>() ?: return null)
    else -> callee
  }
}

internal fun getCypressAliasesAvailiableForContext(context: PsiElement): List<JSLiteralExpression> =
  context.containingFile?.allCypressAliasDeclarations?.filter {
    context.textRange.startOffset > it.textRange.endOffset &&
    computeScopeOfAliasDeclaration(it)?.textRange?.contains(context.textRange) ?: false
  } ?: emptyList()
