package org.batfish.datamodel.acl;

import static org.batfish.datamodel.matchers.AclLineMatchExprMatchers.matches;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
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
    assertThat(exprSrcInterface, matches(createFlow(), "test"));

    // Confirm a different interface name is not matched
    assertThat(exprSrcInterface, not(matches(createFlow(), "fail")));
  }
}
