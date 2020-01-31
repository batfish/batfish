package org.batfish.datamodel.acl;

import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.acl.IpAccessListRenamer.Visitor;
import org.batfish.datamodel.visitors.IpSpaceRenamer;
import org.junit.Test;

/** Tests of {@link IpAccessListRenamer}. */
public class IpAccessListRenamerTest {
  private static final IpAccessListRenamer RENAMER =
      new IpAccessListRenamer(
          ImmutableMap.of("a", "b")::get, new IpSpaceRenamer(ImmutableMap.of("c", "d")::get));

  private static final Visitor VISITOR = RENAMER.getAclLineMatchExprVisitor();

  private static final PermittedByAcl ACL_A = new PermittedByAcl("a");
  private static final PermittedByAcl ACL_B = new PermittedByAcl("b");

  private static final IpSpaceReference IP_SPACE_C = new IpSpaceReference("c");
  private static final IpSpaceReference IP_SPACE_D = new IpSpaceReference("d");

  private static final HeaderSpace HEADER_SPACE_ORIG =
      HeaderSpace.builder()
          // set some non-IpSpace field to make sure they're preserved
          .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
          .setDstIps(IP_SPACE_C)
          .setNotDstIps(IP_SPACE_C)
          .setSrcIps(IP_SPACE_C)
          .setNotSrcIps(IP_SPACE_C)
          .setSrcOrDstIps(IP_SPACE_C)
          .build();

  private static final MatchHeaderSpace MATCH_HEADER_SPACE_ORIG =
      new MatchHeaderSpace(HEADER_SPACE_ORIG);

  private static final HeaderSpace HEADER_SPACE_RENAMED =
      HeaderSpace.builder()
          .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
          .setDstIps(IP_SPACE_D)
          .setNotDstIps(IP_SPACE_D)
          .setSrcIps(IP_SPACE_D)
          .setNotSrcIps(IP_SPACE_D)
          .setSrcOrDstIps(IP_SPACE_D)
          .build();
  private static final MatchHeaderSpace MATCH_HEADER_SPACE_RENAMED =
      new MatchHeaderSpace(HEADER_SPACE_RENAMED);

  @Test
  public void testIdentities() {
    assertThat(VISITOR.visit(TRUE), equalTo(TRUE));
    assertThat(VISITOR.visit(FALSE), equalTo(FALSE));
    assertThat(VISITOR.visit(ORIGINATING_FROM_DEVICE), equalTo(ORIGINATING_FROM_DEVICE));
    assertThat(VISITOR.visit(matchSrcInterface("iface")), equalTo(matchSrcInterface("iface")));
  }

  @Test
  public void testAnd() {
    assertThat(
        VISITOR.visit(and(TRUE, ACL_A, MATCH_HEADER_SPACE_ORIG)),
        equalTo(and(TRUE, ACL_B, MATCH_HEADER_SPACE_RENAMED)));
  }

  @Test
  public void testOr() {
    assertThat(
        VISITOR.visit(or(TRUE, ACL_A, MATCH_HEADER_SPACE_ORIG)),
        equalTo(or(TRUE, ACL_B, MATCH_HEADER_SPACE_RENAMED)));
  }

  @Test
  public void testNot() {
    assertThat(VISITOR.visit(not(TRUE)), equalTo(not(TRUE)));
    assertThat(VISITOR.visit(not(ACL_A)), equalTo(not(ACL_B)));
    assertThat(
        VISITOR.visit(not(MATCH_HEADER_SPACE_ORIG)), equalTo(not(MATCH_HEADER_SPACE_RENAMED)));
  }

  @Test
  public void testHeaderSpace() {
    assertThat(VISITOR.visit(MATCH_HEADER_SPACE_ORIG), equalTo(MATCH_HEADER_SPACE_RENAMED));
  }

  @Test
  public void testDeniedByAcl() {
    assertThat(VISITOR.visit(new DeniedByAcl("a")), equalTo(new DeniedByAcl("b")));
  }

  @Test
  public void testApply() {
    assertThat(
        RENAMER.apply(
            IpAccessList.builder()
                .setName("a")
                .setLines(
                    ImmutableList.of(
                        accepting(TRUE), rejecting(ACL_A), accepting(MATCH_HEADER_SPACE_ORIG)))
                .build()),
        equalTo(
            IpAccessList.builder()
                .setName("b")
                .setLines(
                    ImmutableList.of(
                        accepting(TRUE), rejecting(ACL_B), accepting(MATCH_HEADER_SPACE_RENAMED)))
                .build()));
  }
}
