package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Tests of {@link Policy} */
public class PolicyTest {

  private static Policy buildCompletePolicy() {
    Policy p = new Policy("1");
    p.getSrcIntf().add("srcIntf");
    p.getDstIntf().add("dstIntf");
    p.getSrcAddrUUIDs().add(new BatfishUUID(1));
    p.getDstAddrUUIDs().add(new BatfishUUID(2));
    p.getServiceUUIDs().add(new BatfishUUID(3));
    p.setValid(true);
    return p;
  }

  @Test
  public void testInvalidReason_validPolicy() {
    assertNull(buildCompletePolicy().getInvalidReason());
  }

  @Test
  public void testInvalidReason_markedInvalid() {
    Policy p = buildCompletePolicy();
    p.setValid(false);
    assertThat(p.getInvalidReason(), equalTo("name is invalid"));
  }

  @Test
  public void testInvalidReason_missingSrcIntf() {
    Policy p = buildCompletePolicy();
    p.getSrcIntf().clear();
    assertThat(p.getInvalidReason(), equalTo("srcintf must be set"));
  }

  @Test
  public void testInvalidReason_missingDstIntf() {
    Policy p = buildCompletePolicy();
    p.getDstIntf().clear();
    assertThat(p.getInvalidReason(), equalTo("dstintf must be set"));
  }

  @Test
  public void testInvalidReason_missingSrcAddr() {
    Policy p = buildCompletePolicy();
    p.getSrcAddrUUIDs().clear();
    assertThat(p.getInvalidReason(), equalTo("srcaddr must be set"));
  }

  @Test
  public void testInvalidReason_missingDstAddr() {
    Policy p = buildCompletePolicy();
    p.getDstAddrUUIDs().clear();
    assertThat(p.getInvalidReason(), equalTo("dstaddr must be set"));
  }

  @Test
  public void testInvalidReason_missingService() {
    Policy p = buildCompletePolicy();
    p.getServiceUUIDs().clear();
    assertThat(p.getInvalidReason(), equalTo("service must be set"));
  }
}
