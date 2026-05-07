// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}

// Ép Gradle đánh giá module :flutter TRƯỚC KHI kết thúc đánh giá root project
// Điều này giúp tránh lỗi Cannot run Project.afterEvaluate(Closure) when the project is already evaluated
project.evaluationDependsOn(":flutter")
