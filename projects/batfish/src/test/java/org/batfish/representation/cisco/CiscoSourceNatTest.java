package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

/** Tests for {@link CiscoSourceNat} */
public class CiscoSourceNatTest {
  @Test
  public void testToTransformation() {
    CiscoSourceNat nat = new CiscoSourceNat();

    assertThat(
        nat.toTransformation(ImmutableMap.of(), ImmutableMap.of()), equalTo(Optional.empty()));

    nat.setAclName("acl");
    nat.setNatPool("pool");

    assertThat(
        nat.toTransformation(ImmutableMap.of(), ImmutableMap.of()), equalTo(Optional.empty()));

    NatPool pool = new NatPool();
    Ip first = Ip.parse("1.1.1.1");
    Ip last = Ip.parse("1.1.1.2");
    pool.setFirst(first);
    pool.setLast(last);
    assertThat(
        nat.toTransformation(
            ImmutableMap.of("acl", IpAccessList.builder().setName("acl").build()),
            ImmutableMap.of("pool", pool)),
        equalTo(
            Optional.of(when(permittedByAcl("acl")).apply(assignSourceIp(first, last)).build())));
  }
}
