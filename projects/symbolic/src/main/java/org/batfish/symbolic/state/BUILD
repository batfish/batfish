load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//projects/symbolic:__pkg__"])

java_library(
    name = "state",
    srcs = glob([
        "*.java",
    ]),
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
    ],
)

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

pmd_test(
    name = "pmd",
    lib = ":state",
)
