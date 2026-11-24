package org.batfish.question.filterlinereachability;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.CircularReferenceException;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchDestinationPort;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchIpProtocol;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSourcePort;
import org.batfish.datamodel.acl.UndefinedReferenceException;
import org.junit.jupiter.api.Test;

/** Tests of {@link HeaderSpaceSanitizer} */
class HeaderSpaceSanitizerTest {

  private static final String IP_SPACE_NAME = "ipSpace";
  private static final Ip REFERENCED_IP = Ip.parse("1.1.1.1");
  private static final IpSpace REFERENCED_SPACE = REFERENCED_IP.toIpSpace();
  private static final String CIRCULAR_REF_NAME = "cycleSpace";
  private static final Map<String, IpSpace> NAMED_IP_SPACES =
      ImmutableMap.of(
          IP_SPACE_NAME,
          REFERENCED_SPACE,
          CIRCULAR_REF_NAME,
          new IpSpaceReference("cycle2"),
          "cycle2",
          new IpSpaceReference(CIRCULAR_REF_NAME));

  private static final HeaderSpaceSanitizer SANITIZER = new HeaderSpaceSanitizer(NAMED_IP_SPACES);

  @Test
  void testDereferencesNamedIpSpaceInLine() {
    ExprAclLine.Builder lineBuilder = ExprAclLine.builder().setAction(LineAction.PERMIT);
    MatchHeaderSpace matchHeaderSpace =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstIps(new IpSpaceReference(IP_SPACE_NAME)).build());
    assertThat(
        SANITIZER.visit(lineBuilder.setMatchCondition(matchHeaderSpace).build()),
        equalTo(
            lineBuilder
                .setMatchCondition(
                    new MatchHeaderSpace(HeaderSpace.builder().setDstIps(REFERENCED_SPACE).build()))
                .build()));
  }

  @Test
  void testDereferencesNamedIpSpace() {
    MatchHeaderSpace matchHeaderSpace =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstIps(new IpSpaceReference(IP_SPACE_NAME)).build());
    assertThat(
        SANITIZER.visit(matchHeaderSpace),
        equalTo(new MatchHeaderSpace(HeaderSpace.builder().setDstIps(REFERENCED_SPACE).build())));
  }

  @Test
  void testDoesNotAffectIpIpSpace() {
    MatchHeaderSpace matchHeaderSpace =
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(REFERENCED_SPACE).build());
    assertThat(SANITIZER.visit(matchHeaderSpace), equalTo(matchHeaderSpace));
  }

  @Test
  void testUndefinedIpSpaceReference() {
    MatchHeaderSpace matchHeaderSpace =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstIps(new IpSpaceReference("unknown name")).build());
    assertThrows(UndefinedReferenceException.class, () -> SANITIZER.visit(matchHeaderSpace));
  }

  @Test
  void testDestinationIpError() {
    MatchDestinationIp matchDestinationIp =
        (MatchDestinationIp) matchDst(new IpSpaceReference("unknown name"));
    assertThrows(UndefinedReferenceException.class, () -> SANITIZER.visit(matchDestinationIp));
  }

  @Test
  void testDestinationIpInterning() {
    // Unchanged is interned
    MatchDestinationIp matchDestinationIp = (MatchDestinationIp) matchDst(REFERENCED_IP);
    assertThat(SANITIZER.visit(matchDestinationIp), sameInstance(matchDestinationIp));
    MatchDestinationIp referencedIp =
        (MatchDestinationIp) matchDst(new IpSpaceReference(IP_SPACE_NAME));
    assertThat(SANITIZER.visit(referencedIp), equalTo(matchDestinationIp));
  }

  @Test
  void testSourceIpInterning() {
    // Unchanged is interned
    MatchSourceIp matchSourceIp = (MatchSourceIp) matchSrc(REFERENCED_IP);
    assertThat(SANITIZER.visit(matchSourceIp), sameInstance(matchSourceIp));
    MatchSourceIp referencedIp = (MatchSourceIp) matchSrc(new IpSpaceReference(IP_SPACE_NAME));
    assertThat(SANITIZER.visit(referencedIp), equalTo(matchSourceIp));
  }

  @Test
  void testDestinationPort() {
    MatchDestinationPort match = (MatchDestinationPort) matchDstPort(1);
    assertThat(SANITIZER.visit(match), sameInstance(match));
  }

  @Test
  void testSourcePort() {
    MatchSourcePort match = (MatchSourcePort) matchSrcPort(1);
    assertThat(SANITIZER.visit(match), sameInstance(match));
  }

  @Test
  void testIpProtocol() {
    MatchIpProtocol match = (MatchIpProtocol) matchIpProtocol(IpProtocol.TCP);
    assertThat(SANITIZER.visit(match), sameInstance(match));
  }

  @Test
  void testCyclicIpSpaceReference() {
    MatchHeaderSpace matchHeaderSpace =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstIps(new IpSpaceReference(CIRCULAR_REF_NAME)).build());
    assertThrows(CircularReferenceException.class, () -> SANITIZER.visit(matchHeaderSpace));
  }
}
