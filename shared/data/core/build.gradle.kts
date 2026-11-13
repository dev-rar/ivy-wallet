plugins {
    id("ivy.feature")
    id("ivy.room")
    id("ivy.integration.testing")
}

android {
    namespace = "com.ivy.data"
}

dependencies {
    implementation(projects.shared.base)
    api(projects.shared.data.model)

    api(libs.datastore)
    implementation(libs.bundles.ktor)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.google.play.billing)

    testImplementation(projects.shared.data.modelTesting)
    androidTestImplementation(libs.bundles.integration.testing)
}
