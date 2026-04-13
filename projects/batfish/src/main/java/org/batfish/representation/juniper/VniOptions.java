package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** Per-VNI options configured under {@code protocols evpn vni-options}. */
@ParametersAreNonnullByDefault
public final class VniOptions implements Serializable {

  private final int _vniId;
  private @Nullable ExtendedCommunityOrAuto _vrfTargetCommunityOrAuto;
  private @Nullable ExtendedCommunity _vrfTargetImport;
  private @Nullable ExtendedCommunity _vrfTargetExport;

  public VniOptions(int vniId) {
    _vniId = vniId;
  }

  public int getVniId() {
    return _vniId;
  }

  public @Nullable ExtendedCommunityOrAuto getVrfTargetCommunityOrAuto() {
    return _vrfTargetCommunityOrAuto;
  }

  public void setVrfTargetCommunityOrAuto(
      @Nullable ExtendedCommunityOrAuto vrfTargetCommunityOrAuto) {
    _vrfTargetCommunityOrAuto = vrfTargetCommunityOrAuto;
  }

  public @Nullable ExtendedCommunity getVrfTargetImport() {
    return _vrfTargetImport;
  }

  public void setVrfTargetImport(@Nullable ExtendedCommunity vrfTargetImport) {
    _vrfTargetImport = vrfTargetImport;
  }

  public @Nullable ExtendedCommunity getVrfTargetExport() {
    return _vrfTargetExport;
  }

  public void setVrfTargetExport(@Nullable ExtendedCommunity vrfTargetExport) {
    _vrfTargetExport = vrfTargetExport;
  }
}
