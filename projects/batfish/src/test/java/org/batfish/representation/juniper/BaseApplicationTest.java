package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.ApplicationSetMember.getTraceElement;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.representation.juniper.BaseApplication.Term;
import org.junit.Test;

/** Test for {@link BaseApplication} */
public class BaseApplicationTest {
  private static JuniperConfiguration jc = new JuniperConfiguration();

  static {
    jc.setFilename("host");
  }

  @Test
  public void testTermToAclLineMatchExpr() {
    Term term = new Term("T");
    term.setHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.TCP)
            .setSrcIps(IpWildcard.parse("1.1.1.0/24").toIpSpace())
            .build());

    assertEquals(
        term.toAclLineMatchExpr(),
        new MatchHeaderSpace(
            HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build(),
            TraceElement.of("Matched term T")));
  }

  @Test
  public void testTermToAclLineMatchExpr_noTraceElement() {
    Term term = new Term();
    term.setHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.TCP)
            .setSrcIps(IpWildcard.parse("1.1.1.0/24").toIpSpace())
            .build());

    assertEquals(
        term.toAclLineMatchExpr(),
        new MatchHeaderSpace(HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build()));
  }

  @Test
  public void testToAclLineMatchExpr_noTerms() {
    BaseApplication app = new BaseApplication("APP");
    assertEquals(
        app.toAclLineMatchExpr(jc, null),
        new MatchHeaderSpace(
            app.getMainTerm().toHeaderSpace(),
            getTraceElement("host", JuniperStructureType.APPLICATION, "APP")));
  }

  @Test
  public void testToAclLineMatchExpr_hasTerms() {
    BaseApplication app = new BaseApplication("APP");
    Term term1 = new Term("TERM1");
    Term term2 = new Term("TERM2");
    app.getTerms().put("TERM1", term1);
    app.getTerms().put("TERM2", term2);

    assertEquals(
        app.toAclLineMatchExpr(jc, null),
        new OrMatchExpr(
            ImmutableList.of(term1.toAclLineMatchExpr(), term2.toAclLineMatchExpr()),
            getTraceElement(jc.getFilename(), JuniperStructureType.APPLICATION, "APP")));
  }
}
