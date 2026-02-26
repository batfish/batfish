package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public class SwitchOptions implements Serializable {

  private String _vtepSourceInterface;
  private RouteDistinguisher _routeDistinguisher;
  private ExtendedCommunityOrAuto _vrfTargetCommunityorAuto;
  private ExtendedCommunity _vrfTargetImport;
  private ExtendedCommunity _vrfTargetExport;
  private String _vrfImportPolicy;
  private String _vrfExportPolicy;

  public String getVtepSourceInterface() {
    return _vtepSourceInterface;
  }

  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
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

  public void setVtepSourceInterface(String vtepSourceInterface) {
    _vtepSourceInterface = vtepSourceInterface;
  }

  public void setRouteDistinguisher(RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
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

  public String getVrfImportPolicy() {
    return _vrfImportPolicy;
  }

  public void setVrfImportPolicy(String vrfImportPolicy) {
    _vrfImportPolicy = vrfImportPolicy;
  }

  public String getVrfExportPolicy() {
    return _vrfExportPolicy;
  }

  public void setVrfExportPolicy(String vrfExportPolicy) {
    _vrfExportPolicy = vrfExportPolicy;
  }
}
