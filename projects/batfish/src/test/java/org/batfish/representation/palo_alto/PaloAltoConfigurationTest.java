package org.batfish.representation.palo_alto;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlag;
import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.matchers.AclLineMatchers.hasTraceElement;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejectsByDefault;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.checkIntrazoneValidityAndWarn;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateCrossZoneCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateCrossZoneCallsFromExternal;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateCrossZoneCallsFromLayer3;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateDoubleCrossZoneCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateInterVsysCrossZoneCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateOutgoingFilter;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateSgSgLines;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateSharedGatewayOutgoingFilter;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.generateVsysSharedGatewayCalls;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.securityRuleApplies;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.zoneToZoneMatchTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.zoneToZoneRejectTraceElement;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.representation.palo_alto.SecurityRule.RuleType;
import org.batfish.representation.palo_alto.Zone.Type;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link PaloAltoConfiguration}. */
@ParametersAreNonnullByDefault
public final class PaloAltoConfigurationTest {

  private static final Ip DST_IP = Ip.parse("10.0.2.2");
  private static final String EXTERNAL_FROM_ZONE_NAME = "externalFromZone";
  private static final String EXTERNAL_TO_ZONE_NAME = "externalToZone";
  private static final String EXTERNAL_VSYS_NAME = "externalVsys";
  private static final String FROM_ZONE_NAME = "fromZone";
  private static final String SRC_INTERFACE_NAME = "srcInterface";
  private static final Ip SRC_IP = Ip.parse("10.0.1.2");
  private static final String TO_ZONE_NAME = "toZone";
  private static final String VSYS_NAME = "vsys";
  private NetworkFactory _nf;

  private @Nonnull Flow createFlow(Ip srcIp, Ip dstIp, String ingressInterface) {
    return Flow.builder()
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
    String externalVsysCrossZoneFilterName =
        zoneToZoneFilter(
            computeObjectName(EXTERNAL_VSYS_NAME, EXTERNAL_FROM_ZONE_NAME),
            computeObjectName(EXTERNAL_VSYS_NAME, EXTERNAL_TO_ZONE_NAME));
    String crossZoneFilterName =
        zoneToZoneFilter(
            computeObjectName(VSYS_NAME, FROM_ZONE_NAME),
            computeObjectName(VSYS_NAME, TO_ZONE_NAME));

    IpAccessList externalVsysCrossZoneFilter =
        IpAccessList.builder()
            .setLines(ImmutableList.of(accepting(matchSrc(SRC_IP))))
            .setName(externalVsysCrossZoneFilterName)
            .build();
    IpAccessList crossZoneFilter =
        IpAccessList.builder()
            .setLines(ImmutableList.of(accepting(matchDst(DST_IP))))
            .setName(externalVsysCrossZoneFilterName)
            .build();
    return ImmutableMap.of(
        externalVsysCrossZoneFilterName,
        externalVsysCrossZoneFilter,
        crossZoneFilterName,
        crossZoneFilter);
  }

  private @Nonnull Map<String, IpAccessList> createIntraVsysCrossZoneFilters() {
    String crossZoneFilterName =
        zoneToZoneFilter(
            computeObjectName(VSYS_NAME, FROM_ZONE_NAME),
            computeObjectName(VSYS_NAME, TO_ZONE_NAME));

    IpAccessList crossZoneFilter =
        IpAccessList.builder()
            .setLines(ImmutableList.of(accepting(matchDst(DST_IP))))
            .setName(crossZoneFilterName)
            .build();
    return ImmutableMap.of(crossZoneFilterName, crossZoneFilter);
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
  }

  @Test
  public void testGenerateCrossZoneCallsExternal() {
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.EXTERNAL);
    fromZone.getExternalNames().add(EXTERNAL_VSYS_NAME);
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    externalVsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, externalVsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    externalVsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCalls(
                        fromZone,
                        new Zone(TO_ZONE_NAME, vsys),
                        ImmutableList.of(),
                        ImmutableList.of(externalVsys, vsys))
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
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.EXTERNAL);
    fromZone.getExternalNames().add(EXTERNAL_VSYS_NAME);
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    externalVsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, externalVsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    externalVsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCallsFromExternal(
                        fromZone,
                        new Zone(TO_ZONE_NAME, vsys),
                        ImmutableList.of(),
                        ImmutableList.of(externalVsys, vsys))
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
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.EXTERNAL);
    // missing external reference to externalVsys
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    externalVsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, externalVsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    externalVsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    // no lines should be returned since fromZone does not point to externalVsys
    assertEquals(
        generateCrossZoneCallsFromExternal(
                fromZone,
                new Zone(TO_ZONE_NAME, vsys),
                ImmutableList.of(),
                ImmutableList.of(externalVsys, vsys))
            .count(),
        0L);
  }

  @Test
  public void testGenerateCrossZoneCallsFromLayer3() {
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.LAYER3);
    fromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCallsFromLayer3(fromZone, new Zone(TO_ZONE_NAME, vsys))
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
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.LAYER2);
    fromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    // no lines should be returned since fromZone is a layer-2 zone
    assertEquals(
        generateCrossZoneCalls(
                fromZone, new Zone(TO_ZONE_NAME, vsys), ImmutableList.of(), ImmutableList.of(vsys))
            .count(),
        0L);
  }

  @Test
  public void testGenerateCrossZoneCallsLayer3() {
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.LAYER3);
    fromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateCrossZoneCalls(
                        fromZone,
                        new Zone(TO_ZONE_NAME, vsys),
                        ImmutableList.of(),
                        ImmutableList.of(vsys))
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
    Vsys vsys = new Vsys(VSYS_NAME);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateDoubleCrossZoneCalls(
                        new Zone(FROM_ZONE_NAME, vsys),
                        new Zone(TO_ZONE_NAME, vsys),
                        externalFromZone,
                        new Zone(EXTERNAL_TO_ZONE_NAME, externalVsys))
                    .collect(ImmutableList.toImmutableList()))
            .build();

    Flow matchesExternalVsysCrossZoneFilter = createFlow(SRC_IP, Ip.ZERO, SRC_INTERFACE_NAME);
    Flow matchesCrossZoneFilter = createFlow(Ip.ZERO, DST_IP, SRC_INTERFACE_NAME);
    Flow matchesBothFilters = createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME);
    Flow matchesBothFiltersButNotInterface = createFlow(SRC_IP, DST_IP, "other");

    // Reject because dstIp does not match
    assertThat(
        generatedFilter,
        rejects(
            matchesExternalVsysCrossZoneFilter,
            SRC_INTERFACE_NAME,
            crossZoneFilters,
            ImmutableMap.of()));
    // Reject because srcIp does not match
    assertThat(
        generatedFilter,
        rejects(matchesCrossZoneFilter, SRC_INTERFACE_NAME, crossZoneFilters, ImmutableMap.of()));
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
    Vsys vsys = new Vsys(VSYS_NAME);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    externalVsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, externalVsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    externalVsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateInterVsysCrossZoneCalls(
                        new Zone(FROM_ZONE_NAME, vsys), new Zone(TO_ZONE_NAME, vsys), externalVsys)
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
    Vsys vsys = new Vsys(VSYS_NAME);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    externalVsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    // no lines should be returned since externalVsys has no external zones
    assertEquals(
        generateInterVsysCrossZoneCalls(
                new Zone(FROM_ZONE_NAME, vsys), new Zone(TO_ZONE_NAME, vsys), externalVsys)
            .count(),
        0L);
  }

  @Test
  public void testGenerateInterVsysCrossZoneCallsNoMatchingExternal() {
    Vsys vsys = new Vsys(VSYS_NAME);

    Vsys externalVsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, externalVsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    externalVsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, externalVsys);
    externalToZone.setType(Type.EXTERNAL);
    externalVsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    // no lines should be returned since externalVsys has no external zone pointing to vsys
    assertEquals(
        generateInterVsysCrossZoneCalls(
                new Zone(FROM_ZONE_NAME, vsys), new Zone(TO_ZONE_NAME, vsys), externalVsys)
            .count(),
        0L);
  }

  @Test
  public void testGenerateOutgoingFilter() {
    Vsys vsys = new Vsys(VSYS_NAME);
    Zone fromZone = new Zone(FROM_ZONE_NAME, vsys);
    fromZone.setType(Type.LAYER3);
    fromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(FROM_ZONE_NAME, fromZone);

    IpAccessList generatedFilter =
        generateOutgoingFilter(
            new Zone(TO_ZONE_NAME, vsys), ImmutableList.of(), ImmutableList.of(vsys));

    // Valid, so should accept intra-vsys flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createIntraVsysCrossZoneFilters(),
            ImmutableMap.of()));

    // Should have trace elements about zone traversal
    assertThat(
        generatedFilter.getLines(),
        contains(
            hasTraceElement(
                zoneToZoneMatchTraceElement(FROM_ZONE_NAME, TO_ZONE_NAME, vsys.getName())),
            hasTraceElement(
                zoneToZoneRejectTraceElement(FROM_ZONE_NAME, TO_ZONE_NAME, vsys.getName()))));
  }

  @Test
  public void testGenerateSgSglines() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);
    Vsys ingressSharedGateway = new Vsys(EXTERNAL_VSYS_NAME);
    ingressSharedGateway.getImportedInterfaces().add(SRC_INTERFACE_NAME);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateSgSgLines(sharedGateway, ingressSharedGateway)
                    .collect(ImmutableList.toImmutableList()))
            .build();

    // Valid, so should accept inter-sg flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            ImmutableMap.of(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateSgSglinesEmptyIngress() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);
    Vsys ingressSharedGateway = new Vsys(EXTERNAL_VSYS_NAME);

    // no lines should be returned since there are no interfaces in ingressSharedGateway
    assertEquals(generateSgSgLines(sharedGateway, ingressSharedGateway).count(), 0L);
  }

  @Test
  public void testGenerateSharedGatewayOutgoingFilterInterSg() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);
    Vsys ingressSharedGateway = new Vsys(EXTERNAL_VSYS_NAME);
    ingressSharedGateway.getImportedInterfaces().add(SRC_INTERFACE_NAME);

    // Valid, so should accept inter-sg flow
    assertThat(
        generateSharedGatewayOutgoingFilter(
            sharedGateway,
            ImmutableList.of(sharedGateway, ingressSharedGateway),
            ImmutableList.of()),
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateSharedGatewayOutgoingFilterIntraSg() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);
    sharedGateway.getImportedInterfaces().add(SRC_INTERFACE_NAME);

    // Valid, so should accept intra-sg flow
    assertThat(
        generateSharedGatewayOutgoingFilter(
            sharedGateway, ImmutableList.of(sharedGateway), ImmutableList.of()),
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateSharedGatewayOutgoingFilterVsys() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);

    Vsys vsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, vsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, vsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    vsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    // Valid, so should accept vsys->shared gateway flow
    assertThat(
        generateSharedGatewayOutgoingFilter(
            sharedGateway, ImmutableList.of(sharedGateway), ImmutableList.of(vsys)),
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateVsysSharedGatewayCalls() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);

    Vsys vsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, vsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, vsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    vsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    IpAccessList generatedFilter =
        _nf.aclBuilder()
            .setLines(
                generateVsysSharedGatewayCalls(sharedGateway, vsys)
                    .collect(ImmutableList.toImmutableList()))
            .build();

    // Valid, so should accept vsys->shared gateway flow
    assertThat(
        generatedFilter,
        accepts(
            createFlow(SRC_IP, DST_IP, SRC_INTERFACE_NAME),
            SRC_INTERFACE_NAME,
            createInterVsysCrossZoneFilters(),
            ImmutableMap.of()));
  }

  @Test
  public void testGenerateVsysSharedGatewayCallsMisconfiguredExternal() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);

    Vsys vsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, vsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, vsys);
    externalToZone.setType(Type.EXTERNAL);
    // missing external pointer to sharedGateway
    vsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    // no lines should be returned since externalToZone does not point to sharedGateway
    assertEquals(generateVsysSharedGatewayCalls(sharedGateway, vsys).count(), 0L);
  }

  @Test
  public void testGenerateVsysSharedGatewayCallsMissingExternal() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);

    Vsys vsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalFromZone = new Zone(EXTERNAL_FROM_ZONE_NAME, vsys);
    externalFromZone.setType(Type.LAYER3);
    externalFromZone.getInterfaceNames().add(SRC_INTERFACE_NAME);
    vsys.getZones().put(EXTERNAL_FROM_ZONE_NAME, externalFromZone);
    // missing external zone on vsys

    // no lines should be returned since vsys has no external zone
    assertEquals(generateVsysSharedGatewayCalls(sharedGateway, vsys).count(), 0L);
  }

  @Test
  public void testGenerateVsysSharedGatewayCallsMissingLayer3() {
    Vsys sharedGateway = new Vsys(VSYS_NAME);

    Vsys vsys = new Vsys(EXTERNAL_VSYS_NAME);
    Zone externalToZone = new Zone(EXTERNAL_TO_ZONE_NAME, vsys);
    externalToZone.setType(Type.EXTERNAL);
    externalToZone.getExternalNames().add(VSYS_NAME);
    vsys.getZones().put(EXTERNAL_TO_ZONE_NAME, externalToZone);

    // no lines should be returned since externalToZone has no layer-3 zone
    assertEquals(generateVsysSharedGatewayCalls(sharedGateway, vsys).count(), 0L);
  }

  /** Covers universal and default case of no rule-type */
  @Test
  public void testSecurityRuleAppliesRuleTypeUniversal() {
    SecurityRule rule = new SecurityRule("rule", new Vsys(VSYS_NAME));
    rule.getFrom().addAll(ImmutableList.of("A", "B"));
    rule.getTo().addAll(ImmutableList.of("C", "B"));

    Warnings w = new Warnings();

    assertTrue(securityRuleApplies("A", "B", rule, w));
    assertTrue(securityRuleApplies("A", "C", rule, w));
    assertTrue(securityRuleApplies("B", "B", rule, w));

    assertFalse(securityRuleApplies("A", "A", rule, w));
    assertFalse(securityRuleApplies("C", "A", rule, w));
    assertFalse(securityRuleApplies("A", "D", rule, w));
  }

  @Test
  public void testSecurityRuleAppliesRuleTypeInterzone() {
    SecurityRule rule = new SecurityRule("rule", new Vsys(VSYS_NAME));
    rule.getFrom().addAll(ImmutableList.of("A", "B"));
    rule.getTo().addAll(ImmutableList.of("C", "B"));
    rule.setRuleType(RuleType.INTERZONE);

    Warnings w = new Warnings();

    assertTrue(securityRuleApplies("A", "C", rule, w));
    assertTrue(securityRuleApplies("A", "B", rule, w));

    assertFalse(securityRuleApplies("A", "A", rule, w));
    assertFalse(securityRuleApplies("B", "B", rule, w));
    assertFalse(securityRuleApplies("A", "D", rule, w));
  }

  @Test
  public void testSecurityRuleAppliesRuleTypeIntrazone() {
    SecurityRule rule = new SecurityRule("rule", new Vsys(VSYS_NAME));
    rule.getFrom().addAll(ImmutableList.of("A", "B"));
    rule.getTo().addAll(ImmutableList.of("A", "B"));
    rule.setRuleType(RuleType.INTRAZONE);

    Warnings w = new Warnings();

    assertTrue(securityRuleApplies("A", "A", rule, w));
    assertFalse(securityRuleApplies("A", "B", rule, w));
    assertFalse(securityRuleApplies("D", "D", rule, w));
  }

  @Test
  public void testCheckIntrazoneValidityAndWarn() {
    SecurityRule rule = new SecurityRule("rule", new Vsys(VSYS_NAME));
    rule.getFrom().addAll(ImmutableList.of("A", "B"));
    rule.getTo().addAll(ImmutableList.of("A", "B"));
    Warnings w = new Warnings(true, true, true);

    // non-intrazone
    assertTrue(checkIntrazoneValidityAndWarn(rule, w));
    assertThat(w.getRedFlagWarnings(), empty());

    // valid intrazone
    rule.setRuleType(RuleType.INTRAZONE);
    assertTrue(checkIntrazoneValidityAndWarn(rule, w));
    assertThat(w.getRedFlagWarnings(), empty());

    // invalid intrazone
    rule.getTo().add("C");
    assertFalse(checkIntrazoneValidityAndWarn(rule, w));
    assertThat(w, hasRedFlag(hasText(containsString("Skipping invalid intrazone security rule"))));
  }
}
