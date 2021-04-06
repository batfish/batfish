package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A set of elements used to match a standard community attribute or represent of set of standard
 * communities.
 */
@ParametersAreNonnullByDefault
public final class XrCommunitySet implements Serializable {

  public XrCommunitySet(List<XrCommunitySetElem> elements) {
    _elements = ImmutableList.copyOf(elements);
  }

  public @Nonnull List<XrCommunitySetElem> getElements() {
    return _elements;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof XrCommunitySet)) {
      return false;
    }
    XrCommunitySet that = (XrCommunitySet) o;
    return _elements.equals(that._elements);
  }

  @Override
  public int hashCode() {
    return _elements.hashCode();
  }

  private final @Nonnull List<XrCommunitySetElem> _elements;
}
