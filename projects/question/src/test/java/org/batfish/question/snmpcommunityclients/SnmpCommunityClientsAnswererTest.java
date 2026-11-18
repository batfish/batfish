package org.batfish.question.snmpcommunityclients;

import static org.batfish.question.snmpcommunityclients.SnmpCommunityClientsAnswerer.COL_COMMUNITY;
import static org.batfish.question.snmpcommunityclients.SnmpCommunityClientsAnswerer.COL_NODE;
import static org.batfish.question.snmpcommunityclients.SnmpCommunityClientsAnswerer.COL_REASON;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SnmpCommunityClientsAnswererTest {
  private static final String TESTCONFIGS_DIR = "org/batfish/question/snmpcommunityclients";

  @TempDir public File _folder;

  public IBatfish getBatfish(List<String> filenames) throws IOException {
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder().setConfigurationFiles(TESTCONFIGS_DIR, filenames).build(), _folder);
  }

  @Test
  void testPasses() throws IOException {
    IpSpace expectedIps = Prefix.parse("1.2.3.4/32").toIpSpace();
    BDDPacket bddPacket = new BDDPacket();
    BDD expectedBDD = bddPacket.getDstIpSpaceToBDD().visit(expectedIps);
    IBatfish batfish = getBatfish(ImmutableList.of("arista", "juniper", "nxos"));

    for (Configuration c : batfish.loadConfigurations(batfish.getSnapshot()).values()) {
      assertEquals(
          Optional.empty(), SnmpCommunityClientsAnswerer.getRow(expectedBDD, c, "COMM", bddPacket));
    }
  }

  @Test
  void testMissingCommunity() throws IOException {
    IpSpace expectedIps = Prefix.parse("1.2.3.4/32").toIpSpace();
    BDDPacket bddPacket = new BDDPacket();
    BDD expectedBDD = bddPacket.getDstIpSpaceToBDD().visit(expectedIps);
    IBatfish batfish = getBatfish(ImmutableList.of("arista"));

    assertEquals(
        Optional.of(
            Row.builder()
                .put(COL_NODE, new Node("arista"))
                .put(COL_COMMUNITY, "COMM-Missing")
                .put(COL_REASON, SnmpCommunityClientsAnswerer.Reason.NO_SUCH_COMMUNITY)
                .build()),
        SnmpCommunityClientsAnswerer.getRow(
            expectedBDD,
            batfish.loadConfigurations(batfish.getSnapshot()).get("arista"),
            "COMM-Missing",
            bddPacket));
  }

  @Test
  void testUnexpectedClients() throws IOException {
    IpSpace expectedIps = Prefix.parse("10.20.30.40/32").toIpSpace();
    BDDPacket bddPacket = new BDDPacket();
    BDD expectedBDD = bddPacket.getDstIpSpaceToBDD().visit(expectedIps);
    IBatfish batfish = getBatfish(ImmutableList.of("arista"));

    assertEquals(
        Optional.of(
            Row.builder()
                .put(COL_NODE, new Node("arista"))
                .put(COL_COMMUNITY, "COMM")
                .put(COL_REASON, SnmpCommunityClientsAnswerer.Reason.UNEXPECTED_CLIENTS)
                .build()),
        SnmpCommunityClientsAnswerer.getRow(
            expectedBDD,
            batfish.loadConfigurations(batfish.getSnapshot()).get("arista"),
            "COMM",
            bddPacket));
  }

  @Test
  void testUnsupportedDevice() throws IOException {
    IpSpace expectedIps = Prefix.parse("10.20.30.40/32").toIpSpace();
    BDDPacket bddPacket = new BDDPacket();
    BDD expectedBDD = bddPacket.getDstIpSpaceToBDD().visit(expectedIps);
    IBatfish batfish = getBatfish(ImmutableList.of("ios"));

    assertEquals(
        Optional.of(
            Row.builder()
                .put(COL_NODE, new Node("ios"))
                .put(COL_COMMUNITY, "COMM")
                .put(COL_REASON, SnmpCommunityClientsAnswerer.Reason.UNSUPPORTED_DEVICE)
                .build()),
        SnmpCommunityClientsAnswerer.getRow(
            expectedBDD,
            batfish.loadConfigurations(batfish.getSnapshot()).get("ios"),
            "COMM",
            bddPacket));
  }
}
