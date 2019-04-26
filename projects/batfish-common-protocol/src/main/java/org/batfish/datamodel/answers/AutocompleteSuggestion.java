package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class AutocompleteSuggestion {

  public enum CompletionType {
    BGP_PEER_PROPERTY,
    BGP_PROCESS_PROPERTY,
    INTERFACE_PROPERTY,
    NAMED_STRUCTURE,
    NODE,
    NODE_PROPERTY,
    OSPF_PROPERTY
  }

  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_HINT = "hint";
  private static final String PROP_INSERTION_INDEX = "insertionIndex";
  private static final String PROP_IS_PARTIAL = "isPartial";
  private static final String PROP_RANK = "rank";
  private static final String PROP_TEXT = "text";

  public static final int DEFAULT_RANK = Integer.MAX_VALUE;

  /** Some helpful text about what the suggestion specifies */
  @Nullable private final String _description;

  /**
   * Short text to show the user how to complete a partial suggestion. Should be provided for every
   * partial suggestion.
   */
  @Nullable private final String _hint;

  /** Index in the input query string where the suggestion text should be inserted */
  private final int _insertionIndex;

  /**
   * True if the suggestion text is only partially valid and requires additional input to become
   * valid
   */
  private final boolean _isPartial;

  /** Relevance of the suggestion relative to other suggestions */
  private int _rank;

  /** Actual text of the suggestion */
  @Nonnull private final String _text;

  @JsonCreator
  private static @Nonnull AutocompleteSuggestion create(
      @Nullable @JsonProperty(PROP_TEXT) String text,
      @JsonProperty(PROP_IS_PARTIAL) boolean isPartial,
      @Nullable @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_RANK) int rank,
      @JsonProperty(PROP_INSERTION_INDEX) int insertionIndex,
      @Nullable @JsonProperty(PROP_HINT) String hint) {
    return new AutocompleteSuggestion(
        firstNonNull(text, ""), isPartial, description, rank, insertionIndex, hint);
  }

  public AutocompleteSuggestion(String text, boolean isPartial) {
    this(text, isPartial, null, DEFAULT_RANK);
  }

  public AutocompleteSuggestion(String text, boolean isPartial, @Nullable String description) {
    this(text, isPartial, description, DEFAULT_RANK);
  }

  public AutocompleteSuggestion(
      String text, boolean isPartial, @Nullable String description, int rank) {
    this(text, isPartial, description, rank, 0);
  }

  public AutocompleteSuggestion(
      String text, boolean isPartial, @Nullable String description, int rank, int insertionIndex) {
    this(text, isPartial, description, rank, insertionIndex, null);
  }

  public AutocompleteSuggestion(
      String text,
      boolean isPartial,
      @Nullable String description,
      int rank,
      int insertionIndex,
      @Nullable String hint) {
    _text = text;
    _isPartial = isPartial;
    _description = description;
    _rank = rank;
    _insertionIndex = insertionIndex;
    _hint = hint;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AutocompleteSuggestion)) {
      return false;
    }
    // ignore rank and description
    return Objects.equals(_isPartial, ((AutocompleteSuggestion) o)._isPartial)
        && Objects.equals(_text, ((AutocompleteSuggestion) o)._text)
        && Objects.equals(_insertionIndex, ((AutocompleteSuggestion) o)._insertionIndex)
        && Objects.equals(_hint, ((AutocompleteSuggestion) o)._hint);
  }

  @JsonProperty(PROP_DESCRIPTION)
  @Nullable
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_HINT)
  @Nullable
  public String getHint() {
    return _hint;
  }

  @JsonProperty(PROP_INSERTION_INDEX)
  public int getInsertionIndex() {
    return _insertionIndex;
  }

  @JsonProperty(PROP_IS_PARTIAL)
  public boolean getIsPartial() {
    return _isPartial;
  }

  @JsonProperty(PROP_RANK)
  public int getRank() {
    return _rank;
  }

  @JsonProperty(PROP_TEXT)
  @Nonnull
  public String getText() {
    return _text;
  }

  @Override
  public int hashCode() {
    // ignore rank and description
    return Objects.hash(_isPartial, _text, _insertionIndex);
  }

  public void setRank(int rank) {
    _rank = rank;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_HINT, _hint)
        .add(PROP_INSERTION_INDEX, _insertionIndex)
        .add(PROP_IS_PARTIAL, _isPartial)
        .add(PROP_RANK, _rank)
        .add(PROP_TEXT, _text)
        .toString();
  }
}
