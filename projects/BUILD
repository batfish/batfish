load("@batfish//skylark:javadoc.bzl", "java_doc")

exports_files(["pom.xml"])

java_doc(
    name = "javadoc",
    libs = [
        "//projects/batfish-client:client",
        "//projects/batfish-common-protocol:common",
        "//projects/coordinator:coordinator",
        "//projects/batfish:batfish",
        "//projects/allinone:allinone",
        "//projects/question:question",
    ],
    pkgs = [
        "org.batfish",
    ],
    tags = ["manual"],
)
