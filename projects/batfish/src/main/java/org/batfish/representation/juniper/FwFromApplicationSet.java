package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public class FwFromApplicationSet implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final ApplicationSet _applicationSet;

  private final String _applicationSetName;

  public FwFromApplicationSet(ApplicationSet applicationSet) {
    _applicationSet = applicationSet;
    _applicationSetName = null;
  }

  public FwFromApplicationSet(String applicationSetName) {
    _applicationSet = null;
    _applicationSetName = applicationSetName;
  }

  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    ApplicationSetMember applicationSetMember;
    if (_applicationSetName != null) {
      applicationSetMember = jc.getApplicationSets().get(_applicationSetName);
    } else {
      applicationSetMember = _applicationSet;
    }
    if (applicationSetMember == null) {
      w.redFlag("Reference to undefined application: \"" + _applicationSetName + "\"");
    } else {
      applicationSetMember.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }
}
