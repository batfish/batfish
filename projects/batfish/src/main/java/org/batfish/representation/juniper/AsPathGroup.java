package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AsPathGroup implements Serializable {

  private Map<String, NamedAsPath> _asPaths;

  private final String _name;

  public AsPathGroup(String name) {
    _name = name;
    _asPaths = new TreeMap<>();
  }

  public @Nonnull Map<String, NamedAsPath> getAsPaths() {
    return _asPaths;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
