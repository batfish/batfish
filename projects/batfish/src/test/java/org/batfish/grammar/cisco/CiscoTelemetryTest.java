package org.batfish.grammar.cisco;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.config.Settings;
import org.batfish.main.Batfish;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoTelemetryTest {

  private static void parse(String code) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);

    CiscoCombinedParser parser = new CiscoCombinedParser(code, settings);
    ParserRuleContext ctx =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    assertThat(ctx, notNullValue());
  }

  @Test
  public void testTelemetryParsingExample1() {
    String config =
        """
        telemetry ietf subscription 101
          encoding encode-kvgpb
          filter xpath /memory-ios-xe-oper:memory-statistics/memory-statistic
          stream yang-push
          update-policy periodic 6000
          source-vrf Mgmt-intf
          source-address 192.0.2.1
          receiver ip address 10.28.35.45 57555 protocol grpc-tcp
        """;

    parse(config);
  }

  @Test
  public void testTelemetryParsingExample2() {
    String config =
        """
        telemetry ietf subscription 8
          stream yang-push
          filter xpath /iosxe-oper:ios-oper-db/hwidb-table
          update-policy on-change
          encoding encode-kvgpb
          receiver ip address 10.22.22.45 45000 protocol grpc_tls profile secure_profile
        """;

    parse(config);
  }
}
