package org.batfish.question.snmpcommunityclients;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.SpecifierContext;

/** An answerer for {@link SnmpCommunityClientsQuestion}. */
public class SnmpCommunityClientsAnswerer extends Answerer {
  @VisibleForTesting static final String COL_NODE = "Node";
  @VisibleForTesting static final String COL_COMMUNITY = "Community";
  @VisibleForTesting static final String COL_REASON = "Reason";

  public enum Reason {
    NO_SUCH_COMMUNITY,
    UNEXPECTED_CLIENTS,
    UNSUPPORTED_DEVICE,
  }

  private final SnmpCommunityClientsQuestion _question;

  public SnmpCommunityClientsAnswerer(SnmpCommunityClientsQuestion question, IBatfish batfish) {
    super(question, batfish);
    checkNotNull(question.getCommunity(), "SNMP community must be specified");
    _question = question;
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext ctxt = _batfish.specifierContext(snapshot);
    SortedMap<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);

    String community = _question.getCommunity();
    IpSpace expectedIps = _question.getClientsIpSpaceSpecifier().resolve(ctxt);
    BDDPacket bddPacket = new BDDPacket();
    BDD expectedBDD = bddPacket.getDstIpSpaceToBDD().visit(expectedIps);

    TableMetadata tableMetadata = metadata();
    Multiset<Row> rows =
        _question.getNodeSpecifier().resolve(ctxt).stream()
            .map(configurations::get)
            .filter(SnmpCommunityClientsAnswerer::isConfigurationInScope)
            .map(c -> getRow(expectedBDD, c, community, bddPacket))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement answer = new TableAnswerElement(tableMetadata);
    answer.postProcessAnswer(_question, rows);
    return answer;
  }

  /** Return the result row for the configuration. The optional is empty if test passes. */
  @VisibleForTesting
  static Optional<Row> getRow(
      BDD expectedBdd, Configuration c, String community, BDDPacket packet) {

    Row.RowBuilder rowBuilder =
        Row.builder(metadata().toColumnMap())
            .put(COL_NODE, new Node(c.getHostname()))
            .put(COL_COMMUNITY, community);

    if (!isConfigurationSupported(c)) {
      return Optional.of(rowBuilder.put(COL_REASON, Reason.UNSUPPORTED_DEVICE).build());
    }

    Optional<SnmpCommunity> maybeCommunity =
        Optional.ofNullable(c.getDefaultVrf())
            .map(Vrf::getSnmpServer)
            .map(SnmpServer::getCommunities)
            .map(comms -> comms.get(community));
    if (!maybeCommunity.isPresent()) {
      return Optional.of(rowBuilder.put(COL_REASON, Reason.NO_SUCH_COMMUNITY).build());
    }

    SnmpCommunity snmpCommunity = maybeCommunity.get();

    IpSpace actualClientSpace = firstNonNull(snmpCommunity.getClientIps(), EmptyIpSpace.INSTANCE);
    BDD actualBDD =
        new IpSpaceToBDD(packet.getDstIpSpaceToBDD(), c.getIpSpaces()).visit(actualClientSpace);
    if (!actualBDD.equals(expectedBdd)) {
      return Optional.of(rowBuilder.put(COL_REASON, Reason.UNEXPECTED_CLIENTS).build());
    }

    return Optional.empty();
  }

  /** Create table metadata for the answer */
  private static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.NODE, "Hostname.", true, false),
            new ColumnMetadata(COL_COMMUNITY, Schema.STRING, "The community name.", false, true),
            new ColumnMetadata(COL_REASON, Schema.STRING, "Result of the test.", false, true));
    return new TableMetadata(columnMetadata);
  }

  /**
   * Indicates whether this configuration is in scope for this question.
   *
   * <p>Auto-generated configs and AWS are out of scope.
   */
  private static boolean isConfigurationInScope(Configuration c) {
    return c.getConfigurationFormat() != ConfigurationFormat.AWS
        && c.getDeviceModel() != DeviceModel.BATFISH_INTERNET
        && c.getDeviceModel() != DeviceModel.BATFISH_ISP;
  }

  /**
   * Indicates whether this configuration is supported for this question.
   *
   * <p>For now, only Arista, Juniper, and SONiC are supported.
   */
  static boolean isConfigurationSupported(Configuration c) {
    return c.getConfigurationFormat() == ConfigurationFormat.ARISTA
        || c.getConfigurationFormat() == ConfigurationFormat.CISCO_NX
        || c.getConfigurationFormat() == ConfigurationFormat.JUNIPER
        || c.getConfigurationFormat() == ConfigurationFormat.JUNIPER_SWITCH
        || c.getConfigurationFormat() == ConfigurationFormat.FLAT_JUNIPER
        || c.getConfigurationFormat() == ConfigurationFormat.SONIC;
  }
}
