package org.batfish.representation.juniper;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Before;
import org.junit.Test;

public class FwFromDscpTest {
  private JuniperConfiguration _jc;
  private Warnings _w;
  private Configuration _c;

  private static final String BASE_ALIAS = "alias";
  private static final int BASE_VALUE = 3;

  @Before
  public void setup() {
    _jc = new JuniperConfiguration();
    _jc.getMasterLogicalSystem().getDscpAliases().put(BASE_ALIAS, BASE_VALUE);
    _w = new Warnings(true, true, true);
    _c = new Configuration("test", ConfigurationFormat.FLAT_JUNIPER);
  }

  @Test
  public void testToAclLineMatchExpr() {
    // configured custom alias
    assertEquals(
        new FwFromDscp(BASE_ALIAS).toAclLineMatchExpr(_jc, _c, _w),
        new MatchHeaderSpace(
            HeaderSpace.builder().setDscps(ImmutableList.of(BASE_VALUE)).build(),
            TraceElement.of("Matched DSCP alias")));

    // builtin alias
    assertEquals(
        new FwFromDscp("cs1").toAclLineMatchExpr(_jc, _c, _w),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setDscps(ImmutableList.of(DscpUtil.defaultValue("cs1").get()))
                .build(),
            TraceElement.of("Matched DSCP cs1")));

    // constant value
    assertEquals(
        new FwFromDscp("3").toAclLineMatchExpr(_jc, _c, _w),
        new MatchHeaderSpace(
            HeaderSpace.builder().setDscps(ImmutableList.of(3)).build(),
            TraceElement.of("Matched DSCP 3")));

    assertTrue(_w.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testToAclLineMatchExpr_undefinedAlias() {
    assertEquals(
        new FwFromDscp("other").toAclLineMatchExpr(_jc, _c, _w),
        new FalseExpr(TraceElement.of("Treated undefined DSCP alias 'other' as not matching")));
  }

  @Test
  public void testToAclLineMatchExpr_badConstant() {
    assertEquals(
        new FwFromDscp("93").toAclLineMatchExpr(_jc, _c, _w),
        new FalseExpr(TraceElement.of("Treated illegal DSCP value '93' as not matching")));
  }
}
