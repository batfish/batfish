load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(default_visibility = [
    "//projects/symbolic:__pkg__",
    "//projects/symbolic/src/test:__subpackages__",
])

java_library(
    name = "core",
    srcs = ["IngressLocation.java"],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@maven//:com_google_code_findbugs_jsr305",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":core",
)
