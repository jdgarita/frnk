plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.platform"
    }
}