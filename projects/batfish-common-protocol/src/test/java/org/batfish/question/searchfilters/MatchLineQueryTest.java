package org.batfish.question.searchfilters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

/** Tests of {@link MatchLineQuery} */
public class MatchLineQueryTest {
  @Test
  public void testCanQuery() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL))
            .build();

    // Match line queries should work only if the line number is within range for the ACL
    MatchLineQuery matchLine0 = new MatchLineQuery(0);
    MatchLineQuery matchLine1 = new MatchLineQuery(1);
    assertTrue(matchLine0.canQuery(acl));
    assertFalse(matchLine1.canQuery(acl));
  }
}
