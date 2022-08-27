load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "role",
    srcs = glob(["**/*.java"]),
    deps = [
        "//projects/common/src/main/java/org/batfish/datamodel:names",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
    ],
)

pmd_test(
    name = "role_pmd",
    lib = ":role",
)
