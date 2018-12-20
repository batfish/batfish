package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
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
    new EqualsTester()
        .addEqualityGroup(always().build(), always().build())
        .addEqualityGroup(when(FALSE).build(), when(FALSE).build())
        .addEqualityGroup(
            always().apply(shiftDestinationIp(Prefix.ZERO)).build(),
            always().apply(shiftDestinationIp(Prefix.ZERO)))
        .addEqualityGroup(
            always().setAndThen(always().build()).build(),
            always().setAndThen(always().build()).build())
        .addEqualityGroup(
            always().setOrElse(always().build()).build(),
            always().setOrElse(always().build()).build())
        .testEquals();
  }
}
