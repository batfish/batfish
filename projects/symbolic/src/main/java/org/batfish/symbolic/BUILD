package(default_visibility = [
    "//projects/symbolic:__pkg__",
    "//projects/symbolic/src/test:__subpackages__",
])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_library(
    name = "core",
    srcs = ["IngressLocation.java"],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@jackson_annotations//:compile",
        "@jsr305//:compile",
    ],
)
