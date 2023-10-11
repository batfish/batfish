package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents one (of possibly multiple) potential matches when the parser input does not match */
@ParametersAreNonnullByDefault
class PotentialMatch {

  /** The anchor where this match hinges on and we use for auto completion */
  private final @Nonnull PathElement _anchor;

  /** What the user entered for this match */
  private final @Nonnull String _matchPrefix;

  /** The list of elements along the path we are analyzing */
  private final @Nonnull List<PathElement> _path;

  PotentialMatch(PathElement anchor, String matchPrefix, List<PathElement> path) {
    _anchor = anchor;
    _matchPrefix = matchPrefix;
    _path = path;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PotentialMatch)) {
      return false;
    }
    return Objects.equals(_anchor, ((PotentialMatch) o)._anchor)
        && Objects.equals(_matchPrefix, ((PotentialMatch) o)._matchPrefix)
        && Objects.equals(_path, ((PotentialMatch) o)._path);
  }

  PathElement getAnchor() {
    return _anchor;
  }

  Anchor.Type getAnchorType() {
    return _anchor.getAnchorType();
  }

  /**
   * Computes what input would have matched the current rule. This is knowable (and thus non-null)
   * only for rules that correspond to string and char literals.
   */
  @Nullable
  String getMatch() {
    if (getAnchorType() == STRING_LITERAL || getAnchorType() == CHAR_LITERAL) {
      // Remove quotes inserted by parboiled for completion suggestions
      String fullToken = _anchor.getLabel();
      if (fullToken.length() >= 2) { // remove surrounding quotes
        fullToken = fullToken.substring(1, fullToken.length() - 1);
      }
      return fullToken;
    }
    return null;
  }

  @Nonnull
  String getMatchPrefix() {
    return _matchPrefix;
  }

  int getMatchStartIndex() {
    return _anchor.getStartIndex();
  }

  @Nonnull
  List<PathElement> getPath() {
    return _path;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_anchor, _matchPrefix, _path);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("anchor", _anchor)
        .add("matchingPrefix", _matchPrefix)
        .add("path", _path)
        .toString();
  }
}
