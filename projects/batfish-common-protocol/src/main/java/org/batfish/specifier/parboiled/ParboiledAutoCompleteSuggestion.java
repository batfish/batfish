package org.batfish.specifier.parboiled;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.batfish.specifier.parboiled.CommonParser.SET_OP_DIFFERENCE;
import static org.batfish.specifier.parboiled.CommonParser.SET_OP_INTERSECTION;
import static org.batfish.specifier.parboiled.CommonParser.SET_OP_UNION;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.AutocompleteSuggestion.SuggestionType;

/**
 * A class that represents an auto complete suggestion generated by {@link ParboiledAutoComplete}
 */
@ParametersAreNonnullByDefault
final class ParboiledAutoCompleteSuggestion {

  static final String SET_PREFIX_DIFFERENCE = "Set difference";
  static final String SET_PREFIX_INTERSECTION = "Set intersection";
  static final String SET_PREFIX_UNION = "Set union";

  /** The anchor based on which we are auto completing. */
  private final @Nonnull Anchor.Type _anchorType;

  /** Index in the input query string where the suggestion text should be inserted */
  private final int _insertionIndex;

  /** Actual text of the suggestion */
  private final @Nonnull String _text;

  /** Short text to show the user how to complete a partial suggestion. */
  private final @Nullable String _hint;

  /** Some helpful text about what the suggestion specifies */
  private final @Nullable String _description;

  ParboiledAutoCompleteSuggestion(String text, int insertionIndex, Anchor.Type anchorType) {
    this(text, anchorType.getHint(), insertionIndex, anchorType);
  }

  ParboiledAutoCompleteSuggestion(
      String text, @Nullable String hint, int insertionIndex, Anchor.Type anchorType) {
    this(text, hint, insertionIndex, anchorType, null);
  }

  ParboiledAutoCompleteSuggestion(
      String text,
      @Nullable String hint,
      int insertionIndex,
      Anchor.Type anchorType,
      @Nullable String description) {
    _text = text;
    _hint = hint;
    _insertionIndex = insertionIndex;
    _anchorType = anchorType;
    _description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ParboiledAutoCompleteSuggestion)) {
      return false;
    }
    return Objects.equals(_anchorType, ((ParboiledAutoCompleteSuggestion) o)._anchorType)
        && Objects.equals(_description, ((ParboiledAutoCompleteSuggestion) o)._description)
        && Objects.equals(_hint, ((ParboiledAutoCompleteSuggestion) o)._hint)
        && Objects.equals(_insertionIndex, ((ParboiledAutoCompleteSuggestion) o)._insertionIndex)
        && Objects.equals(_text, ((ParboiledAutoCompleteSuggestion) o)._text);
  }

  public @Nonnull Anchor.Type getAnchorType() {
    return _anchorType;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public int getInsertionIndex() {
    return _insertionIndex;
  }

  public @Nonnull String getText() {
    return _text;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_anchorType, _description, _hint, _insertionIndex, _text);
  }

  static AutocompleteSuggestion toAutoCompleteSuggestion(
      ParboiledAutoCompleteSuggestion suggestion) {
    return AutocompleteSuggestion.builder()
        .setText(suggestion._text)
        .setInsertionIndex(suggestion._insertionIndex)
        .setDescription(
            suggestion._description != null
                ? suggestion._description
                : completeDescriptionIfNeeded(suggestion))
        .setHint(suggestion._hint != null ? suggestion._hint : suggestion._anchorType.getHint())
        .setSuggestionType(suggestion._anchorType.getSuggestionType())
        .build();
  }

  /**
   * The description of set operations is incomplete because the same anchor is used for union,
   * difference, and intersection. This function completes the description based on the suggestion
   * text.
   */
  @VisibleForTesting
  static String completeDescriptionIfNeeded(ParboiledAutoCompleteSuggestion suggestion) {
    if (suggestion._anchorType.getSuggestionType() != SuggestionType.SET_OPERATOR) {
      return suggestion._anchorType.getDescription();
    }
    switch (suggestion._text) {
      case SET_OP_DIFFERENCE:
        return SET_PREFIX_DIFFERENCE + suggestion._anchorType.getDescription();
      case SET_OP_INTERSECTION:
        return SET_PREFIX_INTERSECTION + suggestion._anchorType.getDescription();
      case SET_OP_UNION:
        return SET_PREFIX_UNION + suggestion._anchorType.getDescription();
      default:
        return suggestion._anchorType.getDescription();
    }
  }

  static List<AutocompleteSuggestion> toAutoCompleteSuggestions(
      Collection<ParboiledAutoCompleteSuggestion> suggestions) {
    return suggestions.stream()
        .map(ParboiledAutoCompleteSuggestion::toAutoCompleteSuggestion)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("text", _text)
        .add("insertionIndex", _insertionIndex)
        .add("anchorType", _anchorType)
        .toString();
  }
}
