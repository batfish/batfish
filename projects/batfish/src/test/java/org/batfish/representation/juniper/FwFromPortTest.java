package org.batfish.representation.juniper;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromPort} */
public class FwFromPortTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromPort from = new FwFromPort(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setSrcOrDstPorts(ImmutableList.of(new SubRange(1, 2))).build(),
            TraceElement.of("Matched port 1-2")));
  }
}
