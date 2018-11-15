package org.batfish.question.bgpproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.PropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class BgpPeerConfigurationAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_VRF = "VRF";
  public static final String COL_REMOTE_IP = "Remote_IP";

  public BgpPeerConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates {@link ColumnMetadata}s that the answer should have.
   *
   * @return The {@link List} of {@link ColumnMetadata}s
   */
  public static List<ColumnMetadata> createColumnMetadata(
      BgpPeerPropertySpecifier propertySpecifier) {
    return ImmutableList.<ColumnMetadata>builder()
        .add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false))
        .add(new ColumnMetadata(COL_VRF, Schema.STRING, "VRF", true, false))
        .add(new ColumnMetadata(COL_REMOTE_IP, Schema.SELF_DESCRIBING, "Remote IP", true, false))
        .addAll(
            propertySpecifier
                .getMatchingProperties()
                .stream()
                .map(
                    prop ->
                        new ColumnMetadata(
                            getColumnName(prop),
                            BgpPeerPropertySpecifier.JAVA_MAP.get(prop).getSchema(),
                            "Property " + prop,
                            false,
                            true))
                .collect(Collectors.toList()))
        .build();
  }

  /** Creates a {@link TableMetadata} object from the question. */
  static TableMetadata createTableMetadata(BgpPeerConfigurationQuestion question) {
    String textDesc =
        String.format(
            "Properties of BGP peer ${%s}:${%s}: ${%s}", COL_NODE, COL_VRF, COL_REMOTE_IP);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(createColumnMetadata(question.getProperties()), textDesc);
  }

  @Override
  public AnswerElement answer() {
    BgpPeerConfigurationQuestion question = (BgpPeerConfigurationQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodesSpecifier().resolve(_batfish.specifierContext());

    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Multiset<Row> propertyRows =
        getAnswerRows(configurations, nodes, tableMetadata.toColumnMap(), question.getProperties());

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  @VisibleForTesting
  public static Multiset<Row> getAnswerRows(
      Map<String, Configuration> configurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columnMetadata,
      BgpPeerPropertySpecifier propertySpecifier) {

    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      for (Vrf vrf : configurations.get(nodeName).getVrfs().values()) {
        BgpProcess bgpProcess = vrf.getBgpProcess();
        if (bgpProcess == null) {
          continue;
        }
        Node node = new Node(nodeName);
        for (BgpActivePeerConfig peer : bgpProcess.getActiveNeighbors().values()) {
          rows.add(getRow(node, vrf.getName(), peer, columnMetadata, propertySpecifier));
        }
        for (BgpPassivePeerConfig peer : bgpProcess.getPassiveNeighbors().values()) {
          rows.add(getRow(node, vrf.getName(), peer, columnMetadata, propertySpecifier));
        }
      }
    }
    return rows;
  }

  private static Row getRow(
      Node node,
      String vrfName,
      BgpPeerConfig peer,
      Map<String, ColumnMetadata> columnMetadata,
      BgpPeerPropertySpecifier propertySpecifier) {
    RowBuilder rowBuilder =
        Row.builder(columnMetadata)
            .put(COL_NODE, node)
            .put(COL_VRF, vrfName)
            .put(COL_REMOTE_IP, getRemoteIp(peer));
    for (String property : propertySpecifier.getMatchingProperties()) {
      PropertyDescriptor<BgpPeerConfig> propertyDescriptor =
          BgpPeerPropertySpecifier.JAVA_MAP.get(property);
      try {
        PropertySpecifier.fillProperty(propertyDescriptor, peer, property, rowBuilder);
      } catch (ClassCastException e) {
        throw new BatfishException(
            String.format(
                "Type mismatch between property value ('%s') and Schema ('%s') for property '%s' for BGP peer '%s->%s-%s': %s",
                propertyDescriptor.getGetter().apply(peer),
                propertyDescriptor.getSchema(),
                property,
                node.getName(),
                vrfName,
                peer,
                e.getMessage()),
            e);
      }
    }
    return rowBuilder.build();
  }

  @VisibleForTesting
  static SelfDescribingObject getRemoteIp(@Nonnull BgpPeerConfig peer) {
    if (peer instanceof BgpActivePeerConfig) {
      return new SelfDescribingObject(Schema.IP, ((BgpActivePeerConfig) peer).getPeerAddress());
    }
    if (peer instanceof BgpPassivePeerConfig) {
      return new SelfDescribingObject(Schema.PREFIX, ((BgpPassivePeerConfig) peer).getPeerPrefix());
    }
    throw new IllegalArgumentException(
        String.format("Peer is neither Active nor Passive: %s", peer));
  }

  /** Returns the name of the column that contains the value of property {@code property} */
  public static String getColumnName(String property) {
    return property;
  }
}
