# Adding support for a new vendor or feature

## Adding a new vendor

Detailed notes on adding parsing support for a new configuration format are [here](https://docs.google.com/document/d/1ikGAh5LT4RGAycDLWe92l_EOS5-HclQ6I56VZMc3hws/edit#);
an outline of the steps is below.

Assume that your new format is Netscreen.  The instructions below are high-level; look at existing examples for more guidance; Mrv is a particularly simple example, and Cisco is a particularly complex one.

1. Write grammar files (*.g4) in src/main/antlr4/org/batfish/grammar/netscreen. You should create at least two files, NetscreenLexer.g4 and NetscreenParser.g4, which correspond to the lexer and the parser. For modularity, portions of parsing can be included in other files that are imported by the main file.

2. In src/main/java/org/batfish/representation/netscreen, create a class called NetscreenConfiguration that extends VendorConfiguration. This class, which can be sparse in the beginning, should eventually capture the vendor-specific data model and have a method called toVendorIndependentConfiguration() to convert it to vendor-independent data model.

3. Compile (e.g., using batfish_build_all), which will generate the necessary Java files needed for steps below.

4. Create NetscreenCombinedParser and NetscreenControlPlaneExtractor classes in src/main/java/org/batfish/grammar/netscreen. The former is mostly boiler plate. The latter is where you are pulling out the information recovered by the parser and storing in the (vendor-specific data model of) a NetscreenConfiguration object.

5. Add NETSCREEN to the enum of configuration formats in ConfigurationFormat.java

6. In VendorConfigurationFormatDetector.java, add a function checkNetscreen() that is able to judge if a given file is Netscreen based on its content and call this function from identifyConfigurationFormat().

7. Add a case for your format to the switch statement in the call() function of ParseVendorConfigurationJob.java.

That is it; you are done now. If you did everything right, given a file with Netscreen format, it should be parsed and converted to vendor-specific data model and then to the vendor-independent data model. (As with any complex task, we recommend that doing the bare minimum in each code file and get the pipeline working on a very simple file and then extend the bits.)

## Adding new information to the datamodel for an existing vendor

Batfish focuses on parsing information that it needs for its analysis. Its vendor-independent datamodel thus may not have all the information that the configuration files contain.  This section explains how to add new information to the datamodel using interface MTU for Cisco files as an example.

1. Enhance the Cisco parser to parse interface MTUs. The entry point for parsing Cisco files is org.batfish.grammar.cisco.CiscoParser.g4. The grammar files are in the Antlr framework.

Compile the code so that the java files that are auto-generated (by Antlr) are generated.

2. Extend the vendor-specific representation in package org.batfish.representation.cisco to include new information. The relevant file for our MTU example is Interface.java.

3. Assuming the grammar rule for parsing the MTU is mtu_if_stanza, create the following function in CiscoControlPlaneExtractor.java.

```java
   @Override
   public void exitMtu_if_stanza(Mtu_if_stanzaContext ctx) {
       // 1. recover the necessary information from the context
       // 2. add it to vendor representation object
       // see other functions as example
   }
```

4. Extend the vendor-independent datamodel in package in org.batfish.datamodel to include the new information. The relevant file for our MTU example is Interface.java.

5. Pass the new information from vendor-specific representation to vendor-independent datamodel. The starting point here is toVendorIndependentConfiguration() in org.batfish.representation.cisco.CiscoConfiguration.java.

6. Add proper Json annotations to ensure that the new information is serialized and deserialized properly. Look at what is being done for other fields.
