package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

public interface OspfAreaTypeSettings extends Serializable {

  <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor);
}
