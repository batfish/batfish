package org.batfish.grammar.silent_syntax;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A collection of silent syntax elements. */
@ParametersAreNonnullByDefault
public final class SilentSyntaxCollection implements Serializable {

  /** Data for an instance of silent syntax in a parse tree. */
  @ParametersAreNonnullByDefault
  public static final class SilentSyntaxElem implements Serializable {

    public SilentSyntaxElem(String ruleName, int line, String text) {
      _ruleName = ruleName;
      _line = line;
      _text = text;
    }

    public @Nonnull String getRuleName() {
      return _ruleName;
    }

    public int getLine() {
      return _line;
    }

    public @Nonnull String getText() {
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

    private final @Nonnull String _ruleName;
    private final int _line;
    private final @Nonnull String _text;
  }

  public SilentSyntaxCollection() {
    _elements = new LinkedList<>();
  }

  public @Nonnull Collection<SilentSyntaxElem> getElements() {
    return ImmutableList.copyOf(_elements);
  }

  public void addElement(SilentSyntaxElem element) {
    _elements.add(element);
  }

  private @Nonnull List<SilentSyntaxElem> _elements;
}
