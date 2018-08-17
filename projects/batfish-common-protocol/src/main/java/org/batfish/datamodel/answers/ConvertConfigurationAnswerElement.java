package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.Version;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.DefinedStructureInfo;

/**
 * Stores information extracted while converting vendor-specific configurations to
 * vendor-independent ones.
 */
public class ConvertConfigurationAnswerElement extends InitStepAnswerElement
    implements Serializable {

  /** A non-default number because we changed the class */
  private static final long serialVersionUID = 2L;

  private static final String PROP_DEFINED_STRUCTURES = "definedStructures";
  private static final String PROP_ERRORS = "errors";
  private static final String PROP_FAILED = "failed";
  private static final String PROP_UNDEFINED_REFERENCES = "undefinedReferences";
  private static final String PROP_VERSION = "version";
  private static final String PROP_WARNINGS = "warnings";

  // hostname -> structType -> structName -> info
  @Nonnull
  private SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
      _definedStructures;

  @Nonnull private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

  @Nonnull private Set<String> _failed;

  @Nonnull
  private SortedMap<
          String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
      _undefinedReferences;

  @Nonnull private String _version;

  @Nonnull private SortedMap<String, Warnings> _warnings;

  public ConvertConfigurationAnswerElement() {
    this(null, null, null, null, null, null);
  }

  @JsonCreator
  public ConvertConfigurationAnswerElement(
      @JsonProperty(PROP_DEFINED_STRUCTURES)
          SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
              definedStructures,
      @JsonProperty(PROP_ERRORS) SortedMap<String, BatfishException.BatfishStackTrace> errors,
      @JsonProperty(PROP_FAILED) SortedSet<String> failed,
      @JsonProperty(PROP_UNDEFINED_REFERENCES)
          SortedMap<
                  String,
                  SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
              undefinedReferences,
      @JsonProperty(PROP_VERSION) String version,
      @JsonProperty(PROP_WARNINGS) SortedMap<String, Warnings> warnings) {
    _definedStructures = firstNonNull(definedStructures, new TreeMap<>());
    _errors = firstNonNull(errors, new TreeMap<>());
    _failed = firstNonNull(failed, new TreeSet<>());
    _undefinedReferences = firstNonNull(undefinedReferences, new TreeMap<>());
    _version = firstNonNull(version, Version.getVersion());
    _warnings = firstNonNull(warnings, new TreeMap<>());
  }

  @JsonProperty(PROP_DEFINED_STRUCTURES)
  public SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
      getDefinedStructures() {
    return _definedStructures;
  }

  @Override
  @JsonProperty(PROP_ERRORS)
  public SortedMap<String, BatfishException.BatfishStackTrace> getErrors() {
    return _errors;
  }

  @JsonProperty(PROP_FAILED)
  public Set<String> getFailed() {
    return _failed;
  }

  @JsonProperty(PROP_UNDEFINED_REFERENCES)
  public SortedMap<
          String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
      getUndefinedReferences() {
    return _undefinedReferences;
  }

  @JsonProperty(PROP_VERSION)
  public String getVersion() {
    return _version;
  }

  @Override
  @JsonProperty(PROP_WARNINGS)
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
                          (usage, lines) ->
                              sb.append("      " + usage + ": lines " + lines + "\n"));
                    });
              });
        });
    _definedStructures.forEach(
        (hostname, byType) -> {
          sb.append("\n  " + hostname + "[Defined structures]\n");
          byType.forEach(
              (structureType, byName) ->
                  byName.forEach(
                      (name, info) -> {
                        if (info.getNumReferrers() == 0) {
                          sb.append(
                              "    "
                                  + structureType
                                  + ": "
                                  + name
                                  + ":"
                                  + info.getDefinitionLines()
                                  + "\n");
                        }
                      }));
        });
    return sb.toString();
  }

  @Override
  public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
    _errors = errors;
  }

  public void setDefinedStructures(
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
          definedStructures) {
    _definedStructures = definedStructures;
  }

  public void setUndefinedReferences(
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          undefinedReferences) {
    _undefinedReferences = undefinedReferences;
  }

  public void setVersion(String version) {
    _version = version;
  }

  @Override
  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }
}
