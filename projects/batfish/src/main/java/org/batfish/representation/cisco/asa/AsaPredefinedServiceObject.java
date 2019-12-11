package org.batfish.representation.cisco.asa;

import javax.annotation.Nonnull;
import org.batfish.representation.cisco.ServiceObject;

/** Helper class for generating definitions of ASA predefined service objects. */
public final class AsaPredefinedServiceObject {

  public static @Nonnull ServiceObject forName(String name) {
    // TODO: generate correct definitions
    return new ServiceObject(name);
  }

  private AsaPredefinedServiceObject() {}
}
