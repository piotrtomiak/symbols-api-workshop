package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement

data class CypressAliasJSImplicitElement(
    val symbol: CypressAliasSymbol,
) : JSLocalImplicitElementImpl(
    symbol.name,
    null,
    symbol.declaration,
    JSImplicitElement.Type.Variable,
)
