Except where otherwise noted, everything in this repository is provided under the terms of the Apache 2.0 License.

[Homepage] (http://www.batfish.org)

[Sources] (http://github.com/arifogel/batfish)

##Instructions for building and running Batfish

######Cygwin: Read README.CYGWIN before continuing
######OSX: Read README.OSX before continuing

###Prerequisites:
  - ant
  - Java 8 JDK
  - python
  - z3

####Optional:
   JProfiler - a Java profiler - located at http://www.ej-technologies.com/products/jprofiler/overview.html

### Installation steps:

*After any step involving modifications to .bashrc or equivalent, make sure to reload your shell or take other action to load the changes.*

1. Install z3 - use master branch
  - git clone https://github.com/Z3Prover/z3
  - cd z3
  - python scripts/mk_make.py --java
  - cd build
  - make -j<number-of-jobs>
  - make install # as administrator (sudo or whatever)

2. Clone batfish
  - git clone https://github.com/arifogel/batfish.git

3. Prepare your environment by adding the following to your .bashrc or equivalent
  - source <batfish-root>/tools/batfish_functions.sh

    Sourcing batfish_functions.sh will give your shell access to batfish functions. You may prefer to source it manually if you do not want to clutter up your environment in every interactive bash session.

4. Compile batfish
  - batfish_build_all

    This command runs 'ant' in each batfish project directory with corresponding args. You can clean all generated output with 'batfish_build_all distclean'.

5. Run tests from the root of the batfish repository
  - allinone -cmdfile tests/commands

6. Run the batfish service locally
  - allinone -runmode interactive

    This command also brings up an interactive prompt for the Java client.

7. Explore using a client
   - See demo-java/commands for the Java client
   - See demo-python/commands.py for the python client

     These files operate on example configurations in the test_rigs folder.