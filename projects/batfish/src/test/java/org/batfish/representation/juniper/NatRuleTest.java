package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.Transformation.Builder;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

/** Tests of {@link NatRule}. */
public class NatRuleTest {
  @Test
  public void testToLocation() {
    NatRule rule = new NatRule("RS");
    Prefix pfx = Prefix.parse("1.1.1.1/32");
    HeaderSpace hs = HeaderSpace.builder().setDstIps(pfx.toIpSpace()).build();
    rule.getMatches().add(new NatRuleMatchDstAddr(pfx));
    rule.setThen(NatRuleThenOff.INSTANCE);

    assertThat(
        rule.toTransformationBuilder(DESTINATION, ImmutableMap.of()).map(Builder::build),
        equalTo(Optional.of(when(match(hs)).apply().build())));

    rule.setThen(new NatRuleThenPool("pool"));

    // pool is undefined
    assertThat(
        rule.toTransformationBuilder(DESTINATION, ImmutableMap.of()).map(Builder::build),
        equalTo(Optional.empty()));

    // pool is defined
    NatPool pool = new NatPool();
    Ip startIp = Ip.parse("5.5.5.5");
    Ip endIp = Ip.parse("6.6.6.6");
    pool.setFromAddress(startIp);
    pool.setToAddress(endIp);

    // destination NAT
    assertThat(
        rule.toTransformationBuilder(DESTINATION, ImmutableMap.of("pool", pool))
            .map(Builder::build),
        equalTo(
            Optional.of(
                when(match(hs))
                    .apply(TransformationStep.assignDestinationIp(startIp, endIp))
                    .build())));

    // source NAT
    assertThat(
        rule.toTransformationBuilder(SOURCE, ImmutableMap.of("pool", pool)).map(Builder::build),
        equalTo(
            Optional.of(
                when(match(hs)).apply(TransformationStep.assignSourceIp(startIp, endIp)).build())));
  }
}
