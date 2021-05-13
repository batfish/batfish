package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AsPathSet implements Serializable {

  private @Nonnull List<AsPathSetElem> _elements;

  public AsPathSet() {
    _elements = ImmutableList.of();
  }

  public @Nonnull List<AsPathSetElem> getElements() {
    return _elements;
  }

  public void addElement(AsPathSetElem element) {
    _elements =
        ImmutableList.<AsPathSetElem>builderWithExpectedSize(_elements.size() + 1)
            .addAll(_elements)
            .add(element)
            .build();
  }
}
