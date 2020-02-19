load("@rules_java//java:defs.bzl", "java_plugin")

package(default_visibility = ["//visibility:public"])

java_plugin(
    name = "auto_service_plugin",
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    deps = [
        "@maven//:com_google_auto_service_auto_service",
    ],
)
