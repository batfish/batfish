package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public class FwFromJunosApplicationSet implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String _junosApplicationSetName;

  public FwFromJunosApplicationSet(String junosApplicationSetName) {
    _junosApplicationSetName = junosApplicationSetName;
  }

  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    JunosApplicationSet junosApplicationSet = JunosApplicationSet.valueOf(_junosApplicationSetName);
    if (!junosApplicationSet.hasDefinition()) {
      w.redFlag("Reference to undefined application: \"" + junosApplicationSet.name() + "\"");
    } else {
      junosApplicationSet.getApplicationSet().applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }
}
