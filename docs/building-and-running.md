# :warning: This is a guide for Batfish developers :warning:

**If you are interested in trying out Batfish on your network, check out
our [instructions for getting started](https://pybatfish.readthedocs.io/en/latest/getting_started.html)
instead.**

***

# Overview

Batfish runs as a service that can be accessed via RESTful APIs.
A [Python client](https://github.com/batfish/pybatfish) comes bundled
with
the [suggested allinone Docker container](https://github.com/batfish/batfish#how-do-i-get-started)
or can be [set up separately](https://github.com/batfish/pybatfish#how-do-i-get-started).

## Prerequisites

- Java 11 JDK
- git
- [bazelisk](https://github.com/bazelbuild/bazelisk#installation)

We provide OS-specific advice below.

* `Ubuntu`: Read [Ubuntu notes](#ubuntu) first
* `macOS`: Read [macOS notes](#macos) first

## Installation steps

1. Check out the Batfish code
    - `git clone https://github.com/batfish/batfish.git`
    - `cd batfish`

2. Compile Batfish
    - `bazel build //projects/allinone:allinone_main`

3. Run the Batfish service
    - ```
      bazel run //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
      ```
      will start Batfish in service mode using the library of questions within the Batfish
      repository.

   Data stored by the service to analyze a network will be stored in a `containers` folder.

   If you wish to provide Java arguments, use the `--jvmopt` option once for each java argument in
   between `run` and `//projects/allinone:allinone_main`:

   To set the RAM:
    ```
    bazel run --jvmopt=-Xmx2g //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
    ```

   To turn on assertions and enable a debugger to attach:
    ```
    bazel run --jvmopt=-ea --jvmopt=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
    ```

   For more info, see the Bazel documentation.

4. Explore using Pybatfish

   Once the service is running, you can
   use [Pybatfish](https://github.com/batfish/pybatfish) to analyze
   your network.

## Operation-system specific advice

### macOS

Do the following before doing anything

1. Install [XCode from Apple Store](https://itunes.apple.com/us/app/xcode/id497799835)

2. Install XCode command-line tools.
    - `xcode-select --install`

3. Install Homebrew. Follow [these steps](https://brew.sh/).

3. Open a fresh terminal to ensure the utilities are correctly picked up.

4. If you don't already have the Java 11 JDK installed, first install homebrew cask and then Java 11
   using the following commands.
    - `brew tap homebrew/cask-versions`
    - `brew install --cask temurin11`

5. If you don't already have it, install Bazelisk.
    - `brew install bazelisk`

### Ubuntu

Do the following before doing anything

1. Install Java 11 and corresponding debug symbols
    - `sudo apt install openjdk-11-jdk openjdk-11-dbg`

2. If you don't already have it, install Bazelisk.
    - Open the [bazelisk release page](https://github.com/bazelbuild/bazelisk/releases)
    - Copy the link for the `bazelisk-linux-amd64` release asset for the latest version
    - Download and install bazelisk using the copied link (example url below is for `v1.12.2`, but
      you should use the latest version):

      `curl -s https://github.com/bazelbuild/bazelisk/releases/download/v1.12.2/bazelisk-linux-amd64 | sudo tee /usr/local/bin/bazelisk`
    - Make bazelisk executable:

      `sudo chmod +x /usr/local/bin/bazelisk`
    - Symlink bazel to bazelisk:

      `sudo ln -s bazelisk /usr/local/bin/bazel`
