package buildlogic

val java21JvmDefaults = listOf(
    "-D-file.encoding=UTF-8",
    "-D--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
    "-D--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
    "-D--add-exports=java.base/sun.reflect.misc=ALL-UNNAMED",
    "-D--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "-D--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
    "-D--add-exports=java.base/java.lang=ALL-UNNAMED",
    "-D--add-opens=java.base/java.io=ALL-UNNAMED",
    "-D--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "-D--add-opens=java.base/java.lang=ALL-UNNAMED",
    "-D--add-opens=java.base/java.util=ALL-UNNAMED",
    "-D--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED",
)
