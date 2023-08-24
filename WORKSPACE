workspace(name = "batfish")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")

##########################################################
# Bazel helpers                                          #
##########################################################
http_archive(
    name = "bazel_skylib",
    sha256 = "74d544d96f4a5bb630d465ca8bbcfe231e3594e5aae57e1edbf17a6eb3ca2506",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.3.0/bazel-skylib-1.3.0.tar.gz",
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.3.0/bazel-skylib-1.3.0.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

##########################################################
# External Java dependencies (from Maven)                #
##########################################################

RULES_JVM_EXTERNAL_TAG = "5.1"

RULES_JVM_EXTERNAL_SHA = "8c3b207722e5f97f1c83311582a6c11df99226e65e2471086e296561e57cc954"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load(":library_deps.bzl", "BATFISH_MAVEN_ARTIFACTS")

maven_install(
    artifacts = BATFISH_MAVEN_ARTIFACTS,
    excluded_artifacts = ["org.hamcrest:hamcrest-core"],
    fetch_sources = True,
    maven_install_json = "@batfish//:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    strict_visibility = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

##########################################################
## GPL'ed JOL tool
load("//tools/jol:jol.bzl", "setup_jol_maven")

setup_jol_maven()

load("@jol_maven//:defs.bzl", jol_pinned_maven_install = "pinned_maven_install")

jol_pinned_maven_install()

##########################################################
## GPL'ed JMH tool
load("//tools/benchmarks:jmh.bzl", "setup_jmh_maven")

setup_jmh_maven()

load("@jmh_maven//:defs.bzl", jmh_pinned_maven_install = "pinned_maven_install")

jmh_pinned_maven_install()

##########################################################
## Python setup

http_archive(
    name = "rules_python",
    sha256 = "b593d13bb43c94ce94b483c2858e53a9b811f6f10e1e0eedc61073bd90e58d9c",
    strip_prefix = "rules_python-0.12.0",
    url = "https://github.com/bazelbuild/rules_python/archive/refs/tags/0.12.0.tar.gz",
)

load("@rules_python//python:repositories.bzl", "python_register_toolchains")

python_register_toolchains(
    name = "python3_9",
    # Available versions are listed in @rules_python//python:versions.bzl.
    python_version = "3.9",
)

load("@python3_9//:defs.bzl", "interpreter")
load("@rules_python//python:pip.bzl", "pip_parse")

pip_parse(
    name = "pip",
    python_interpreter_target = interpreter,
    requirements_lock = "//python:requirements.txt",
)

load("@pip//:requirements.bzl", "install_deps")

install_deps()

##########################################################
## Third section: tools

# ANTLR4 tool
http_jar(
    name = "antlr4_tool",
    sha256 = "6852386d7975eff29171dae002cc223251510d35f291ae277948f381a7b380b4",
    url = "https://search.maven.org/remotecontent?filepath=org/antlr/antlr4/4.7.2/antlr4-4.7.2-complete.jar",
)

# Buildifier
http_archive(
    name = "buildifier_prebuilt",
    sha256 = "95387c9dded7f8e3bdd4c598bc2ca4fbb6366cb214fa52e7d7b689eb2f421e01",
    strip_prefix = "buildifier-prebuilt-6.0.0",
    urls = [
        "http://github.com/keith/buildifier-prebuilt/archive/6.0.0.tar.gz",
    ],
)

load("@buildifier_prebuilt//:deps.bzl", "buildifier_prebuilt_deps")

buildifier_prebuilt_deps()

bazel_skylib_workspace()

load("@buildifier_prebuilt//:defs.bzl", "buildifier_prebuilt_register_toolchains")

buildifier_prebuilt_register_toolchains()
