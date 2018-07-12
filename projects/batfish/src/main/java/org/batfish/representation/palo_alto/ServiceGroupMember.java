package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.List;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public interface ServiceGroupMember extends Serializable {
  /**
   * Add ServiceGroupMember match condition as a new IpAccessListLine to the provided list of
   * IpAccessListLines
   */
  void addTo(List<IpAccessListLine> lines, LineAction action, PaloAltoConfiguration pc, Vsys vsys);

  /** Return the name of this ServiceGroupMember */
  String getName();
}
