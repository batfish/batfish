package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.batfish.specifier.parboiled.ParserUtils.isCharLiteralLabel;
import static org.batfish.specifier.parboiled.ParserUtils.isStringLiteralLabel;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.support.MatcherPath.Element;

/** Captures elements of that matched the input or failed to match for invalid input */
@ParametersAreNonnullByDefault
final class PathElement {

  /** The anchor type of this element */
  private final @Nullable Anchor.Type _anchorType;

  /** The parboiled label for this element */
  private String _label;

  /** How deep this element is from the start */
  private int _level;

  /** Where in the input buffer (query) this element starts */
  private int _startIndex;

  static PathElement create(Element element, Map<String, Type> anchorTypes) {
    String label = element.matcher.getLabel();
    Anchor.Type anchorType =
        anchorTypes.containsKey(label)
            ? anchorTypes.get(label)
            : isStringLiteralLabel(label)
                ? STRING_LITERAL
                : isCharLiteralLabel(label) ? CHAR_LITERAL : null;

    return new PathElement(anchorType, label, element.level, element.startIndex);
  }

  PathElement(@Nullable Anchor.Type anchorType, String label, int level, int startIndex) {
    _anchorType = anchorType;
    _label = label;
    _level = level;
    _startIndex = startIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PathElement)) {
      return false;
    }
    PathElement that = (PathElement) o;
    return _anchorType == that._anchorType
        && Objects.equals(_label, that._label)
        && _level == that._level
        && _startIndex == that._startIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_anchorType, _label, _level, _startIndex);
  }

  @Nullable
  Anchor.Type getAnchorType() {
    return _anchorType;
  }

  @Nonnull
  String getLabel() {
    return _label;
  }

  int getLevel() {
    return _level;
  }

  int getStartIndex() {
    return _startIndex;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("anchorType", _anchorType)
        .add("label", _label)
        .add("level", _level)
        .add("startIndex", _startIndex)
        .toString();
  }
}
