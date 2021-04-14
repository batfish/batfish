package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A set of elements used to match the route distinguisher of an NLRI. */
@ParametersAreNonnullByDefault
public final class RdSet implements Serializable {

  public RdSet(Iterable<RdSetElem> elements) {
    _elements = ImmutableList.copyOf(elements);
  }

  public @Nonnull List<RdSetElem> getElements() {
    return _elements;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSet)) {
      return false;
    }
    RdSet that = (RdSet) o;
    return _elements.equals(that._elements);
  }

  @Override
  public int hashCode() {
    return _elements.hashCode();
  }

  private final @Nonnull List<RdSetElem> _elements;
}
