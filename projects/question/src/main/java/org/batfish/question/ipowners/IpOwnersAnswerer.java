package org.batfish.question.ipowners;

import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

class IpOwnersAnswerer extends Answerer {
  static final String COL_ACTIVE = "Active";
  static final String COL_INTERFACE_NAME = "Interface";
  static final String COL_IP = "IP";
  static final String COL_MASK = "Mask";
  static final String COL_NODE = "Node";
  static final String COL_VRFNAME = "VRF";

  IpOwnersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    IpOwnersQuestion question = (IpOwnersQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);
    Map<Ip, Set<String>> ipNodeOwners = IpOwners.computeIpNodeOwners(configurations, false);
    Map<String, Set<Interface>> interfaces = computeNodeInterfaces(configurations);

    TableAnswerElement answerElement = new TableAnswerElement(getTableMetadata());

    answerElement.postProcessAnswer(
        _question, generateRows(ipNodeOwners, interfaces, question.getDuplicatesOnly()));
    return answerElement;
  }

  @VisibleForTesting
  static Multiset<Row> generateRows(
      Map<Ip, Set<String>> ipNodeOwners,
      Map<String, Set<Interface>> interfaces,
      boolean duplicatesOnly) {
    Multiset<Row> rows = HashMultiset.create();

    interfaces.forEach(
        (hostname, interfaceSet) ->
            interfaceSet.forEach(
                iface -> {
                  iface
                      .getAllConcreteAddresses()
                      .forEach(
                          address -> {
                            if (ipNodeOwners.getOrDefault(address.getIp(), ImmutableSet.of()).size()
                                    > 1
                                || !duplicatesOnly) {
                              rows.add(
                                  Row.builder()
                                      .put(COL_NODE, new Node(hostname))
                                      .put(COL_VRFNAME, iface.getVrfName())
                                      .put(COL_INTERFACE_NAME, iface.getName())
                                      .put(COL_IP, address.getIp())
                                      .put(COL_MASK, address.getNetworkBits())
                                      .put(COL_ACTIVE, iface.getActive())
                                      .build());
                            }
                          });
                }));
    return rows;
  }

  /** Create table metadata for this answer. */
  private static TableMetadata getTableMetadata() {
    List<ColumnMetadata> columnMetadata = getColumnMetadata();
    return new TableMetadata(
        columnMetadata,
        String.format(
            "On node ${%s} in VRF ${%s}, interface ${%s} has IP ${%s}.",
            COL_NODE, COL_VRFNAME, COL_INTERFACE_NAME, COL_IP));
  }

  /** Create column metadata. */
  @VisibleForTesting
  static List<ColumnMetadata> getColumnMetadata() {
    return ImmutableList.of(
        new ColumnMetadata(COL_NODE, Schema.NODE, "Node hostname"),
        new ColumnMetadata(COL_VRFNAME, Schema.STRING, "VRF name"),
        new ColumnMetadata(COL_INTERFACE_NAME, Schema.STRING, "Interface name"),
        new ColumnMetadata(COL_IP, Schema.IP, "IP address"),
        new ColumnMetadata(COL_MASK, Schema.INTEGER, "Network mask length"),
        new ColumnMetadata(COL_ACTIVE, Schema.BOOLEAN, "Whether the interface is active"));
  }
}
