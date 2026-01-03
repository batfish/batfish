package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A class that captures the results of running input validation on an input */
@ParametersAreNonnullByDefault
public final class InputValidationNotes {

  /** The overall status of the input */
  public enum Validity {
    /** Syntactically valid but parts of the input do not match anything */
    NO_MATCH,
    /** Syntactically invalid */
    INVALID,
    /** Syntactically valid and may match things */
    VALID
  }

  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_ERROR_INDEX = "errorIndex";
  private static final String PROP_EXPANSIONS = "expansions";
  private static final String PROP_VALIDITY = "validity";

  /**
   * Commentary on the current input. Could be a message to show the user why the input is invalid
   * or could describe what is specified by the current input if it is valid
   */
  private final @Nullable String _description;

  /** For invalid input, where the error begins */
  private final @Nullable Integer _errorIndex;

  /** List of everything specified by the input */
  private final @Nullable List<String> _expansions;

  /** Whether the current input is valid or not */
  private final Validity _validity;

  @JsonCreator
  private static @Nonnull InputValidationNotes create(
      @JsonProperty(PROP_VALIDITY) Validity validity,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_ERROR_INDEX) @Nullable Integer errorIndex,
      @JsonProperty(PROP_EXPANSIONS) @Nullable List<String> expansions) {
    return new InputValidationNotes(validity, description, errorIndex, expansions);
  }

  public InputValidationNotes(Validity validity, String description) {
    this(validity, description, -1, null);
  }

  public InputValidationNotes(Validity validity, List<String> expansions) {
    this(validity, null, null, expansions);
  }

  public InputValidationNotes(Validity validity, String description, @Nullable Integer errorIndex) {
    this(validity, description, errorIndex, null);
  }

  public InputValidationNotes(
      Validity validity,
      @Nullable String description,
      @Nullable Integer errorIndex,
      @Nullable List<String> expansions) {
    _validity = validity;
    _description = description;
    _errorIndex = errorIndex;
    _expansions = expansions;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_ERROR_INDEX)
  public @Nullable Integer getErrorIndex() {
    return _errorIndex;
  }

  @JsonProperty(PROP_EXPANSIONS)
  public @Nullable List<String> getExpansions() {
    return _expansions;
  }

  @JsonProperty(PROP_VALIDITY)
  public Validity getValidity() {
    return _validity;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InputValidationNotes)) {
      return false;
    }

    return Objects.equals(_description, ((InputValidationNotes) o)._description)
        && Objects.equals(_errorIndex, ((InputValidationNotes) o)._errorIndex)
        && Objects.equals(_expansions, ((InputValidationNotes) o)._expansions)
        && Objects.equals(_validity, ((InputValidationNotes) o)._validity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _errorIndex, _expansions, _validity);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_ERROR_INDEX, _errorIndex)
        .add(PROP_EXPANSIONS, _expansions)
        .add(PROP_VALIDITY, _validity)
        .toString();
  }
}
