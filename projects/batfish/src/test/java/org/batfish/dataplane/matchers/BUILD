package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "matchers",
    srcs = glob(
        ["*.java"],
    ),
    deps = [
        "//projects/batfish",
        "//projects/batfish-common-protocol:common",
        "@maven//:org_hamcrest_hamcrest",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":matchers",
)
