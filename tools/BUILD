load("@rules_java//java:defs.bzl", "java_binary")

package(default_visibility = ["//visibility:public"])

java_binary(
    name = "flatten",
    main_class = "org.batfish.main.Flatten",
    runtime_deps = [
        "//projects/allinone",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)

java_binary(
    name = "preprocess_juniper",
    main_class = "org.batfish.main.PreprocessJuniper",
    runtime_deps = [
        "//projects/allinone",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
