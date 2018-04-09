package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.matchers.AclLineMatchExprMatchers;
import org.junit.Test;

public class MatchSrcInterfaceTest {
  private static Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSrcInterface() {
    // Test source interface matching

    Set<String> interfaceNames = new HashSet<>();
    interfaceNames.add("test");
    MatchSrcInterface exprSrcInterface = new MatchSrcInterface(interfaceNames);

    // Confirm the same interface name is matched
    assertThat(
        exprSrcInterface,
        AclLineMatchExprMatchers.matches(createFlow(), "test", ImmutableMap.of()));

    // Confirm a different interface name is not matched
    assertThat(
        exprSrcInterface,
        not(AclLineMatchExprMatchers.matches(createFlow(), "fail", ImmutableMap.of())));
  }
}
