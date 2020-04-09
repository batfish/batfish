package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.ApplicationSetMember.getTraceElement;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.representation.juniper.BaseApplication.Term;
import org.junit.Test;

/** Test for {@link FwFromApplicationOrApplicationSet} */
public class FwFromApplicationOrApplicationSetTest {
  private static JuniperConfiguration jc = new JuniperConfiguration();

  static {
    jc.setFilename("host");
  }

  @Test
  public void testApplyTo_application() {
    BaseApplication app = new BaseApplication("app");
    app.getTerms()
        .putAll(
            ImmutableMap.of(
                "t1", new Term("t1"),
                "t2", new Term("t2")));

    jc.getMasterLogicalSystem().getApplications().put("app", app);

    FwFromApplicationOrApplicationSet from = new FwFromApplicationOrApplicationSet("app");

    List<ExprAclLine> lines = new ArrayList<>();
    HeaderSpace.Builder hsBuilder = HeaderSpace.builder();

    from.applyTo(jc, hsBuilder, LineAction.PERMIT, lines, null);

    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(HeaderSpace.builder().build()),
                    null,
                    app.getTermTraceElement("t1")),
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(HeaderSpace.builder().build()),
                    null,
                    app.getTermTraceElement("t2")))));
  }

  @Test
  public void testApplyTo_applicationSet() {
    ApplicationSet appSet = new ApplicationSet("appSet");
    appSet.setMembers(
        ImmutableList.of(new ApplicationReference("app1"), new ApplicationReference("app2")));
    jc.getMasterLogicalSystem().getApplicationSets().put("appSet", appSet);
    BaseApplication app1 = new BaseApplication("app1");
    BaseApplication app2 = new BaseApplication("app2");
    jc.getMasterLogicalSystem()
        .getApplications()
        .putAll(ImmutableMap.of("app1", app1, "app2", app2));

    FwFromApplicationOrApplicationSet from = new FwFromApplicationOrApplicationSet("appSet");

    List<ExprAclLine> lines = new ArrayList<>();
    HeaderSpace.Builder hsBuilder = HeaderSpace.builder();

    from.applyTo(jc, hsBuilder, LineAction.PERMIT, lines, null);

    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(HeaderSpace.builder().build()),
                    null,
                    app1.getTermTraceElement(null)),
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(HeaderSpace.builder().build()),
                    null,
                    app2.getTermTraceElement(null)))));
  }

  @Test
  public void testToAclLineMatchExpr_application() {
    BaseApplication app = new BaseApplication("app");
    app.getTerms()
        .putAll(
            ImmutableMap.of(
                "t1", new Term("t1"),
                "t2", new Term("t2")));

    jc.getMasterLogicalSystem().getApplications().put("app", app);

    FwFromApplicationOrApplicationSet from = new FwFromApplicationOrApplicationSet("app");

    assertEquals(
        from.toAclLineMatchExpr(jc, null),
        new OrMatchExpr(
            ImmutableList.of(
                new MatchHeaderSpace(
                    HeaderSpace.builder().build(), TraceElement.of("Matched term t1")),
                new MatchHeaderSpace(
                    HeaderSpace.builder().build(), TraceElement.of("Matched term t2"))),
            getTraceElement("host", JuniperStructureType.APPLICATION, "app")));
  }

  @Test
  public void testToAclLineMatchExpr_applicationSet() {

    ApplicationSet appSet = new ApplicationSet("appSet");
    appSet.setMembers(
        ImmutableList.of(new ApplicationReference("app1"), new ApplicationReference("app2")));
    jc.getMasterLogicalSystem().getApplicationSets().put("appSet", appSet);
    BaseApplication app1 = new BaseApplication("app1");
    BaseApplication app2 = new BaseApplication("app2");
    jc.getMasterLogicalSystem()
        .getApplications()
        .putAll(ImmutableMap.of("app1", app1, "app2", app2));

    FwFromApplicationOrApplicationSet from = new FwFromApplicationOrApplicationSet("appSet");

    assertEquals(
        from.toAclLineMatchExpr(jc, null),
        new OrMatchExpr(
            ImmutableList.of(
                new MatchHeaderSpace(
                    HeaderSpace.builder().build(),
                    getTraceElement("host", JuniperStructureType.APPLICATION, "app1")),
                new MatchHeaderSpace(
                    HeaderSpace.builder().build(),
                    getTraceElement("host", JuniperStructureType.APPLICATION, "app2"))),
            getTraceElement("host", JuniperStructureType.APPLICATION_SET, "appSet")));
  }
}
