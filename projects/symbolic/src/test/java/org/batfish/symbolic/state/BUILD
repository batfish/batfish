load("@batfish//skylark:junit.bzl", "junit_tests")

package(default_visibility = ["//visibility:private"])

junit_tests(
    name = "tests",
    srcs = glob(["*.java"]),
    deps = [
        "//projects/symbolic/src/main/java/org/batfish/symbolic/state:state",
          "//projects/batfish-common-protocol:common",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_guava_guava_testlib",
        "@maven//:junit_junit",
    ],
)
