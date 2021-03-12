package org.batfish.representation.cisco_asa;

import javax.annotation.Nonnull;

/** Helper class for generating definitions of ASA predefined service objects. */
public final class AsaPredefinedServiceObject {

  public static @Nonnull ServiceObject forName(String name) {
    // TODO: generate correct definitions
    return new ServiceObject(name);
  }

  private AsaPredefinedServiceObject() {}
}
