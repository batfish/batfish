package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public final class FwFromJunosApplicationSet extends FwFromApplicationSetMember {

  private static final long serialVersionUID = 1L;

  private final JunosApplicationSet _junosApplicationSet;

  public FwFromJunosApplicationSet(JunosApplicationSet junosApplicationSet) {
    _junosApplicationSet = junosApplicationSet;
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    if (!_junosApplicationSet.hasDefinition()) {
      w.redFlag("Reference to undefined application: \"" + _junosApplicationSet.name() + "\"");
    } else {
      _junosApplicationSet.getApplicationSet().applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }
}
