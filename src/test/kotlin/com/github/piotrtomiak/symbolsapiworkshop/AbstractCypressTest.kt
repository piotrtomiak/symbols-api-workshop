package com.github.piotrtomiak.symbolsapiworkshop

import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.javascript.psi.types.evaluable.JSStubBasedExpressionType
import kotlin.io.path.toPath

internal abstract class AbstractCypressTest : JSTempDirWithNodeInterpreterTest() {
    override fun setUp() {
        JSStubBasedExpressionType.setNoAssertInTestModeOnCalculatingAnchor(testRootDisposable)
        super.setUp()
        myFixture.testDataPath = AbstractCypressTest::class.java.classLoader.getResource("cypress")!!
            .toURI().toPath().parent.toString()
        myFixture.copyDirectoryToProject("cypress", "")
        performNpmInstallForPackageJson("package.json")
    }
}
