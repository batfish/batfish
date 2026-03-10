package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.PsFromPrefixListFilterOrLonger.name;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.junit.Test;

public class PsFromPrefixListFilterOrLongerTest {

  @Test
  public void toBooleanExpr() {
    // Set up
    PrefixList pl = new PrefixList("pl");
    pl.getPrefixes().add(Prefix.parse("1.2.3.4/32"));
    pl.getPrefixes().add(Prefix.parse("2.0.0.0/8"));
    JuniperConfiguration jc = new JuniperConfiguration();
    jc.getMasterLogicalSystem().getPrefixLists().put(pl.getName(), pl);
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("c")
            .build();
    Warnings w = new Warnings();

    // Conversion
    PsFromPrefixListFilterOrLonger expr = new PsFromPrefixListFilterOrLonger(pl.getName());
    BooleanExpr e = expr.toBooleanExpr(jc, c, w);
    assertThat(
        e,
        equalTo(
            new MatchPrefixSet(
                DestinationNetwork.instance(), new NamedPrefixSet(name(pl.getName())))));
    RouteFilterList converted = c.getRouteFilterLists().get(name(pl.getName()));
    assertThat(converted, notNullValue());
    assertThat(converted.permits(Prefix.parse("1.2.3.4/32")), equalTo(true));
    assertThat(converted.permits(Prefix.parse("1.2.3.4/31")), equalTo(false));
    assertThat(converted.permits(Prefix.parse("2.0.0.0/7")), equalTo(false));
    assertThat(converted.permits(Prefix.parse("2.0.0.0/8")), equalTo(true));
    assertThat(converted.permits(Prefix.parse("2.0.0.0/9")), equalTo(true));
  }
}
