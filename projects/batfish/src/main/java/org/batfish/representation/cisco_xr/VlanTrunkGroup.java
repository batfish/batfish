package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SubRange;

/** A VLAN trunk group configuration, Arista-specific concept */
@ParametersAreNonnullByDefault
public final class VlanTrunkGroup implements Serializable {

  private String _name;
  private IntegerSpace _vlans;

  public VlanTrunkGroup(String name) {
    _name = name;
    _vlans = IntegerSpace.EMPTY;
  }

  public String getName() {
    return _name;
  }

  /** Return the set of VLANs that belong to this trunk group, as an {@link IntegerSpace} */
  public IntegerSpace getVlans() {
    return _vlans;
  }

  public void addVlans(SubRange range) {
    addVlans(IntegerSpace.of(range));
  }

  public void addVlans(IntegerSpace vlans) {
    _vlans = _vlans.union(vlans);
  }

  public void addVlan(int vlan) {
    _vlans = _vlans.union(IntegerSpace.of(vlan));
  }
}
