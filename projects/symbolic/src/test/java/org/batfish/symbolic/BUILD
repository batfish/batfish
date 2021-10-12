load("@batfish//skylark:junit.bzl", "junit_tests")

package(default_visibility = ["//visibility:private"])

junit_tests(
    name = "IngressLocationTest",
    srcs = ["IngressLocationTest.java"],
    deps = [
        "//projects/symbolic/src/main/java/org/batfish/symbolic:core",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_guava_guava_testlib",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
