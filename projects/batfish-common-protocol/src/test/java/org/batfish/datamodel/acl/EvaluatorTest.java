package org.batfish.datamodel.acl;

import static org.batfish.datamodel.ExprAclLine.ACCEPT_ALL;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

/** Test for {@link Evaluator}. */
public final class EvaluatorTest {
  private static final Flow FLOW = Flow.builder().setIngressNode("node").build();

  @Test
  public void testDeniedByAcl_permit() {
    IpAccessList acl =
        IpAccessList.builder().setName("acl").setLines(ImmutableList.of(ACCEPT_ALL)).build();
    assertFalse(
        Evaluator.matches(
            new DeniedByAcl(acl.getName()),
            FLOW,
            null,
            ImmutableMap.of(acl.getName(), acl),
            ImmutableMap.of()));
  }

  @Test
  public void testDeniedByAcl_explicitDeny() {
    IpAccessList acl =
        IpAccessList.builder().setName("acl").setLines(ImmutableList.of(REJECT_ALL)).build();
    assertTrue(
        Evaluator.matches(
            new DeniedByAcl(acl.getName()),
            FLOW,
            null,
            ImmutableMap.of(acl.getName(), acl),
            ImmutableMap.of()));
  }

  @Test
  public void testDeniedByAcl_noMatch() {
    IpAccessList acl = IpAccessList.builder().setName("acl").build();
    assertTrue(
        Evaluator.matches(
            new DeniedByAcl(acl.getName()),
            FLOW,
            null,
            ImmutableMap.of(acl.getName(), acl),
            ImmutableMap.of()));
  }
}
