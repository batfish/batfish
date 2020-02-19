load("@rules_java//java:defs.bzl", "java_binary")

package(default_visibility = ["//visibility:public"])

exports_files(["src/main/resources/org/batfish/pmd/pmd-ruleset.xml"])

java_binary(
    name = "pmd",
    main_class = "net.sourceforge.pmd.PMD",
    runtime_deps = [
        "@maven//:net_sourceforge_pmd_pmd_java",
    ],
)
