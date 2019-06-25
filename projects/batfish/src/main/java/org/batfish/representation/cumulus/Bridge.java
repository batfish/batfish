package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;

/** Settings for bridged ports */
public class Bridge implements Serializable {

  private @Nonnull Set<String> _ports;
  private int _pvid;
  private @Nonnull IntegerSpace _vids;

  public Bridge() {
    _ports = ImmutableSet.of();
    _pvid = 1;
    _vids = IntegerSpace.EMPTY;
  }

  /** Bridged ports */
  public @Nonnull Set<String> getPorts() {
    return _ports;
  }

  /** Default native VLAN for bridged ports */
  public int getPvid() {
    return _pvid;
  }

  /** Default allowed VLANs for bridged ports */
  public @Nonnull IntegerSpace getVids() {
    return _vids;
  }

  public void setPorts(Set<String> ports) {
    _ports = ImmutableSet.copyOf(ports);
  }

  public void setPvid(int pvid) {
    _pvid = pvid;
  }

  public void setVids(IntegerSpace vids) {
    _vids = vids;
  }

  public @Nonnull org.batfish.datamodel.vendor_family.cumulus.Bridge toDataModel() {
    return org.batfish.datamodel.vendor_family.cumulus.Bridge.builder()
        .setPorts(_ports)
        .setPvid(_pvid)
        .setVids(_vids)
        .build();
  }
}
