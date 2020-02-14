package(default_visibility = [
    "//visibility:public",
])

java_library(
    name = "ip",
    srcs = glob(["*.java"]),
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

pmd_test(
    name = "pmd",
    lib = ":ip",
)
