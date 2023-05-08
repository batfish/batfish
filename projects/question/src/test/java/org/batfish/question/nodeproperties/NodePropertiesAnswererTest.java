package org.batfish.question.nodeproperties;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.NameRegexNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.junit.Test;

/** Tests for {@link NodePropertiesAnswerer} */
public class NodePropertiesAnswererTest {

  @Test
  public void getProperties() {
    String property1 = NodePropertySpecifier.CONFIGURATION_FORMAT;
    String property2 = NodePropertySpecifier.NTP_SERVERS;
    NodePropertySpecifier propertySpec =
        new NodePropertySpecifier(ImmutableSet.of(property1, property2));

    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration conf2 = new Configuration("node2", ConfigurationFormat.HOST);
    conf2.setNtpServers(ImmutableSortedSet.of("sa"));
    Map<String, Configuration> configurations = ImmutableMap.of("node1", conf1, "node2", conf2);
    Map<String, ColumnMetadata> columns =
        NodePropertiesAnswerer.createTableMetadata(new NodePropertiesQuestion(null, propertySpec))
            .toColumnMap();

    NodeSpecifier nodeSpecifier = new NameRegexNodeSpecifier(Pattern.compile("node1|node2"));
    MockSpecifierContext ctxt = MockSpecifierContext.builder().setConfigs(configurations).build();

    List<Row> propertyRows =
        NodePropertiesAnswerer.getProperties(propertySpec, ctxt, nodeSpecifier, columns);

    // we should have exactly these two rows
    assertThat(
        propertyRows,
        contains(
            Row.builder()
                .put(NodePropertiesAnswerer.COL_NODE, new Node("node1"))
                .put(property1, ConfigurationFormat.CISCO_IOS)
                .put(property2, ImmutableList.of())
                .build(),
            Row.builder()
                .put(NodePropertiesAnswerer.COL_NODE, new Node("node2"))
                .put(property1, ConfigurationFormat.HOST)
                .put(property2, ImmutableList.of("sa"))
                .build()));
  }
}
