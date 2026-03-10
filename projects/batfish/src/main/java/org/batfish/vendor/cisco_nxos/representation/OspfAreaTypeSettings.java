package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;

public interface OspfAreaTypeSettings extends Serializable {

  <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor);
}
