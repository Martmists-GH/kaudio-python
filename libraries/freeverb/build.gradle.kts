plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.STATIC))
}

val build by tasks.getting {
    dependsOn("assembleRelease")
    dependsOn("assembleDebug")
}
