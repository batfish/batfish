package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class AutocompleteQueryMetadata {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_EXPANSIONS = "expansions";
  private static final String PROP_IS_VALID = "isValid";

  /**
   * Commentary on the current input. Could be a message to show the user why the input is invalid
   * or could describe what is specified by the current input if it is valid
   */
  @Nullable private final String _description;

  /** List of everything specified by the input */
  @Nullable private final List<String> _expansions;

  /** Whether the current input is valid or not */
  private final boolean _isValid;

  @JsonCreator
  private static @Nonnull AutocompleteQueryMetadata create(
      @JsonProperty(PROP_IS_VALID) boolean isValid,
      @Nullable @JsonProperty(PROP_DESCRIPTION) String description,
      @Nullable @JsonProperty(PROP_EXPANSIONS) List<String> expansions) {
    return new AutocompleteQueryMetadata(isValid, description, expansions);
  }

  public AutocompleteQueryMetadata(
      boolean isValid, @Nullable String description, @Nullable List<String> expansions) {
    _isValid = isValid;
    _description = description;
    _expansions = expansions;
  }

  @JsonProperty(PROP_DESCRIPTION)
  @Nullable
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_EXPANSIONS)
  @Nullable
  public List<String> getExpansions() {
    return _expansions;
  }

  @JsonProperty(PROP_IS_VALID)
  public boolean getIsValid() {
    return _isValid;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AutocompleteQueryMetadata)) {
      return false;
    }

    return Objects.equals(_description, ((AutocompleteQueryMetadata) o)._description)
        && Objects.equals(_expansions, ((AutocompleteQueryMetadata) o)._expansions)
        && Objects.equals(_isValid, ((AutocompleteQueryMetadata) o)._isValid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _expansions, _isValid);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_EXPANSIONS, _expansions)
        .add(PROP_IS_VALID, _isValid)
        .toString();
  }
}
