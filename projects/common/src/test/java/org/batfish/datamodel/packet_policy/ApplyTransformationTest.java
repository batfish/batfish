package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests of {@link ApplyTransformation} */
public class ApplyTransformationTest {
  @Test
  public void testEquals() {
    ApplyTransformation at =
        new ApplyTransformation(Transformation.always().apply(ImmutableList.of()).build());
    new EqualsTester()
        .addEqualityGroup(
            at,
            at,
            new ApplyTransformation(Transformation.always().apply(ImmutableList.of()).build()))
        .addEqualityGroup(
            new ApplyTransformation(
                Transformation.when(FalseExpr.INSTANCE).apply(ImmutableList.of()).build()))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ApplyTransformation at =
        new ApplyTransformation(Transformation.always().apply(ImmutableList.of()).build());
    assertThat(SerializationUtils.clone(at), equalTo(at));
  }

  @Test
  public void testJsonSerialization() {
    ApplyTransformation at =
        new ApplyTransformation(Transformation.always().apply(ImmutableList.of()).build());
    assertThat(BatfishObjectMapper.clone(at, ApplyTransformation.class), equalTo(at));
  }
}
