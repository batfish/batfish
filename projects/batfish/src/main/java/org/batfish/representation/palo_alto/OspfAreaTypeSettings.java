package org.batfish.representation.palo_alto;

import java.io.Serializable;

public interface OspfAreaTypeSettings extends Serializable {
  <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor);
}
