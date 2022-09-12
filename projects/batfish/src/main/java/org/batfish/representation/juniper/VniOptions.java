package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public class VniOptions implements Serializable {

  private final Integer _vniId;

  private Map<Integer, VniOptions> _vniOptions;
  private @Nullable ExtendedCommunityOrAuto _vrfTargetCommunityorAuto;
  private @Nullable ExtendedCommunity _vrfTargetImport;
  private @Nullable ExtendedCommunity _vrfTargetExport;

  public VniOptions(Integer vniId) {
    _vniId = vniId;
    _vrfTargetCommunityorAuto = null;
    _vrfTargetImport = null;
    _vrfTargetExport = null;
  }

  public Integer getVniId() {
    return _vniId;
  }

  public ExtendedCommunityOrAuto getVrfTargetCommunityorAuto() {
    return _vrfTargetCommunityorAuto;
  }

  public ExtendedCommunity getVrfTargetImport() {
    return _vrfTargetImport;
  }

  public ExtendedCommunity getVrfTargetExport() {
    return _vrfTargetExport;
  }

  public void setVrfTargetCommunityorAuto(ExtendedCommunityOrAuto vrfTargetCommunityorAuto) {
    _vrfTargetCommunityorAuto = vrfTargetCommunityorAuto;
  }

  public void setVrfTargetImport(ExtendedCommunity vrfTargetImport) {
    _vrfTargetImport = vrfTargetImport;
  }

  public void setVrfTargetExport(ExtendedCommunity vrfTargetExport) {
    _vrfTargetExport = vrfTargetExport;
  }
}
