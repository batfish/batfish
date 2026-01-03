package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an auto complete suggestion for user input */
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

  /**
   * The type of a suggestion which provides more context beyond the suggestion text.
   *
   * <p>The enums here should be kept ordered based on which types we want the user to see first.
   */
  public enum SuggestionType {
    /** Constant enum strings that are network independent, e.g., protocol types */
    CONSTANT,
    /** Represents IP addresses and related things such as ranges and prefix lengths */
    ADDRESS_LITERAL,
    /** Names of objects in the network, e.g., nodes, interfaces, address groups */
    NAME_LITERAL,
    /** Operator that ends a (sub) expression, e.g., ']' in [interface] and ')' in @role(a, b) */
    OPERATOR_END,
    /** Operator with follow-on operands, e.g., node*[*eth], @role(a*,* b), and *!* TCP */
    OPERATOR_NON_END,
    /** Parenthesis to enclose a (sub) expression, e.g., '(' in (node) */
    OPEN_PARENS,
    /** Indicates a regex */
    REGEX,
    /** A function, e.g., @enter( and @in( */
    FUNCTION,
    /** Operators that indicates set functions, e.g., union and intersection */
    SET_OPERATOR,
    /** We don't know or used for backward compatibility as the default */
    UNKNOWN
  }

  @ParametersAreNonnullByDefault
  public static final class Builder {
    private @Nullable String _description;
    private @Nullable String _hint;
    private int _insertionIndex;
    private boolean _isPartial;
    private int _rank;
    private SuggestionType _suggestionType;
    private String _text;

    private Builder() {}

    public Builder(AutocompleteSuggestion suggestion) {
      _description = suggestion.getDescription();
      _hint = suggestion.getHint();
      _insertionIndex = suggestion.getInsertionIndex();
      _isPartial = suggestion.getIsPartial();
      _rank = suggestion.getRank();
      _text = suggestion.getText();
    }

    public Builder setDescription(@Nullable String description) {
      _description = description;
      return this;
    }

    public Builder setHint(@Nullable String hint) {
      _hint = hint;
      return this;
    }

    public Builder setInsertionIndex(int insertionIndex) {
      _insertionIndex = insertionIndex;
      return this;
    }

    public Builder setIsPartial(boolean isPartial) {
      _isPartial = isPartial;
      return this;
    }

    public Builder setRank(int rank) {
      _rank = rank;
      return this;
    }

    public Builder setSuggestionType(SuggestionType suggestionType) {
      _suggestionType = suggestionType;
      return this;
    }

    public Builder setText(String text) {
      _text = text;
      return this;
    }

    public AutocompleteSuggestion build() {
      return new AutocompleteSuggestion(
          _text, _suggestionType, _isPartial, _description, _rank, _insertionIndex, _hint);
    }
  }

  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_HINT = "hint";
  private static final String PROP_INSERTION_INDEX = "insertionIndex";
  private static final String PROP_IS_PARTIAL = "isPartial";
  private static final String PROP_RANK = "rank";
  private static final String PROP_SUGGESTION_TYPE = "suggestionType";
  private static final String PROP_TEXT = "text";

  public static final int DEFAULT_RANK = Integer.MAX_VALUE;

  /** Some helpful text about what the suggestion specifies */
  private final @Nullable String _description;

  /**
   * Short text to show the user how to complete a partial suggestion. Should be provided for every
   * partial suggestion.
   */
  private final @Nullable String _hint;

  /** Index in the input query string where the suggestion text should be inserted */
  private final int _insertionIndex;

  /**
   * True if the suggestion text is only partially valid and requires additional input to become
   * valid
   */
  private final boolean _isPartial;

  /** Relevance of the suggestion relative to other suggestions */
  private final int _rank;

  /** The type of this suggestion */
  private final @Nonnull SuggestionType _suggestionType;

  /** Actual text of the suggestion */
  private final @Nonnull String _text;

  @JsonCreator
  private static @Nonnull AutocompleteSuggestion create(
      @JsonProperty(PROP_TEXT) @Nullable String text,
      @JsonProperty(PROP_SUGGESTION_TYPE) @Nullable SuggestionType suggestionType,
      @JsonProperty(PROP_IS_PARTIAL) boolean isPartial,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_RANK) int rank,
      @JsonProperty(PROP_INSERTION_INDEX) int insertionIndex,
      @JsonProperty(PROP_HINT) @Nullable String hint) {
    return new AutocompleteSuggestion(
        firstNonNull(text, ""),
        firstNonNull(suggestionType, SuggestionType.UNKNOWN),
        isPartial,
        description,
        rank,
        insertionIndex,
        hint);
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
    this(text, SuggestionType.UNKNOWN, isPartial, description, rank, insertionIndex, null);
  }

  public AutocompleteSuggestion(
      String text,
      SuggestionType suggestionType,
      boolean isPartial,
      @Nullable String description,
      int rank,
      int insertionIndex,
      @Nullable String hint) {
    _text = text;
    _suggestionType = suggestionType;
    _isPartial = isPartial;
    _description = description;
    _rank = rank;
    _insertionIndex = insertionIndex;
    _hint = hint;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(AutocompleteSuggestion suggestion) {
    return new Builder(suggestion);
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
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_HINT)
  public @Nullable String getHint() {
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

  public @Nonnull SuggestionType getSuggestionType() {
    return _suggestionType;
  }

  @JsonProperty(PROP_TEXT)
  public @Nonnull String getText() {
    return _text;
  }

  @Override
  public int hashCode() {
    // ignore rank and description
    return Objects.hash(_isPartial, _text, _insertionIndex);
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
