package(default_visibility = ["//visibility:public"])

java_library(
    name = "action",
    srcs = [":LineAction.java"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
    ],
)

java_library(
    name = "ipv4",
    srcs = [
        ":AbstractIpSpaceContainsIp.java",
        ":AclIpSpace.java",
        ":AclIpSpaceLine.java",
        ":EmptyIpSpace.java",
        ":Ip.java",
        ":IpIpSpace.java",
        ":IpSpace.java",
        ":IpSpaceContainsIp.java",
        ":IpSpaceReference.java",
        ":IpWildcard.java",
        ":IpWildcardIpSpace.java",
        ":IpWildcardSetIpSpace.java",
        ":Prefix.java",
        ":PrefixIpSpace.java",
        ":UniverseIpSpace.java",
        ":visitors/GenericIpSpaceVisitor.java",
    ],
    deps = [
        ":action",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_github_ben_manes_caffeine_caffeine",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
    ],
)

java_library(
    name = "names",
    srcs = [":Names.java"],
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
    ],
)
