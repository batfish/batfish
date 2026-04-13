package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public class SwitchOptions implements Serializable {

  private String _vtepSourceInterface;
  private RouteDistinguisher _routeDistinguisher;
  private @Nullable ExtendedCommunityOrAuto _vrfTargetCommunityOrAuto;
  private @Nullable ExtendedCommunity _vrfTargetImport;
  private @Nullable ExtendedCommunity _vrfTargetExport;
  private @Nullable String _vrfExportPolicy;
  private @Nullable String _vrfImportPolicy;

  public String getVtepSourceInterface() {
    return _vtepSourceInterface;
  }

  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setVtepSourceInterface(String vtepSourceInterface) {
    _vtepSourceInterface = vtepSourceInterface;
  }

  public void setRouteDistinguisher(RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
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

  public @Nullable String getVrfExportPolicy() {
    return _vrfExportPolicy;
  }

  public void setVrfExportPolicy(@Nullable String vrfExportPolicy) {
    _vrfExportPolicy = vrfExportPolicy;
  }

  public @Nullable String getVrfImportPolicy() {
    return _vrfImportPolicy;
  }

  public void setVrfImportPolicy(@Nullable String vrfImportPolicy) {
    _vrfImportPolicy = vrfImportPolicy;
  }
}
