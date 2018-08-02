package org.batfish.question.bgpproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpPropertySpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableMetadata;
import org.junit.Test;

public class BgpPropertiesAnswererTest {

  @Test
  public void getProperties() {

    BgpProcess bgp1 = new BgpProcess();
    bgp1.setRouterId(new Ip("1.1.1.1"));
    bgp1.setMultipathEbgp(true);
    bgp1.setTieBreaker(BgpTieBreaker.ARRIVAL_ORDER);

    Vrf vrf1 = new Vrf("vrf1");
    vrf1.setBgpProcess(bgp1);

    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    conf1.setVrfs(ImmutableMap.of("vrf1", vrf1));

    String property1 = "multipath-ebgp";
    String property2 = "tie-breaker";

    BgpPropertiesQuestion question =
        new BgpPropertiesQuestion(null, new BgpPropertySpecifier(property1 + "|" + property2));

    TableMetadata metadata = BgpPropertiesAnswerer.createTableMetadata(question);

    Multiset<Row> propertyRows =
        BgpPropertiesAnswerer.getProperties(
            question.getPropertySpec(),
            ImmutableMap.of("node1", conf1),
            ImmutableSet.of("node1"),
            metadata.toColumnMap());

    // we should have exactly one row1 with two properties
    Row expectedRow =
        Row.builder()
            .put(BgpPropertiesAnswerer.COL_NODE, new Node("node1"))
            .put(BgpPropertiesAnswerer.COL_VRF, "vrf1")
            .put(BgpPropertiesAnswerer.COL_ROUTER_ID, new Ip("1.1.1.1"))
            .put(property2, BgpTieBreaker.ARRIVAL_ORDER.toString())
            .put(property1, true)
            .build();

    assertThat(propertyRows, equalTo(ImmutableMultiset.of(expectedRow)));
  }
}
