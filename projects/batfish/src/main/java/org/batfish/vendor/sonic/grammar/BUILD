load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "grammar",
    srcs = glob(
        ["**/*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/antlr4/org/batfish/vendor/sonic:sonic",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/frr",
        "//projects/batfish/src/main/java/org/batfish/grammar/frr:frr",
        "//projects/batfish/src/main/java/org/batfish/representation/frr:frr",
        "//projects/batfish/src/main/java/org/batfish/vendor/sonic/representation",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":sonic",
)
