package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.junit.Test;

/** Test for {@link ApplicationSet} */
public class ApplicationSetTest {

  @Test
  public void testToAclLineMatchExpr() {
    /*
    masterAppSet
    - app1
    - appSet
      - app2
     */

    JuniperConfiguration jc = new JuniperConfiguration();
    jc.setFilename("host");
    jc.getMasterLogicalSystem().getApplications().put("app2", new BaseApplication("app2", false));

    ApplicationSet appSet = new ApplicationSet("appSet", false);
    appSet.setMembers(ImmutableList.of(new ApplicationReference("app2")));
    jc.getMasterLogicalSystem().getApplicationSets().put("appSet", appSet);

    jc.getMasterLogicalSystem().getApplications().put("app1", new BaseApplication("app1", false));

    ApplicationSet masterAppSet = new ApplicationSet("masterAppSet", false);
    List<ApplicationSetMemberReference> members =
        ImmutableList.of(new ApplicationReference("app1"), new ApplicationSetReference("appSet"));
    masterAppSet.setMembers(members);

    assertEquals(
        masterAppSet.toAclLineMatchExpr(jc, null),
        new OrMatchExpr(
            ImmutableList.of(
                new MatchHeaderSpace(
                    HeaderSpace.builder().build(),
                    ApplicationSetMember.getTraceElementForUserApplication(
                        "host", JuniperStructureType.APPLICATION, "app1")),
                new OrMatchExpr(
                    ImmutableList.of(
                        new MatchHeaderSpace(
                            HeaderSpace.builder().build(),
                            ApplicationSetMember.getTraceElementForUserApplication(
                                "host", JuniperStructureType.APPLICATION, "app2"))),
                    ApplicationSetMember.getTraceElementForUserApplication(
                        "host", JuniperStructureType.APPLICATION_SET, "appSet"))),
            ApplicationSetMember.getTraceElementForUserApplication(
                "host", JuniperStructureType.APPLICATION_SET, "masterAppSet")));
  }

  @Test
  public void testToAclLineMatchExpr_builtIn() {
    JuniperConfiguration jc = new JuniperConfiguration();
    jc.setFilename("host");
    String parentAppSetName = "PARENT_BUILT_IN";
    ApplicationSet appSet = new ApplicationSet(parentAppSetName, true);
    jc.getMasterLogicalSystem().getApplicationSets().put(parentAppSetName, appSet);

    assertEquals(
        appSet.toAclLineMatchExpr(jc, null),
        new FalseExpr(JunosApplicationSet.getTraceElement(parentAppSetName)));
  }
}
