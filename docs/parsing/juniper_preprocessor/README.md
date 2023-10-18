# Juniper preprocessor
Before parsing a Juniper configuration file, the text is run through the [Juniper preprocessor](../../../projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/PreprocessJuniperExtractor.java). The preprocessing stages are detailed in the javadocs for the `preprocess` function within that file.

Roughly, the preprocessing stages build up a nested hierarchy of Juniper configuration, apply hierarchy modifications like inheritance of groups and deactivation of subtrees, then output configuration active lines based on the resulting hierarchy. See the linked documentation for more details.

