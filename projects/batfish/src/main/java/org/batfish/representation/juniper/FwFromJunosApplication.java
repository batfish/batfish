package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public class FwFromJunosApplication implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String _junosApplicationName;

  public FwFromJunosApplication(String junosApplicationName) {
    _junosApplicationName = junosApplicationName;
  }

  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    JunosApplication junosApplication = JunosApplication.valueOf(_junosApplicationName);
    if (!junosApplication.hasDefinition()) {
      w.redFlag("Reference to undefined application: \"" + junosApplication.name() + "\"");
    } else {
      junosApplication.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }
}
