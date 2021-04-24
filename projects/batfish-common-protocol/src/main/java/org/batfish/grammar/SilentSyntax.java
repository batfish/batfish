package org.batfish.grammar;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A collection of silent syntax elements. */
@ParametersAreNonnullByDefault
public final class SilentSyntax {

  public SilentSyntax() {
    _elements = new LinkedList<>();
  }

  @Nonnull
  public Collection<SilentSyntaxElem> getElements() {
    return ImmutableList.copyOf(_elements);
  }

  public void addElement(SilentSyntaxElem element) {
    _elements.add(element);
  }

  @Nonnull private List<SilentSyntaxElem> _elements;
}
