package org.batfish.grammar.palo_alto;

import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasAdministrativeCost;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasNextHopInterface;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.VrfMatchers.hasInterfaces;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.palo_alto.PaloAltoStructureType;
import org.batfish.representation.palo_alto.PaloAltoStructureUsage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PaloAltoGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname);
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    return getBatfishForConfigurationNames(configurationNames).loadConfigurations();
  }

  @Test
  public void testDnsServerInvalid() throws IOException {
    _thrown.expect(BatfishException.class);
    String hostname = "dns-server-invalid";

    // This should throw a BatfishException due to a malformed IP address
    parseConfig(hostname);
  }

  @Test
  public void testDnsServers() throws IOException {
    String hostname = "dns-server";
    Configuration c = parseConfig(hostname);

    // Confirm both dns servers show up
    assertThat(c.getDnsServers(), containsInAnyOrder("1.9.10.99", "100.199.200.255"));
  }

  @Test
  public void testFilesystemConfigFormat() throws IOException {
    String hostname = "config-filesystem-format";
    Configuration c = parseConfig(hostname);

    // Confirm alternate config format is parsed and extracted properly
    // Confirm config devices set-line extraction works
    assertThat(c, hasHostname(equalTo(hostname)));
    // Confirm general config set-line extraction works
    assertThat(c.getLoggingServers(), contains("2.2.2.2"));
  }

  @Test
  public void testHostname() throws IOException {
    String filename = "basic-parsing";
    String hostname = "my-hostname";

    // Confirm hostname extraction works
    assertThat(parseTextConfigs(filename).keySet(), contains(hostname));
  }

  @Test
  public void testInterface() throws IOException {
    String hostname = "interface";
    String interfaceName1 = "ethernet1/1";
    String interfaceName2 = "ethernet1/2";
    String interfaceName3 = "ethernet1/3";
    Configuration c = parseConfig(hostname);

    // Confirm interface MTU is extracted
    assertThat(c, hasInterface(interfaceName1, hasMtu(9001)));

    // Confirm address is extracted
    assertThat(
        c,
        hasInterface(
            interfaceName1, hasAllAddresses(contains(new InterfaceAddress("1.1.1.1/24")))));

    // Confirm comments are extracted
    assertThat(c, hasInterface(interfaceName1, hasDescription("description")));
    assertThat(c, hasInterface(interfaceName2, hasDescription("interface's long description")));
    assertThat(c, hasInterface(interfaceName3, hasDescription("single quoted description")));

    // Confirm link status is extracted
    assertThat(c, hasInterface(interfaceName1, isActive()));
    assertThat(c, hasInterface(interfaceName2, not(isActive())));
    assertThat(c, hasInterface(interfaceName3, isActive()));
  }

  @Test
  public void testInterfaceReference() throws IOException {
    String hostname = "interface-reference";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse();

    // Confirm reference counts are correct for both used and unused structures
    assertThat(ccae, hasNumReferrers(hostname, PaloAltoStructureType.INTERFACE, "ethernet1/1", 1));
    assertThat(
        ccae, hasNumReferrers(hostname, PaloAltoStructureType.INTERFACE, "ethernet1/unused", 0));

    // Confirm undefined reference is detected
    assertThat(
        ccae,
        hasUndefinedReference(
            hostname,
            PaloAltoStructureType.INTERFACE,
            "ethernet1/undefined",
            PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE));
  }

  @Test
  public void testLogSettingsSyslog() throws IOException {
    String hostname = "log-settings-syslog";
    Configuration c = parseConfig(hostname);

    // Confirm all the defined syslog servers show up in VI model
    assertThat(c.getLoggingServers(), containsInAnyOrder("1.1.1.1", "2.2.2.2", "3.3.3.3"));
  }

  @Test
  public void testNestedConfig() throws IOException {
    String hostname = "nested-config";

    // Confirm a simple extraction (hostname) works for nested config format
    assertThat(parseTextConfigs(hostname).keySet(), contains(hostname));
  }

  @Test
  public void testNestedConfigLineMap() throws IOException {
    String hostname = "nested-config";
    Flattener flattener =
        Batfish.flatten(
            CommonUtil.readResource(TESTCONFIGS_PREFIX + hostname),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            ConfigurationFormat.PALO_ALTO_NESTED,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER);
    FlattenerLineMap lineMap = flattener.getOriginalLineMap();
    /*
     * Flattened config should be two lines: header line and set-hostname line
     * This test is only checking content of the set-hostname line
     */
    String setLineText = flattener.getFlattenedConfigurationText().split("\n", -1)[1];

    /* Confirm original line numbers are preserved */
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("deviceconfig")), equalTo(1));
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("system")), equalTo(2));
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("hostname")), equalTo(3));
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("nested-config")), equalTo(3));
  }

  @Test
  public void testNestedConfigStructureDef() throws IOException {
    String hostname = "nested-config-structure-def";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse();

    // Confirm defined structures in nested config show up with original definition line numbers
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            hostname, PaloAltoStructureType.INTERFACE, "ethernet1/1", contains(8, 9, 10, 11, 12)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            hostname, PaloAltoStructureType.INTERFACE, "ethernet1/2", contains(8, 16, 17, 18, 19)));
  }

  @Test
  public void testNtpServers() throws IOException {
    String hostname = "ntp-server";
    Configuration c = parseConfig(hostname);

    // Confirm both ntp servers show up
    assertThat(c.getNtpServers(), containsInAnyOrder("1.1.1.1", "ntpservername"));
  }

  @Test
  public void testStaticRoute() throws IOException {
    String hostname = "static-route";
    String vrName = "somename";
    Configuration c = parseConfig(hostname);

    // Confirm static route shows up with correct extractions
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasAdministrativeCost(equalTo(123))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasMetric(equalTo(12L))))));
    assertThat(
        c, hasVrf(vrName, hasStaticRoutes(hasItem(hasNextHopIp(equalTo(new Ip("1.1.1.1")))))));
    assertThat(
        c, hasVrf(vrName, hasStaticRoutes(hasItem(hasNextHopInterface(equalTo("ethernet1/1"))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasPrefix(Prefix.parse("0.0.0.0/0"))))));
  }

  @Test
  public void testStaticRouteDefaults() throws IOException {
    String hostname = "static-route-defaults";
    String vrName = "default";
    Configuration c = parseConfig(hostname);

    // Confirm static route shows up with correct defaults
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasAdministrativeCost(equalTo(10))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasMetric(equalTo(10L))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasPrefix(Prefix.parse("0.0.0.0/0"))))));
  }

  @Test
  public void testVirtualRouterInterfaces() throws IOException {
    String hostname = "virtual-router-interfaces";
    Configuration c = parseConfig(hostname);

    assertThat(c, hasVrf("default", hasInterfaces(hasItem("ethernet1/1"))));
    assertThat(c, hasVrf("somename", hasInterfaces(hasItems("ethernet1/2", "ethernet1/3"))));
    assertThat(c, hasVrf("someothername", hasInterfaces(emptyIterable())));
  }
}
