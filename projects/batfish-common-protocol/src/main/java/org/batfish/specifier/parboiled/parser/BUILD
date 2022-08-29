load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "parser",
    srcs = glob(["**/*.java"]),
    deps = [
        "//projects/batfish-common-protocol/src/main/java/org/batfish/role",
        "//projects/common/src/main/java:common",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:org_parboiled_parboiled_core",
        "@maven//:org_parboiled_parboiled_java",
    ],
)
