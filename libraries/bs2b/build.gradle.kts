plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.STATIC))
    source.from("src")
    publicHeaders.from("src")
    privateHeaders.from("src")
}

val build by tasks.getting {
    dependsOn("assembleRelease")
}
