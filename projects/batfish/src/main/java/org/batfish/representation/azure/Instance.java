package org.batfish.representation.azure;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;

public abstract class Instance extends Resource implements Serializable {

  public Instance(@Nonnull String name, @Nonnull String id, @Nonnull String type) {
    super(name, id, type);
  }

  public abstract Configuration toConfigurationNode(
      Region rgp, ConvertedConfiguration convertedConfiguration);
}
