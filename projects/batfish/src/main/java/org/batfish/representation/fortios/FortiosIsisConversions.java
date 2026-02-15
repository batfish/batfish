package org.batfish.representation.fortios;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.isis.IsisLevelSettings;

/** Helper functions for generating VI IS-IS structures for {@link FortiosConfiguration}. */
public final class FortiosIsisConversions {

  /**
   * Converts a FortiOS {@link IsisProcess} to a VI IS-IS process and attaches it to the default
   * VRF.
   */
  public static void convertIsis(
      @Nullable org.batfish.representation.fortios.IsisProcess isisProcess,
      Configuration c,
      Warnings w) {
    if (isisProcess == null) {
      return;
    }

    String netAddress = isisProcess.getNetAddress();
    if (netAddress == null) {
      w.redFlag("IS-IS process has no NET address configured");
      return;
    }

    IsoAddress isoAddress;
    try {
      isoAddress = new IsoAddress(netAddress);
    } catch (IllegalArgumentException e) {
      w.redFlagf("Invalid IS-IS NET address: %s", netAddress);
      return;
    }

    // Build VI IS-IS process
    org.batfish.datamodel.isis.IsisProcess.Builder viProcBuilder =
        org.batfish.datamodel.isis.IsisProcess.builder().setNetAddress(isoAddress);

    // Set enabled levels based on is-type
    IsisLevelSettings levelSettings = IsisLevelSettings.builder().build();
    switch (isisProcess.getIsTypeEffective()) {
      case LEVEL_1:
        viProcBuilder.setLevel1(levelSettings);
        break;
      case LEVEL_2:
        viProcBuilder.setLevel2(levelSettings);
        break;
      case LEVEL_1_2:
        viProcBuilder.setLevel1(levelSettings);
        viProcBuilder.setLevel2(levelSettings);
        break;
    }

    // Attach to default VRF
    Vrf defaultVrf = c.getDefaultVrf();
    org.batfish.datamodel.isis.IsisProcess viProc = viProcBuilder.setVrf(defaultVrf).build();

    // Convert IS-IS interfaces
    isisProcess
        .getInterfaces()
        .values()
        .forEach(iface -> convertIsisInterface(iface, c, w, viProc));
  }

  /**
   * Converts a FortiOS {@link IsisInterface} and applies settings to the corresponding VI
   * interface.
   */
  private static void convertIsisInterface(
      org.batfish.representation.fortios.IsisInterface isisInterface,
      Configuration c,
      Warnings w,
      org.batfish.datamodel.isis.IsisProcess viProc) {

    String ifaceName = isisInterface.getName();
    Interface viIface = c.getAllInterfaces().get(ifaceName);

    if (viIface == null) {
      w.redFlagf("IS-IS interface %s does not exist in configuration", ifaceName);
      return;
    }

    if (!isisInterface.getStatusEffective()) {
      // Interface IS-IS is disabled
      return;
    }

    // Determine which levels are enabled for this interface
    IsisProcess.Level circuitType = isisInterface.getCircuitTypeEffective();

    IsisInterfaceLevelSettings.Builder level1Builder =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.ACTIVE);
    IsisInterfaceLevelSettings.Builder level2Builder =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.ACTIVE);

    // Apply metric - use level-specific metrics if configured, otherwise use general metric
    Integer metricLevel1 = isisInterface.getMetricLevel1();
    Integer metricLevel2 = isisInterface.getMetricLevel2();
    Integer generalMetric = isisInterface.getMetric();

    if (metricLevel1 != null) {
      level1Builder.setCost(metricLevel1.longValue());
    } else if (generalMetric != null) {
      level1Builder.setCost(generalMetric.longValue());
    }

    if (metricLevel2 != null) {
      level2Builder.setCost(metricLevel2.longValue());
    } else if (generalMetric != null) {
      level2Builder.setCost(generalMetric.longValue());
    }

    // Build interface settings
    IsisInterfaceSettings.Builder ifaceSettingsBuilder = IsisInterfaceSettings.builder();

    // Set the ISO address from the process
    ifaceSettingsBuilder.setIsoAddress(viProc.getNetAddress());

    // Set enabled levels based on circuit type and process is-type
    switch (circuitType) {
      case LEVEL_1:
        ifaceSettingsBuilder.setLevel1(level1Builder.build());
        break;
      case LEVEL_2:
        ifaceSettingsBuilder.setLevel2(level2Builder.build());
        break;
      case LEVEL_1_2:
        ifaceSettingsBuilder.setLevel1(level1Builder.build());
        ifaceSettingsBuilder.setLevel2(level2Builder.build());
        break;
    }

    viIface.setIsis(ifaceSettingsBuilder.build());
  }

  private FortiosIsisConversions() {}
}
