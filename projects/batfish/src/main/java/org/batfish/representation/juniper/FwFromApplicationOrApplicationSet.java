package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public class FwFromApplicationOrApplicationSet implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String _applicationName;

  public FwFromApplicationOrApplicationSet(String applicationName) {
    _applicationName = applicationName;
  }

  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    ApplicationSetMember application = jc.getApplications().get(_applicationName);
    if (application == null) {
      application = jc.getApplicationSets().get(_applicationName);
    }
    if (application == null) {
      w.redFlag("Reference to undefined application: \"" + _applicationName + "\"");
    } else {
      application.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }
}
