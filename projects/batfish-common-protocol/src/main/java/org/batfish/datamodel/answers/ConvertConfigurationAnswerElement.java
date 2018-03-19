package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.Version;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;

public class ConvertConfigurationAnswerElement extends InitStepAnswerElement
    implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

  private Set<String> _failed;

  private SortedMap<
          String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
      _undefinedReferences;

  private SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
      _unusedStructures;

  private String _version;

  private SortedMap<String, Warnings> _warnings;

  public ConvertConfigurationAnswerElement() {
    _failed = new TreeSet<>();
    _warnings = new TreeMap<>();
    _undefinedReferences = new TreeMap<>();
    _unusedStructures = new TreeMap<>();
    _errors = new TreeMap<>();
    _version = Version.getVersion();
  }

  @Override
  public SortedMap<String, BatfishException.BatfishStackTrace> getErrors() {
    return _errors;
  }

  public Set<String> getFailed() {
    return _failed;
  }

  public SortedMap<
          String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
      getUndefinedReferences() {
    return _undefinedReferences;
  }

  public SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
      getUnusedStructures() {
    return _unusedStructures;
  }

  public String getVersion() {
    return _version;
  }

  @Override
  public SortedMap<String, Warnings> getWarnings() {
    return _warnings;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder("Results from converting vendor configurations\n");
    _warnings.forEach(
        (name, warnings) -> {
          sb.append("\n  " + name + "[Conversion warnings]\n");
          for (Warning warning : warnings.getRedFlagWarnings()) {
            sb.append("    RedFlag " + warning.getTag() + " : " + warning.getText() + "\n");
          }
          for (Warning warning : warnings.getUnimplementedWarnings()) {
            sb.append("    Unimplemented " + warning.getTag() + " : " + warning.getText() + "\n");
          }
          for (Warning warning : warnings.getPedanticWarnings()) {
            sb.append("    Pedantic " + warning.getTag() + " : " + warning.getText() + "\n");
          }
        });
    _errors.forEach(
        (name, errors) -> {
          sb.append("\n  " + name + "[Conversion errors]\n");
          for (String line : errors.getLineMap()) {
            sb.append("    " + line + "\n");
          }
        });
    _undefinedReferences.forEach(
        (hostname, byType) -> {
          sb.append("\n  " + hostname + "[Undefined references]\n");
          byType.forEach(
              (type, byName) -> {
                sb.append("  " + type + ":\n");
                byName.forEach(
                    (name, byUsage) -> {
                      sb.append("    " + name + ":\n");
                      byUsage.forEach(
                          (usage, lines) -> {
                            sb.append("      " + usage + ": lines " + lines + "\n");
                          });
                    });
              });
        });
    _unusedStructures.forEach(
        (hostname, byType) -> {
          sb.append("\n  " + hostname + "[Unused structures]\n");
          byType.forEach(
              (structureType, byName) -> {
                byName.forEach(
                    (name, lines) -> {
                      sb.append("    " + structureType + ": " + name + ":" + lines + "\n");
                    });
              });
        });
    return sb.toString();
  }

  @Override
  public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
    _errors = errors;
  }

  public void setFailed(Set<String> failed) {
    _failed = failed;
  }

  public void setUndefinedReferences(
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          undefinedReferences) {
    _undefinedReferences = undefinedReferences;
  }

  public void setUnusedStructures(
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
          unusedStructures) {
    _unusedStructures = unusedStructures;
  }

  public void setVersion(String version) {
    _version = version;
  }

  @Override
  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }
}
