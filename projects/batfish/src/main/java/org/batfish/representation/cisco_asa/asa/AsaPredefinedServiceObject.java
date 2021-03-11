package org.batfish.representation.cisco_asa.asa;

import javax.annotation.Nonnull;
import org.batfish.representation.cisco_asa.ServiceObject;

/** Helper class for generating definitions of ASA predefined service objects. */
public final class AsaPredefinedServiceObject {

  public static @Nonnull ServiceObject forName(String name) {
    // TODO: generate correct definitions
    return new ServiceObject(name);
  }

  private AsaPredefinedServiceObject() {}
}
