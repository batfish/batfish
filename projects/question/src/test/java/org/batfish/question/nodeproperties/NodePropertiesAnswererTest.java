package org.batfish.question.nodeproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class NodePropertiesAnswererTest {

  @Test
  public void rawAnswer() {
    String property1 = "configuration-format";
    String property2 = "ntp-servers";
    NodePropertiesQuestion question =
        new NodePropertiesQuestion(null, new NodePropertySpecifier(property1 + "|" + property2));

    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration conf2 = new Configuration("node2", ConfigurationFormat.HOST);
    conf2.setNtpServers(ImmutableSortedSet.of("sa"));
    Map<String, Configuration> configurations = ImmutableMap.of("node1", conf1, "node2", conf2);

    Set<String> nodes = ImmutableSet.of("node1", "node2");

    Multiset<Row> propertyRows = NodePropertiesAnswerer.rawAnswer(question, configurations, nodes);

    // we should have exactly these two rows
    String colName1 = property1;
    String colName2 = property2;
    Row row1 =
        Row.builder()
            .put(NodePropertiesAnswerer.COL_NODE, new Node("node1"))
            .put(colName1, ConfigurationFormat.CISCO_IOS)
            .put(colName2, ImmutableList.of())
            .build();
    Row row2 =
        Row.builder()
            .put(NodePropertiesAnswerer.COL_NODE, new Node("node2"))
            .put(colName1, ConfigurationFormat.HOST)
            .put(colName2, ImmutableList.of("sa"))
            .build();

    assertThat(propertyRows.size(), equalTo(2));
    assertThat(propertyRows, hasItems(row1, row2));
  }
}
