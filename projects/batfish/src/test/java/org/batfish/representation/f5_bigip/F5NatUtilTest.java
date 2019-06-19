package org.batfish.representation.f5_bigip;

import static org.batfish.representation.f5_bigip.F5NatUtil.orElseChain;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests of {@link F5NatUtil} */
@ParametersAreNonnullByDefault
public final class F5NatUtilTest {

  @Test
  public void testOrElseChainEmpty() {
    assertThat(orElseChain(ImmutableList.of()), nullValue());
  }

  @Test
  public void testOrElseChainMultiple() {
    Collection<SimpleTransformation> transformations =
        ImmutableList.of(
            new SimpleTransformation(FalseExpr.INSTANCE, Noop.NOOP_DEST_NAT),
            new SimpleTransformation(FalseExpr.INSTANCE, Noop.NOOP_SOURCE_NAT),
            new SimpleTransformation(TrueExpr.INSTANCE, Noop.NOOP_SOURCE_NAT));

    assertThat(
        orElseChain(transformations),
        equalTo(
            Transformation.when(TrueExpr.INSTANCE)
                .apply(Noop.NOOP_SOURCE_NAT)
                .setOrElse(
                    Transformation.when(FalseExpr.INSTANCE)
                        .apply(Noop.NOOP_SOURCE_NAT)
                        .setOrElse(
                            Transformation.when(FalseExpr.INSTANCE)
                                .apply(Noop.NOOP_DEST_NAT)
                                .build())
                        .build())
                .build()));
  }

  @Test
  public void testOrElseChainSingleton() {
    SimpleTransformation t1 = new SimpleTransformation(TrueExpr.INSTANCE, Noop.NOOP_DEST_NAT);
    assertThat(
        orElseChain(ImmutableList.of(t1)),
        equalTo(Transformation.when(TrueExpr.INSTANCE).apply(Noop.NOOP_DEST_NAT).build()));
  }
}
