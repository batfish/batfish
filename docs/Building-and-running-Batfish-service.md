# :warning: This is a guide for Batfish developers :warning:

**If you are interested in trying out Batfish on your network, check out our [instructions for getting started](https://pybatfish.readthedocs.io/en/latest/getting_started.html) instead.**

***

# Overview

Batfish runs as a service that can be accessed via RESTful APIs. A [Python client](https://github.com/batfish/batfish/wiki/Batfish-clients#pybatfish) comes bundled with the [suggested allinone Docker container](https://github.com/batfish/batfish#how-do-i-get-started) or can be [set up separately](https://github.com/batfish/pybatfish#how-do-i-get-started).

## Prerequisites

- Java 11 JDK
- git
- [bazelisk](https://github.com/bazelbuild/bazelisk#installation)

We provide OS-specific advice below.

* `Cygwin`: Read [Cygwin notes](#cygwin-notes) first
* `macOS`: Read [macOS notes](#macos-notes) first

## Installation steps

1. Check out the Batfish code
    - `git clone https://github.com/batfish/batfish.git`
    - `cd batfish`

2. Compile Batfish
    - `bazel build //projects/allinone:allinone_main`

3. Run the Batfish service
    - `./bazel-bin/projects/allinone/allinone_main -runclient false -coordinatorargs "-templatedirs questions" -runclient false -coordinatorargs "-templatedirs questions"` will start Batfish in service mode using the library of questions within the Batfish repository.

    Data stored by the service to analyze a network will be stored in a `containers` folder.

    If you wish to provide Java arguments, use the `--jvm_args` option:

    To set the RAM:
    ```
    --jvm_flag=-Xmx2g
    ```

    To turn on assertions and enable a debugger to attach:
    ```
    --jvm_flag=-ea --jvm_flag=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009
    ```

    For more info, see the Bazel documentation.

4. Explore using Pybatfish

    Once the service is running, you can use [Pybatfish](https://github.com/batfish/batfish/wiki/Batfish-clients#pybatfish) to analyze your network.

## Operation-system specific advice

### macOS

Do the following before doing anything

1. Install [XCode from Apple Store](https://itunes.apple.com/us/app/xcode/id497799835)

2. Install Homebrew. Follow [these steps](https://brew.sh/).

3. Open a fresh terminal to ensure the utilities are correctly picked up.

4. If you don't already have the Java 11 JDK installed, first install homebrew cask and then Java 11 using the following commands.
    - `brew tap AdoptOpenJDK/openjdk `
    - `brew install adoptopenjdk11`

5. If you don't already have it, install Bazelisk.
    - `brew install bazelisk`

### Cygwin

Do the following before building Batfish

1. Handling line-endings

    Windows uses different line endings. To handle these differences gracefully, add the following lines to the [core] section of ~/.gitconfig (global settings) or .git/config (repository settings):

        autocrlf = false