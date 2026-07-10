package org.batfish.specifier.parse;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An element along a completion match path: an anchor type, a grammar label, the nesting level, and
 * the start index in the query. Built by the antlr4-c3 completion adapter ({@link
 * C3PotentialMatches}).
 */
@ParametersAreNonnullByDefault
final class PathElement {

  /** The anchor type of this element */
  private final @Nullable Anchor.Type _anchorType;

  /** The grammar label for this element */
  private String _label;

  /** How deep this element is from the start */
  private int _level;

  /** Where in the input buffer (query) this element starts */
  private int _startIndex;

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
