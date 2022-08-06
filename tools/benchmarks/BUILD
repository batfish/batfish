load(":jmh.bzl", "jmh_java_benchmarks")
load("@rules_java//java:defs.bzl", "java_plugin")

package(default_visibility = ["//tools/benchmarks:__subpackages__"])

java_plugin(
    name = "jmh_annotation_processor",
    processor_class = "org.openjdk.jmh.generators.BenchmarkProcessor",
    deps = ["@jmh_maven//:org_openjdk_jmh_jmh_generator_annprocess"],
)

jmh_java_benchmarks(
    name = "ipWildcardToBdd",
    srcs = ["BenchmarkIpWildcardToBdd.java"],
    deps = [
        "//projects/allinone",
        "//projects/bdd",
        "//projects/batfish-common-protocol:common",
         "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    ],
)

