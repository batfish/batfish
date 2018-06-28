package org.batfish.question.definedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;

public class DefinedStructureRow implements Comparable<DefinedStructureRow> {

  @Nonnull private final SortedSet<Integer> _definitionLines;

  @Nonnull private final String _nodeName;

  @Nonnull private final String _structType;

  @Nonnull private final String _structName;

  private final int _numReferences;

  public DefinedStructureRow(
      String nodeName,
      String structType,
      String structName,
      Integer numReferences,
      SortedSet<Integer> definitionLines) {
    if (nodeName == null) {
      throw new IllegalArgumentException(
          "Cannot initialize DefinedStructureInfo: nodeName is null");
    }
    if (structType == null) {
      throw new IllegalArgumentException(
          "Cannot initialize DefinedStructureInfo: structType is null");
    }
    if (structName == null) {
      throw new IllegalArgumentException(
          "Cannot initialize DefinedStructureInfo: structName is null");
    }
    _nodeName = nodeName;
    _structType = structType;
    _structName = structName;
    _numReferences = numReferences == null ? -1 : numReferences;
    _definitionLines = firstNonNull(definitionLines, new TreeSet<>());
  }

  public SortedSet<Integer> getDefinitionLines() {
    return _definitionLines;
  }

  public String getNodeName() {
    return _nodeName;
  }

  public int getNumReferences() {
    return _numReferences;
  }

  public String getStructName() {
    return _nodeName;
  }

  public String getStructType() {
    return _nodeName;
  }

  @Override
  public int compareTo(@Nonnull DefinedStructureRow o) {
    return Comparator.comparing(DefinedStructureRow::getNodeName)
        .thenComparing(DefinedStructureRow::getStructType)
        .thenComparing(DefinedStructureRow::getStructName)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DefinedStructureRow)) {
      return false;
    }
    DefinedStructureRow other = (DefinedStructureRow) o;
    return Objects.equals(_definitionLines, other._definitionLines)
        && Objects.equals(_nodeName, other._nodeName)
        && Objects.equals(_numReferences, other._numReferences)
        && Objects.equals(_structName, other._structName)
        && Objects.equals(_structType, other._structType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_definitionLines, _nodeName, _numReferences, _structName, _structType);
  }

  @Override
  public String toString() {
    return String.format(
        "node=%s structType=%s structName=%s numRefs=%d defLines=%s",
        _nodeName, _structType, _structType, _numReferences, _definitionLines);
  }
}
