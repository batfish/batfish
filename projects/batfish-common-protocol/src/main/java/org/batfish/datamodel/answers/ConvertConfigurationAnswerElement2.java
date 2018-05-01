package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.MutablePair;

public class ConvertConfigurationAnswerElement2 extends ConvertConfigurationAnswerElement {

  // hostname -> type -> name -> [number of references, definition lines ]
  private SortedMap<
          String, SortedMap<String, SortedMap<String, MutablePair<Integer, SortedSet<Integer>>>>>
      _definedStructures;

  public ConvertConfigurationAnswerElement2() {
    _definedStructures = new TreeMap<>();
  }

  public SortedMap<
          String, SortedMap<String, SortedMap<String, MutablePair<Integer, SortedSet<Integer>>>>>
      getDefinedStructures() {
    return _definedStructures;
  }
}
