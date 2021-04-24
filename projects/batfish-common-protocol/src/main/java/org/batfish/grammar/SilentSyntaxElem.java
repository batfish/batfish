package org.batfish.grammar;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Data for an instance of silent syntax in a parse tree. */
@ParametersAreNonnullByDefault
public final class SilentSyntaxElem {

  public SilentSyntaxElem(String ruleName, int line, String text) {
    _ruleName = ruleName;
    _line = line;
    _text = text;
  }

  @Nonnull
  public String getRuleName() {
    return _ruleName;
  }

  public int getLine() {
    return _line;
  }

  @Nonnull
  public String getText() {
    return _text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SilentSyntaxElem)) {
      return false;
    }
    SilentSyntaxElem that = (SilentSyntaxElem) o;
    return _line == that._line && _ruleName.equals(that._ruleName) && _text.equals(that._text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ruleName, _line, _text);
  }

  @Nonnull private final String _ruleName;
  private final int _line;
  @Nonnull private final String _text;
}
