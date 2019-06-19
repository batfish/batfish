package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link Transformation}. */
public class TransformationTest {
  @Test
  public void testEquals() {
    Transformation trivial = always().apply(NOOP_SOURCE_NAT).build();
    new EqualsTester()
        .addEqualityGroup(trivial, always().apply(NOOP_SOURCE_NAT).build())
        .addEqualityGroup(when(FALSE).apply(NOOP_SOURCE_NAT).build())
        .addEqualityGroup(always().apply(shiftDestinationIp(Prefix.ZERO)).build())
        .addEqualityGroup(always().apply(NOOP_SOURCE_NAT).setAndThen(trivial).build())
        .addEqualityGroup(always().apply(NOOP_SOURCE_NAT).setOrElse(trivial).build())
        .testEquals();
  }
}
