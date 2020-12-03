load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "hierarchical",
    srcs = glob(
        ["*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":hierarchical",
)
