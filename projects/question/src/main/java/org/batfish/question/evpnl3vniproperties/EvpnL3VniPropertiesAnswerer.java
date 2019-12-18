package org.batfish.question.evpnl3vniproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.Layer3VniConfig;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Implements {@link EvpnL3VniPropertiesQuestion}. */
public final class EvpnL3VniPropertiesAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_VRF = "VRF";
  public static final String COL_VNI = "VNI";
  public static final String COL_ROUTE_DISTINGUISHER = "Route_Distinguisher";
  public static final String COL_IMPORT_ROUTE_TARGET = "Import_Route_Target";
  public static final String COL_EXPORT_ROUTE_TARGET = "Export_Route_Target";

  public static final List<ColumnMetadata> COLUMN_METADATA =
      ImmutableList.<ColumnMetadata>builder()
          .add(new ColumnMetadata(COL_NODE, Schema.STRING, "Node", true, false))
          .add(new ColumnMetadata(COL_VRF, Schema.STRING, "VRF", true, false))
          .add(new ColumnMetadata(COL_VNI, Schema.INTEGER, "VXLAN Segment ID", true, false))
          .add(
              new ColumnMetadata(
                  COL_ROUTE_DISTINGUISHER, Schema.STRING, "Route distinguisher", false, true))
          .add(
              new ColumnMetadata(
                  COL_IMPORT_ROUTE_TARGET, Schema.STRING, "Import route target", false, true))
          .add(
              new ColumnMetadata(
                  COL_EXPORT_ROUTE_TARGET, Schema.STRING, "Export route target", false, true))
          .build();

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  @VisibleForTesting
  static TableMetadata createTableMetadata(EvpnL3VniPropertiesQuestion question) {
    String textDesc =
        String.format("Properties of VXLAN VNI ${%s} on node ${%s}.", COL_VNI, COL_NODE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(COLUMN_METADATA, textDesc);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    EvpnL3VniPropertiesQuestion question = (EvpnL3VniPropertiesQuestion) _question;
    Set<String> nodes =
        SpecifierFactories.getNodeSpecifierOrDefault(
                question.getNodes(), AllNodesNodeSpecifier.INSTANCE)
            .resolve(_batfish.specifierContext(snapshot));

    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Set<Row> propertyRows =
        getRows(
            nodes,
            NetworkConfigurations.of(_batfish.loadConfigurations(_batfish.getSnapshot())),
            tableMetadata.toColumnMap());

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  /** Generate a row for a {@link Layer3VniConfig} */
  @VisibleForTesting
  @Nonnull
  static Row generateRow(
      Layer3VniConfig vniConfig, String nodeName, Map<String, ColumnMetadata> columns) {
    return Row.builder(columns)
        .put(COL_NODE, nodeName)
        .put(COL_VRF, vniConfig.getVrf())
        .put(COL_VNI, vniConfig.getVni())
        .put(COL_EXPORT_ROUTE_TARGET, vniConfig.getRouteTarget().matchString())
        .put(COL_IMPORT_ROUTE_TARGET, vniConfig.getImportRouteTarget())
        .put(COL_ROUTE_DISTINGUISHER, vniConfig.getRouteDistinguisher())
        .build();
  }

  /**
   * Gets properties of EVPN Layer 3 VNIs.
   *
   * @param nodes the set of nodes to consider
   * @param nc all network configurations
   * @param columns a map from column name to {@link ColumnMetadata}
   * @return A multiset of {@link Row}s where each row corresponds to a node and columns correspond
   *     to property values.
   */
  @Nonnull
  public static Set<Row> getRows(
      Set<String> nodes, NetworkConfigurations nc, Map<String, ColumnMetadata> columns) {
    Builder<Row> rows = ImmutableSet.builder();

    for (String nodeName : nodes) {
      nc.get(nodeName).map(Configuration::getVrfs).orElse(ImmutableMap.of()).values().stream()
          .map(Vrf::getBgpProcess)
          .filter(Objects::nonNull)
          .flatMap(BgpProcess::allPeerConfigsStream)
          .map(BgpPeerConfig::getEvpnAddressFamily)
          .filter(Objects::nonNull)
          .flatMap(af -> af.getL3VNIs().stream())
          .distinct() // Get rid of duplication across BGP peers (due to the VI model hierarchy)
          .map(c -> generateRow(c, nodeName, columns))
          .forEach(rows::add);
    }
    return rows.build();
  }

  public EvpnL3VniPropertiesAnswerer(EvpnL3VniPropertiesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }
}
