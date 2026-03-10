package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public final class AclIpSpaceLineTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            AclIpSpaceLine.permit(UniverseIpSpace.INSTANCE),
            new AclIpSpaceLine(UniverseIpSpace.INSTANCE, LineAction.PERMIT))
        .addEqualityGroup(AclIpSpaceLine.reject(UniverseIpSpace.INSTANCE))
        .addEqualityGroup(AclIpSpaceLine.reject(Ip.parse("1.2.3.4").toIpSpace()))
        .testEquals();
  }

  @Test
  public void testInterning() {
    IpIpSpace space = Ip.parse("1.2.3.4").toIpSpace();
    AclIpSpaceLine permit = AclIpSpaceLine.permit(space);
    assertThat(AclIpSpaceLine.permit(space), sameInstance(permit));
    assertThat(AclIpSpaceLine.builder().setIpSpace(space).build(), sameInstance(permit));
    assertThat(
        AclIpSpaceLine.builder().setAction(LineAction.PERMIT).setIpSpace(space).build(),
        sameInstance(permit));
  }

  @Test
  public void testSerialization() {
    AclIpSpaceLine line =
        AclIpSpaceLine.builder()
            .setAction(LineAction.DENY)
            .setIpSpace(Ip.parse("1.2.3.4").toIpSpace())
            .build();
    assertThat(BatfishObjectMapper.clone(line, AclIpSpaceLine.class), sameInstance(line));
    assertThat(SerializationUtils.clone(line), sameInstance(line));
  }
}
