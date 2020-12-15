package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A set of elements used to match the route-target extended communities in an extended community
 * attribute; or used to represent a set of such communities.
 */
@ParametersAreNonnullByDefault
public final class ExtcommunitySetRt implements Serializable {
  public ExtcommunitySetRt() {
    _elements = new ArrayList<>(1);
  }

  public ExtcommunitySetRt(List<ExtcommunitySetRtElem> elements) {
    _elements = ImmutableList.copyOf(elements);
  }

  public void addElement(@Nonnull ExtcommunitySetRtElem element) {
    _elements.add(element);
  }

  public @Nonnull List<ExtcommunitySetRtElem> getElements() {
    return ImmutableList.copyOf(_elements);
  }

  private final @Nonnull List<ExtcommunitySetRtElem> _elements;
}
