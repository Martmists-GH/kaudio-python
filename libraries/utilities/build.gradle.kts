plugins {
    `cpp-library`
    `cpp-unit-test`
}

library {
    linkage.set(listOf(Linkage.STATIC))
}

unitTest {
    targetMachines.set(listOf(machines.linux.x86_64))
}

val build by tasks.getting {
    dependsOn("assembleRelease")
    dependsOn("assembleDebug")
}
