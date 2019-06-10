package org.batfish.representation.palo_alto;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejectsByDefault;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateCrossZoneCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateCrossZoneCallsFromExternal;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateCrossZoneCallsFromLayer3;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateDoubleCrossZoneCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateInterVsysCrossZoneCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateOutgoingFilter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.representation.palo_alto.Zone.Type;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link PaloAltoConfiguration}. */
public final class PaloAltoConfigurationTest {

  private static final String DST_EXTERNAL_ZONE_NAME = "dstExternalZone";
  private static final Ip DST_IP = Ip.parse("10.0.2.2");
  private static final String DST_LAYER3_ZONE_NAME = "dstLayer3Zone";
  private static final String DST_VSYS_NAME = "dstVsys";
  private static final String SRC_EXTERNAL_ZONE_NAME = "srcExternalZone";
  private static final String SRC_INTERFACE_NAME = "srcInterface";
  private static final Ip SRC_IP = Ip.parse("10.0.1.2");
  private static final String SRC_LAYER3_ZONE_NAME = "srcLayer3Zone";
  private static final String SRC_VSYS_NAME = "srcVsys";
  private NetworkFactory _nf;

  private @Nonnull Flow createFlow(Ip srcIp, Ip dstIp, String ingressInterface) {
    return Flow.builder()
        .setTag("tag")
        .setSrcIp(srcIp)
        .setDstIp(dstIp)
        .setIngressInterface(ingressInterface)
        .setIngressNode("dummy")
        .setIpProtocol(IpProtocol.TCP) // arbitrary
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .setDstPort(NamedPort.SSH.number()) // arbitrary
        .build();
  }

  private @Nonnull Map<String, IpAccessList> createInterVsysCrossZoneFilters() {
    String srcLayer3ToSrcExternalName =
        zoneToZoneFilter(
            computeObjectName(SRC_VSYS_NAME, SRC_LAYER3_ZONE_NAME),
            computeObjectName(SRC_VSYS_NAME, SRC_EXTERNAL_ZONE_NAME));
    String dstExternalToDstLayer3Name =
        zoneToZoneFilter(
            computeObjectName(DST_VSYS_NAME, DST_EXTERNAL_ZONE_NAME),
            computeObjectName(DST_VSYS_NAME, DST_LAYER3_ZONE_NAME));

    IpAccessList srcLayer3ToSrcExternal =
        IpAccessList.builder()
            .setLines(ImmutableList.of(accepting(matchSrc(SRC_IP))))
            .setName(srcLayer3ToSrcExternalName)
            .build();
    IpAccessList dstExternalToDstLayer3 =
        IpAccessList.builder()
            .setLines(ImmutableList.of(accepting(matchDst(DST_IP))))
            .setName(srcLayer3ToSrcExternalName)
            .build();
    return ImmutableMap.of(
        srcLayer3ToSrcExternalName,
        srcLayer3ToSrcExternal,
        dstExternalToDstLayer3Name,
        dstExternalToDstLayer3);
  }

  private @Nonnull Map<String, IpAccessList> createIntraVsysCrossZoneFilters() {
    String srcLayer3ToDstLayer3Name =
        zoneToZoneFilter(
            computeObjectName(DST_VSYS_NAME, SRC_LAYER3_ZONE_NAME),
            computeObjectName(DST_VSYS_NAME, DST_LAYER3_ZONE_NAME));

    IpAccessList srcLayer3ToDstLayer3 =
        IpAccessList.builder()
            .setLines(ImmutableList.of(accepting(matchDst(DST_IP))))
            .setName(srcLayer3ToDstLayer3Name)
            .build();
    return ImmutableMap.of(srcLayer3ToDstLayer3Name, srcLayer3ToDstLayer3);
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
  }

  @Test
  public void testGenerateCrossZoneCallsExternal() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone dstVsysExternalZone = new Zone(DST_EXTERNAL_ZONE_NAME, dstVsys);
    dstVsysExternalZone.setType(Type.EXTERNAL);
    dstVsysExternalZone.getExternalNames().add(SRC_VSYS_NAME);
    dstVsys.getZones().put(DST_EXTERNAL_ZONE_NAME, dstVsysExternalZone);

    Vsys srcVsys = new Vsys(SRC_VSYS_NAME);
    Zone srcExternalZone = new Zone(SRC_EXTERNAL_ZONE_NAME, srcVsys);
    srcExternalZone.setType(Type.EXTERNAL);
    srcExternalZone.getExternalNames().add(DST_VSYS_NAME);
    srcVsys.getZones().put(SRC_EXTERNAL_ZONE_NAME, srcExternalZone);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, srcVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    srcVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCalls(
                        dstVsys,
                        dstVsysExternalZone,
                        DST_LAYER3_ZONE_NAME,
                        ImmutableList.of(srcVsys, dstVsys))
                    .collect(ImmutableList.toImmutableList()))
            .build();

    // Valid, so should accept inter-vsys flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateCrossZoneCallsFromExternal() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone dstVsysExternalZone = new Zone(DST_EXTERNAL_ZONE_NAME, dstVsys);
    dstVsysExternalZone.setType(Type.EXTERNAL);
    dstVsysExternalZone.getExternalNames().add(SRC_VSYS_NAME);
    dstVsys.getZones().put(DST_EXTERNAL_ZONE_NAME, dstVsysExternalZone);

    Vsys srcVsys = new Vsys(SRC_VSYS_NAME);
    Zone srcExternalZone = new Zone(SRC_EXTERNAL_ZONE_NAME, srcVsys);
    srcExternalZone.setType(Type.EXTERNAL);
    srcExternalZone.getExternalNames().add(DST_VSYS_NAME);
    srcVsys.getZones().put(SRC_EXTERNAL_ZONE_NAME, srcExternalZone);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, srcVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    srcVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCallsFromExternal(
                        dstVsys,
                        dstVsysExternalZone,
                        DST_LAYER3_ZONE_NAME,
                        ImmutableList.of(srcVsys, dstVsys))
                    .collect(ImmutableList.toImmutableList()))
            .build();

    // Valid, so should accept inter-vsys flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateCrossZoneCallsFromExternalMisconfiguredExternal() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone dstVsysExternalZone = new Zone(DST_EXTERNAL_ZONE_NAME, dstVsys);
    dstVsysExternalZone.setType(Type.EXTERNAL);
    // missing external reference to srcVsys
    dstVsys.getZones().put(DST_EXTERNAL_ZONE_NAME, dstVsysExternalZone);

    Vsys srcVsys = new Vsys(SRC_VSYS_NAME);
    Zone srcExternalZone = new Zone(SRC_EXTERNAL_ZONE_NAME, srcVsys);
    srcExternalZone.setType(Type.EXTERNAL);
    srcExternalZone.getExternalNames().add(DST_VSYS_NAME);
    srcVsys.getZones().put(SRC_EXTERNAL_ZONE_NAME, srcExternalZone);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, srcVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    srcVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    // no lines should be returned since dstVsysExternalZone does not point to srcVsys
    assertEquals(
        generateCrossZoneCallsFromExternal(
                dstVsys,
                dstVsysExternalZone,
                DST_LAYER3_ZONE_NAME,
                ImmutableList.of(srcVsys, dstVsys))
            .count(),
        0L);
  }

  @Test
  public void testGenerateCrossZoneCallsFromLayer3() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, dstVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    dstVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCallsFromLayer3(dstVsys, srcLayer3Zone, DST_LAYER3_ZONE_NAME)
                    .collect(ImmutableList.toImmutableList()))
            .build();

    Map<String, IpAccessList> crossZoneFilters = createIntraVsysCrossZoneFilters();

    // Reject because dstIp does not match
    assertThat(
        generatedFilter,
        rejects(
            createFlow(SRC_IP, Ip.ZERO, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            crossZoneFilters,
            ImmutableMap.of()));
    // Accept because everything matches
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            crossZoneFilters,
            ImmutableMap.of()));
    // Default reject because src interface is not matched
    assertThat(
        generatedFilter,
        rejectsByDefault(
            createFlow(SRC_IP, DST_IP, "other"), "other", crossZoneFilters, ImmutableMap.of()));
  }

  @Test
  public void testGenerateCrossZoneCallsLayer2() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone srcZone = new Zone(SRC_LAYER3_ZONE_NAME, dstVsys);
    srcZone.setType(Type.LAYER2);
    srcZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    dstVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcZone);

    // no lines should be returned since srcZone is a layer-2 zone
    assertEquals(
        generateCrossZoneCalls(dstVsys, srcZone, DST_LAYER3_ZONE_NAME, ImmutableList.of(dstVsys))
            .count(),
        0L);
  }

  @Test
  public void testGenerateCrossZoneCallsLayer3() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, dstVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    dstVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCalls(
                        dstVsys, srcLayer3Zone, DST_LAYER3_ZONE_NAME, ImmutableList.of(dstVsys))
                    .collect(ImmutableList.toImmutableList()))
            .build();

    // Valid, so should accept intra-vsys flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createIntraVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateDoubleCrossZoneCalls() {
    Map<String, IpAccessList> crossZoneFilters = createInterVsysCrossZoneFilters();
    Zone srcVsysLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, new Vsys(SRC_VSYS_NAME));
    srcVsysLayer3Zone.setType(Type.LAYER3);
    srcVsysLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateDoubleCrossZoneCalls(
                        DST_LAYER3_ZONE_NAME,
                        DST_VSYS_NAME,
                        DST_EXTERNAL_ZONE_NAME,
                        SRC_VSYS_NAME,
                        SRC_EXTERNAL_ZONE_NAME,
                        srcVsysLayer3Zone)
                    .collect(ImmutableList.toImmutableList()))
            .build();

    Flow matchesSrcVsysCrossZoneFilter = createFlow(SRC_IP, Ip.ZERO, SRC_INTERFACE_NAME);
    Flow matchesDstVsysCrossZoneFilter = createFlow(Ip.ZERO, DST_IP, SRC_INTERFACE_NAME);
    Flow matchesBothFilters = createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME);
    Flow matchesBothFiltersButNotInterface = createFlow(SRC_IP, DST_IP, "other");

    // Reject because dstIp does not match
    assertThat(
        generatedFilter,
        rejects(
            matchesSrcVsysCrossZoneFilter,
            SRC_INTERFACE_NAME,
            crossZoneFilters,
            ImmutableMap.of()));
    // Reject because srcIp does not match
    assertThat(
        generatedFilter,
        rejects(
            matchesDstVsysCrossZoneFilter,
            SRC_INTERFACE_NAME,
            crossZoneFilters,
            ImmutableMap.of()));
    // Accept because everything matches
    assertThat(
        generatedFilter,
        accepts(matchesBothFilters, SRC_INTERFACE_NAME, crossZoneFilters, ImmutableMap.of()));
    // Default reject because src interface is not matched
    assertThat(
        generatedFilter,
        rejectsByDefault(
            matchesBothFiltersButNotInterface, "other", crossZoneFilters, ImmutableMap.of()));
  }

  @Test
  public void testGenerateInterVsysCrossZoneCalls() {
    Vsys srcVsys = new Vsys(SRC_VSYS_NAME);
    Zone srcExternalZone = new Zone(SRC_EXTERNAL_ZONE_NAME, srcVsys);
    srcExternalZone.setType(Type.EXTERNAL);
    srcExternalZone.getExternalNames().add(DST_VSYS_NAME);
    srcVsys.getZones().put(SRC_EXTERNAL_ZONE_NAME, srcExternalZone);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, srcVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    srcVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateInterVsysCrossZoneCalls(
                        DST_LAYER3_ZONE_NAME, DST_VSYS_NAME, DST_EXTERNAL_ZONE_NAME, srcVsys)
                    .collect(ImmutableList.toImmutableList()))
            .build();

    // Valid, so should accept inter-vsys flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateInterVsysCrossZoneCallsNoExternal() {
    Vsys srcVsys = new Vsys(SRC_VSYS_NAME);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, srcVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    srcVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);
    // no lines should be returned since srcVsys has no external zones
    assertEquals(
        generateInterVsysCrossZoneCalls(
                DST_LAYER3_ZONE_NAME, DST_VSYS_NAME, DST_EXTERNAL_ZONE_NAME, srcVsys)
            .count(),
        0L);
  }

  @Test
  public void testGenerateInterVsysCrossZoneCallsNoMatchingExternal() {
    Vsys srcVsys = new Vsys(SRC_VSYS_NAME);
    Zone srcExternalZone = new Zone(SRC_EXTERNAL_ZONE_NAME, srcVsys);
    srcExternalZone.setType(Type.EXTERNAL);
    srcVsys.getZones().put(SRC_EXTERNAL_ZONE_NAME, srcExternalZone);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, srcVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    srcVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);
    // no lines should be returned since srcVsys has no external zone pointing to dstVsys
    assertEquals(
        generateInterVsysCrossZoneCalls(
                DST_LAYER3_ZONE_NAME, DST_VSYS_NAME, DST_EXTERNAL_ZONE_NAME, srcVsys)
            .count(),
        0L);
  }

  @Test
  public void testGenerateOutgoingFilter() {
    Vsys dstVsys = new Vsys(DST_VSYS_NAME);
    Zone srcLayer3Zone = new Zone(SRC_LAYER3_ZONE_NAME, dstVsys);
    srcLayer3Zone.setType(Type.LAYER3);
    srcLayer3Zone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    dstVsys.getZones().put(SRC_LAYER3_ZONE_NAME, srcLayer3Zone);

    IpAccessList generatedFilter =
        generateOutgoingFilter(dstVsys, DST_LAYER3_ZONE_NAME, ImmutableList.of(dstVsys));

    // Valid, so should accept intra-vsys flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createIntraVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }
}
