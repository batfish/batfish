load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(
    default_visibility = ["//docs/example_code:__subpackages__"],
)

java_library(
    name = "cool_nos",
    srcs = glob(
        ["*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cool_nos",
)
