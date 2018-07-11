package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.List;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public interface ServiceGroupMember extends Serializable {
  /**
   * Add the ServiceGroupMember match conditions with the specified action as a new IpAccessListLine
   * to the provided list of IpAccessListLines
   */
  void applyTo(Vsys vsys, LineAction action, List<IpAccessListLine> lines);
}
