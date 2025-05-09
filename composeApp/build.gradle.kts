import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    js(IR){
        browser()
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        val jsMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            //HTML DSL
            implementation(compose.html.core)

            //Image Load
            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.mp)
            implementation(libs.coil.network.ktor)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            // https://mvnrepository.com/artifact/com.jsoizo/kotlin-csv
            implementation("com.jsoizo:kotlin-csv:1.10.0")
        }

        jsMain.dependencies {
            // 인터넷 접속
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.serialization.kotlinx.json)


            // build.gradle.kts에 외부 라이브러리 추가
            // 안되면 수동으로 터미널에 >npm install papaparse
            implementation(npm("papaparse", "5.5.2"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.kcg.gobongchan.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.kcg.gobongchan"
            packageVersion = "1.0.0"
        }
    }



}
