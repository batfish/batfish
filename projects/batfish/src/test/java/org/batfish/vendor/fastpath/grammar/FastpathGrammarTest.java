package org.batfish.vendor.fastpath.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.FASTPATH;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.fastpath.representation.Aaa;
import org.batfish.vendor.fastpath.representation.AaaMethod;
import org.batfish.vendor.fastpath.representation.Accounting;
import org.batfish.vendor.fastpath.representation.AccountingType;
import org.batfish.vendor.fastpath.representation.Authentication;
import org.batfish.vendor.fastpath.representation.AuthenticationType;
import org.batfish.vendor.fastpath.representation.Authorization;
import org.batfish.vendor.fastpath.representation.AuthorizationType;
import org.batfish.vendor.fastpath.representation.Dns;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;
import org.batfish.vendor.fastpath.representation.Logging;
import org.batfish.vendor.fastpath.representation.LoggingBuffered;
import org.batfish.vendor.fastpath.representation.LoggingServer;
import org.batfish.vendor.fastpath.representation.Sntp;
import org.batfish.vendor.fastpath.representation.Tacacs;
import org.batfish.vendor.fastpath.representation.TacacsServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public final class FastpathGrammarTest {

  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/vendor/fastpath/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private @Nonnull FastpathConfiguration parseVendorConfig(String hostname) {
    return parseVendorConfigText(
        readResource(TESTCONFIGS_PREFIX + hostname, UTF_8), TESTCONFIGS_PREFIX + hostname);
  }

  /** Parses {@code src} directly as a FastPath config, attributing it to {@code name}. */
  private @Nonnull FastpathConfiguration parseVendorConfigText(String src, String name) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    FastpathCombinedParser parser = new FastpathCombinedParser(src, settings);
    Warnings warnings = new Warnings();
    FastpathControlPlaneExtractor extractor =
        new FastpathControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    FastpathConfiguration vc = (FastpathConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(name);

    // Crash if the vendor model is not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }

  @Test
  public void testTacacsParsesWithoutWarnings() {
    FastpathConfiguration c = parseVendorConfig("fastpath_tacacs");
    assertThat(c.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testTacacs() throws IOException {
    Configuration c = parseConfig("fastpath_tacacs");
    assertThat(
        c.getTacacsServers(),
        equalTo(ImmutableSet.of("1.1.1.1", "2.2.2.2", "tacacs.example.com", "3.3.3.3", "4.4.4.4")));
    assertThat(c.getTacacsSourceInterface(), equalTo("serviceport"));
  }

  @Test
  public void testTacacsExtraction() {
    Tacacs tacacs = parseVendorConfig("fastpath_tacacs").getTacacs();
    assertThat(
        tacacs.getServers().keySet(),
        equalTo(ImmutableSet.of("1.1.1.1", "2.2.2.2", "tacacs.example.com", "3.3.3.3", "4.4.4.4")));
    assertThat(tacacs.getSourceInterface(), equalTo("serviceport"));
    assertThat(tacacs.getTimeout(), equalTo(10));
    assertThat(tacacs.getKeyEncrypted(), equalTo(true));

    TacacsServer server1 = tacacs.getServers().get("1.1.1.1");
    assertThat(server1.getPort(), nullValue());
    assertThat(server1.getPriority(), nullValue());
    assertThat(server1.getTimeout(), nullValue());
    assertThat(server1.getKeyEncrypted(), equalTo(false));

    TacacsServer server2 = tacacs.getServers().get("2.2.2.2");
    assertThat(server2.getPort(), nullValue());
    assertThat(server2.getPriority(), nullValue());
    assertThat(server2.getTimeout(), nullValue());
    assertThat(server2.getKeyEncrypted(), equalTo(false));

    TacacsServer server3 = tacacs.getServers().get("tacacs.example.com");
    assertThat(server3.getPort(), nullValue());
    assertThat(server3.getPriority(), nullValue());
    assertThat(server3.getTimeout(), nullValue());
    assertThat(server3.getKeyEncrypted(), nullValue());

    TacacsServer server4 = tacacs.getServers().get("3.3.3.3");
    assertThat(server4.getPort(), equalTo(49));
    assertThat(server4.getPriority(), equalTo(1));
    assertThat(server4.getTimeout(), equalTo(10));
    assertThat(server4.getKeyEncrypted(), equalTo(true));

    TacacsServer server5 = tacacs.getServers().get("4.4.4.4");
    assertThat(server5.getPort(), nullValue());
    assertThat(server5.getPriority(), nullValue());
    assertThat(server5.getTimeout(), nullValue());
    assertThat(server5.getKeyEncrypted(), equalTo(false));
  }

  @Test
  public void testTacacsSourceInterfaceForms() {
    // Covers the source-interface forms in toInterfaceName that aren't covered by tacacs testconfig
    // The source-interface is single-valued, so each form is parsed as its own snippet
    assertThat(
        tacacsSourceInterface("tacacs-server source-interface loopback 0\n"),
        equalTo("loopback 0"));
    assertThat(tacacsSourceInterface("tacacs-server source-interface 0/1\n"), equalTo("0/1"));
    assertThat(tacacsSourceInterface("tacacs-server source-interface 1/0/1\n"), equalTo("1/0/1"));
    assertThat(
        tacacsSourceInterface("tacacs-server source-interface tunnel 5\n"), equalTo("tunnel 5"));
    assertThat(
        tacacsSourceInterface("tacacs-server source-interface vlan 10\n"), equalTo("vlan 10"));
  }

  private @Nonnull String tacacsSourceInterface(String tacacsLine) {
    return parseVendorConfigText(tacacsLine, "source_interface").getTacacs().getSourceInterface();
  }

  @Test
  public void testAaaParsesWithoutWarnings() {
    FastpathConfiguration c = parseVendorConfig("fastpath_aaa");
    assertThat(c.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testAaaExtraction() {
    Aaa aaa = parseVendorConfig("fastpath_aaa").getAaa();

    Authentication login = aaa.getAuthentication().get(AuthenticationType.LOGIN).get("user-authen");
    assertThat(login.getType(), equalTo(AuthenticationType.LOGIN));
    assertThat(login.getMethods(), equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.LOCAL)));
    Authentication enable =
        aaa.getAuthentication().get(AuthenticationType.ENABLE).get("user-enable");
    assertThat(enable.getType(), equalTo(AuthenticationType.ENABLE));
    assertThat(
        enable.getMethods(),
        equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.ENABLE, AaaMethod.RADIUS)));

    Authorization authorizationCommands =
        aaa.getAuthorization().get(AuthorizationType.COMMANDS).get("user-auth");
    assertThat(authorizationCommands.getType(), equalTo(AuthorizationType.COMMANDS));
    assertThat(
        authorizationCommands.getMethods(),
        equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.NONE)));
    Authorization authorizationExec =
        aaa.getAuthorization().get(AuthorizationType.EXEC).get("user-auth");
    assertThat(authorizationExec.getType(), equalTo(AuthorizationType.EXEC));
    assertThat(
        authorizationExec.getMethods(),
        equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.NONE)));
    Authorization authorizationExecLocal =
        aaa.getAuthorization().get(AuthorizationType.EXEC).get("execLocal");
    assertThat(
        authorizationExecLocal.getMethods(),
        equalTo(ImmutableList.of(AaaMethod.LOCAL, AaaMethod.NONE)));

    Accounting exec = aaa.getAccounting().get(AccountingType.EXEC).get("user-acct");
    assertThat(exec.getType(), equalTo(AccountingType.EXEC));
    assertThat(exec.getRecordType(), equalTo(Accounting.RecordType.START_STOP));
    assertThat(exec.getMethods(), equalTo(ImmutableList.of(AaaMethod.TACACS)));
    Accounting commands = aaa.getAccounting().get(AccountingType.COMMANDS).get("user-acct");
    assertThat(commands.getType(), equalTo(AccountingType.COMMANDS));
    assertThat(commands.getRecordType(), equalTo(Accounting.RecordType.STOP_ONLY));
    assertThat(commands.getMethods(), equalTo(ImmutableList.of(AaaMethod.TACACS)));
  }

  @Test
  public void testAaaListNameFormsExtraction() {
    // The list name may be the `default` keyword, a quoted string, or a bare word. `default` is
    // recorded as the literal name "default".
    Aaa aaa = parseVendorConfig("fastpath_aaa").getAaa();

    Authentication loginDefault =
        aaa.getAuthentication().get(AuthenticationType.LOGIN).get("default");
    assertThat(
        loginDefault.getMethods(), equalTo(ImmutableList.of(AaaMethod.LINE, AaaMethod.NONE)));
    // A bare name that starts with `default` is a name, not the keyword.
    Authentication defaultList =
        aaa.getAuthentication().get(AuthenticationType.LOGIN).get("defaultList");
    assertThat(defaultList.getMethods(), equalTo(ImmutableList.of(AaaMethod.LOCAL)));
    Authentication enableNetList =
        aaa.getAuthentication().get(AuthenticationType.ENABLE).get("enableNetList");
    assertThat(
        enableNetList.getMethods(), equalTo(ImmutableList.of(AaaMethod.ENABLE, AaaMethod.DENY)));

    Accounting execList = aaa.getAccounting().get(AccountingType.EXEC).get("ExecList");
    assertThat(execList.getRecordType(), equalTo(Accounting.RecordType.START_STOP));
    assertThat(
        execList.getMethods(), equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.RADIUS)));

    // `none` disables accounting for the list and carries no method.
    Accounting dot1x = aaa.getAccounting().get(AccountingType.DOT1X).get("default");
    assertThat(dot1x.getType(), equalTo(AccountingType.DOT1X));
    assertThat(dot1x.getRecordType(), equalTo(Accounting.RecordType.NONE));
    assertThat(dot1x.getMethods(), empty());

    // radius is the only method dot1x accounting accepts.
    Accounting dot1xRadius =
        parseVendorConfigText("aaa accounting dot1x default start-stop radius\n", "aaa_dot1x")
            .getAaa()
            .getAccounting()
            .get(AccountingType.DOT1X)
            .get("default");
    assertThat(dot1xRadius.getRecordType(), equalTo(Accounting.RecordType.START_STOP));
    assertThat(dot1xRadius.getMethods(), equalTo(ImmutableList.of(AaaMethod.RADIUS)));
  }

  @Test
  public void testAaaListRedefinitionReplaces() {
    // Re-issuing the command for the same type and list name replaces the record type and the
    // method list; it does not append to it.
    Aaa aaa =
        parseVendorConfigText(
                """
                aaa accounting exec ExecList stop-only tacacs
                aaa accounting exec ExecList start-stop tacacs radius
                aaa authentication login "user-authen" tacacs
                aaa authentication login "user-authen" local none
                """,
                "aaa_redefinition")
            .getAaa();

    Accounting exec = aaa.getAccounting().get(AccountingType.EXEC).get("ExecList");
    assertThat(exec.getRecordType(), equalTo(Accounting.RecordType.START_STOP));
    assertThat(exec.getMethods(), equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.RADIUS)));

    Authentication login = aaa.getAuthentication().get(AuthenticationType.LOGIN).get("user-authen");
    assertThat(login.getMethods(), equalTo(ImmutableList.of(AaaMethod.LOCAL, AaaMethod.NONE)));
  }

  @Test
  public void testAaaIasUserBlockIsSilent() {
    FastpathConfiguration c =
        parseVendorConfigText(
            """
            aaa ias-user username client-1
            password a45c74fdf50a558a2b5cf05573cd633bac2c6c598d54497ad4c46104918f2c encrypted
            exit
            hostname "after-block"
            """,
            "aaa_ias_user");
    assertThat(c.getWarnings().getParseWarnings(), empty());
    // The block terminates, so a following global command is still parsed.
    assertThat(c.getHostname(), equalTo("after-block"));
  }

  @Test
  public void testCompleteConfigAaaExtraction() throws IOException {
    // AAA has no vendor-independent representation yet, so pin the two method lists in
    // fastpath_complete_config on the vendor model.
    String hostname = "fastpath_complete_config";
    Batfish batfish = getBatfishAllowUnrecognized(hostname);
    FastpathConfiguration vc =
        (FastpathConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);
    Aaa aaa = vc.getAaa();
    assertThat(
        aaa.getAuthentication().get(AuthenticationType.LOGIN).get("net-authen").getMethods(),
        equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.LOCAL)));
    assertThat(
        aaa.getAuthentication().get(AuthenticationType.ENABLE).get("net-enable").getMethods(),
        equalTo(ImmutableList.of(AaaMethod.TACACS, AaaMethod.ENABLE)));
  }

  @Test
  public void testHostnameExtraction() {
    FastpathConfiguration c = parseVendorConfig("fastpath_hostname");
    assertThat(c.getHostname(), equalTo("fastpath_hostname"));
  }

  @Test
  public void testSetPromptExtraction() {
    // Legacy Quanta variant.
    FastpathConfiguration c = parseVendorConfig("fastpath_set_prompt");
    assertThat(c.getHostname(), equalTo("fastpath_set_prompt"));
  }

  @Test
  public void testBannerIsIgnored() {
    // The `!System ...` header banner is a comment block and must not break parsing.
    FastpathConfiguration c = parseVendorConfig("fastpath_banner");
    assertThat(c.getHostname(), equalTo("fastpath_banner"));
  }

  @Test
  public void testToVendorIndependentConfiguration() {
    FastpathConfiguration vc = parseVendorConfig("fastpath_hostname");
    vc.setVendor(FASTPATH);
    List<Configuration> configs = vc.toVendorIndependentConfigurations();
    Configuration c = getOnlyElement(configs);
    assertThat(c, hasHostname("fastpath_hostname"));
    assertThat(c.getDefaultVrf(), notNullValue());
  }

  @Test
  public void testHostnameEndToEnd() throws IOException {
    // Exercises the full pipeline: format detection -> parse -> convert. The config's banner is
    // what routes it to FASTPATH, so this also guards the detector/parser-job wiring.
    Configuration c = parseConfig("fastpath_hostname");
    assertThat(c, hasConfigurationFormat(FASTPATH));
    assertThat(c.getHostname(), equalTo("fastpath_hostname"));
  }

  @Test
  public void testDns() throws IOException {
    Configuration c = parseConfig("fastpath_dns");
    assertThat(c.getDnsServers(), equalTo(ImmutableSet.of("1.1.1.1", "1.1.1.2")));
    assertThat(c.getDomainName(), equalTo("example.com"));
    assertThat(c.getDnsSourceInterface(), equalTo("serviceport"));
  }

  @Test
  public void testDnsExtraction() {
    Dns dns = parseVendorConfig("fastpath_dns").getDns();
    assertThat(dns.getDomainName(), equalTo("example.com"));
    assertThat(dns.getServers(), equalTo(ImmutableSet.of("1.1.1.1", "1.1.1.2")));
    assertThat(dns.getSourceInterface(), equalTo("serviceport"));
    assertThat(dns.getLookupEnabled(), equalTo(false));
  }

  @Test
  public void testDnsNullCommandsAreSilent() {
    FastpathConfiguration c = parseVendorConfig("fastpath_dns");
    assertThat(c.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testDnsLookup() {
    // Tri-state: unconfigured -> null (device default is enabled); explicit `ip domain lookup` ->
    // TRUE; explicit `no ip domain lookup` -> FALSE.
    assertThat(
        parseVendorConfigText("ip domain name \"x.com\"\n", "dns_lookup")
            .getDns()
            .getLookupEnabled(),
        nullValue());
    assertThat(
        parseVendorConfigText("ip domain lookup\n", "dns_lookup").getDns().getLookupEnabled(),
        equalTo(true));
    assertThat(
        parseVendorConfigText("no ip domain lookup\n", "dns_lookup").getDns().getLookupEnabled(),
        equalTo(false));
  }

  @Test
  public void testUnquotedValuesTolerated() {
    // FastPath's CLI accepts unquoted domain/host values (show running-config renders them
    // quoted); both forms must parse and extract identically.
    assertThat(
        parseVendorConfigText("ip domain name example.com\n", "dns_unquoted")
            .getDns()
            .getDomainName(),
        equalTo("example.com"));
    assertThat(
        parseVendorConfigText("sntp server pool.ntp.org\n", "sntp_unquoted").getSntp().getServers(),
        equalTo(ImmutableSet.of("pool.ntp.org")));
    assertThat(
        parseVendorConfigText("logging host loghost.example.com\n", "logging_unquoted")
            .getLogging()
            .getServers()
            .keySet(),
        equalTo(ImmutableSet.of("loghost.example.com")));
  }

  @Test
  public void testSntp() throws IOException {
    Configuration c = parseConfig("fastpath_sntp");
    assertThat(c.getNtpServers(), equalTo(ImmutableSet.of("100.104.96.2", "100.104.98.2")));
    assertThat(c.getNtpSourceInterface(), equalTo("serviceport"));
  }

  @Test
  public void testSntpExtraction() {
    Sntp sntp = parseVendorConfig("fastpath_sntp").getSntp();
    assertThat(sntp.getServers(), equalTo(ImmutableSet.of("100.104.96.2", "100.104.98.2")));
    assertThat(sntp.getClientMode(), equalTo(Sntp.ClientMode.UNICAST));
    assertThat(sntp.getClientPort(), equalTo(123));
    assertThat(sntp.getSourceInterface(), equalTo("serviceport"));
  }

  @Test
  public void testSntpNullCommandsAreSilent() {
    FastpathConfiguration c = parseVendorConfig("fastpath_sntp");
    assertThat(c.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testSntpClientPortOutOfRangeWarns() {
    FastpathConfiguration c =
        parseVendorConfigText("sntp client port 0\n", "sntp_client_port_invalid");
    assertThat(
        c.getWarnings().getParseWarnings(),
        hasItem(hasComment(containsString("Expected sntp client port in range"))));
    assertThat(c.getSntp().getClientPort(), nullValue());
  }

  @Test
  public void testSntpServerStatusLeakageTolerated() {
    // Some software versions leak operational `sntp server status is ...` show-output into the
    // config between real server lines. It must be tolerated silently (parsed into a _null rule).
    FastpathConfiguration c =
        parseVendorConfigText(
            "sntp server status is Server Kiss Of Death\nsntp server \"76.223.76.249\"\n",
            "sntp_status_leak");
    assertThat(c.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testLoggingExtraction() {
    Logging logging = parseVendorConfig("fastpath_logging").getLogging();

    Map<String, LoggingServer> servers = logging.getServers();
    assertThat(
        servers.keySet(),
        equalTo(ImmutableSet.of("10.0.0.1", "10.0.0.2", "2001:db8::1", "loghost.example.com")));

    LoggingServer s1 = servers.get("10.0.0.1");
    assertThat(s1.getAddressType(), equalTo(LoggingServer.AddressType.IPV4));
    assertThat(s1.getPort(), equalTo(514));
    assertThat(s1.getSeverityLevel(), equalTo(7)); // debug

    LoggingServer s2 = servers.get("10.0.0.2");
    assertThat(s2.getAddressType(), equalTo(LoggingServer.AddressType.DNS));
    assertThat(s2.getPort(), equalTo(1514));
    assertThat(s2.getSeverityLevel(), equalTo(4)); // warning

    LoggingServer s3 = servers.get("loghost.example.com");
    assertThat(s3.getAddressType(), equalTo(LoggingServer.AddressType.DNS));
    assertThat(s3.getPort(), nullValue());
    assertThat(s3.getSeverityLevel(), nullValue());

    LoggingServer s4 = servers.get("2001:db8::1");
    assertThat(s4.getAddressType(), equalTo(LoggingServer.AddressType.IPV6));
    assertThat(s4.getPort(), equalTo(514));
    assertThat(s4.getSeverityLevel(), equalTo(7)); // debug

    LoggingBuffered buffered = logging.getBuffered();
    assertThat(buffered, notNullValue());
    assertThat(buffered.getEnabled(), equalTo(true));
    assertThat(buffered.getWrap(), equalTo(true));
    assertThat(buffered.getSeverity(), equalTo(7)); // debug

    // Other settings.
    assertThat(logging.getPersistentSeverity(), equalTo(4)); // warning
    assertThat(logging.getConsoleEnabled(), equalTo(true));
    assertThat(logging.getConsoleSeverity(), equalTo(3)); // error
    assertThat(logging.getCliCommand(), equalTo(true));
    assertThat(logging.getSyslogEnabled(), equalTo(true));
    assertThat(logging.getSourceInterface(), equalTo("loopback 0"));
  }

  @Test
  public void testLoggingNullCommandsAreSilent() {
    FastpathConfiguration c = parseVendorConfig("fastpath_logging");
    assertThat(c.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testNoLoggingConsole() {
    assertThat(
        parseVendorConfigText("no logging console\n", "no_logging_console")
            .getLogging()
            .getConsoleEnabled(),
        equalTo(false));
  }

  @Test
  public void testLoggingSourceInterfaceForms() {
    // Cover the source-interface forms in toInterfaceName. loopback ("loopback 0") is exercised by
    // testLoggingExtraction; here we cover slot/port (incl. multi-slash), tunnel, and vlan. Because
    // the source-interface is a single-valued field, each form is parsed as its own snippet.
    assertThat(sourceInterface("logging syslog source-interface 1/0/1\n"), equalTo("1/0/1"));
    assertThat(sourceInterface("logging syslog source-interface tunnel 5\n"), equalTo("tunnel 5"));
    assertThat(sourceInterface("logging syslog source-interface vlan 10\n"), equalTo("vlan 10"));
  }

  private @Nonnull String sourceInterface(String loggingLine) {
    return parseVendorConfigText(loggingLine, "source_interface").getLogging().getSourceInterface();
  }

  @Test
  public void testInvalidLoggingValuesWarn() {
    FastpathConfiguration c = parseVendorConfig("fastpath_logging_invalid");
    assertThat(
        c.getWarnings().getParseWarnings(),
        hasItem(hasComment(containsString("Expected logging severity in range"))));
    assertThat(
        c.getWarnings().getParseWarnings(),
        hasItem(hasComment(containsString("Expected logging host port in range"))));
  }

  @Test
  public void testLoggingServersEndToEnd() throws IOException {
    Configuration c = parseConfig("fastpath_logging");
    assertThat(
        c.getLoggingServers(),
        equalTo(ImmutableSet.of("10.0.0.1", "10.0.0.2", "2001:db8::1", "loghost.example.com")));
    assertThat(c.getLoggingSourceInterface(), equalTo("loopback 0"));
  }

  /**
   * Like {@link #getBatfishForConfigurationNames} but recovers from unrecognized/unsupported lines
   * instead of throwing, as production parsing does. This lets a config that mixes supported
   * management commands with not-yet-modeled lines parse to {@link
   * ParseStatus#PARTIALLY_UNRECOGNIZED} rather than {@code FAILED}.
   */
  private @Nonnull Batfish getBatfishAllowUnrecognized(String hostname) throws IOException {
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    batfish.getSettings().setThrowOnLexerError(false);
    batfish.getSettings().setThrowOnParserError(false);
    return batfish;
  }

  @Test
  public void testFastPathConfig() throws IOException {
    // A real FastPath config: the modeled DNS/NTP/Syslog surface must extract and
    // convert onto the VI configuration end-to-end, even surrounded by not-yet-modeled L2/L3,
    // routing, ACL, AAA, and DHCP lines.
    String hostname = "fastpath_complete_config";
    Batfish batfish = getBatfishAllowUnrecognized(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(c, hasConfigurationFormat(FASTPATH));
    assertThat(c.getHostname(), equalTo(hostname));
    // DNS
    assertThat(c.getDomainName(), equalTo("example.com"));
    assertThat(c.getDnsServers(), equalTo(ImmutableSet.of("1.1.1.1", "1.1.1.2")));
    assertThat(c.getDnsSourceInterface(), equalTo("serviceport"));
    // NTP
    assertThat(c.getNtpServers(), equalTo(ImmutableSet.of("2.2.2.1", "2.2.2.2", "2.2.2.3")));
    assertThat(c.getNtpSourceInterface(), equalTo("serviceport"));
    // Syslog
    assertThat(c.getLoggingServers(), equalTo(ImmutableSet.of("3.3.3.1", "3.3.3.2")));
    assertThat(c.getLoggingSourceInterface(), equalTo("serviceport"));
    // TACACS
    assertThat(c.getTacacsServers(), equalTo(ImmutableSet.of("9.9.9.9")));
  }

  @Test
  public void testUnrecognizedLinesArePartiallyRecognized() throws IOException {
    // Not-yet-modeled lines must not fail the whole parse: the config parses to
    // PARTIALLY_UNRECOGNIZED and the supported lines are still modeled.
    String hostname = "fastpath_complete_config";
    Batfish batfish = getBatfishAllowUnrecognized(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(c, hasHostname(hostname));
    InitInfoAnswerElement initInfo = batfish.initInfo(batfish.getSnapshot(), false, true);
    assertThat(
        initInfo.getParseStatus().get("configs/" + hostname),
        equalTo(ParseStatus.PARTIALLY_UNRECOGNIZED));
  }
}
