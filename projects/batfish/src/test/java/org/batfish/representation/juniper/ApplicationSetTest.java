package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
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
    jc.getMasterLogicalSystem().getApplications().put("app2", new BaseApplication("app2"));

    ApplicationSet appSet = new ApplicationSet("appSet");
    appSet.setMembers(ImmutableList.of(new ApplicationReference("app2")));
    jc.getMasterLogicalSystem().getApplicationSets().put("appSet", appSet);

    jc.getMasterLogicalSystem().getApplications().put("app1", new BaseApplication("app1"));

    ApplicationSet masterAppSet = new ApplicationSet("masterAppSet");
    List<ApplicationSetMemberReference> members =
        ImmutableList.of(new ApplicationReference("app1"), new ApplicationSetReference("appSet"));
    masterAppSet.setMembers(members);

    assertEquals(
        masterAppSet.toAclLineMatchExpr(jc, null),
        new OrMatchExpr(
            ImmutableList.of(
                new MatchHeaderSpace(
                    HeaderSpace.builder().build(),
                    ApplicationSetMember.getTraceElement(
                        "host", JuniperStructureType.APPLICATION, "app1")),
                new OrMatchExpr(
                    ImmutableList.of(
                        new MatchHeaderSpace(
                            HeaderSpace.builder().build(),
                            ApplicationSetMember.getTraceElement(
                                "host", JuniperStructureType.APPLICATION, "app2"))),
                    ApplicationSetMember.getTraceElement(
                        "host", JuniperStructureType.APPLICATION_SET, "appSet"))),
            ApplicationSetMember.getTraceElement(
                "host", JuniperStructureType.APPLICATION_SET, "masterAppSet")));
  }
}
