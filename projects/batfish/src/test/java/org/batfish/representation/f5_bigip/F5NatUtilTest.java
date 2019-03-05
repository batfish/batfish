package org.batfish.representation.f5_bigip;

import static org.batfish.representation.f5_bigip.F5NatUtil.addOrElses;
import static org.batfish.representation.f5_bigip.F5NatUtil.chain;
import static org.batfish.representation.f5_bigip.F5NatUtil.orElseChain;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests of {@link F5NatUtil} */
@ParametersAreNonnullByDefault
public final class F5NatUtilTest {

  @Test
  public void testAddOrElsesBaseCase() {
    ImmutableList.Builder<Transformation> outputList = ImmutableList.builder();
    Transformation input = null;
    addOrElses(outputList, input);

    assertThat(outputList.build(), empty());
  }

  @Test
  public void testAddOrElsesNested() {
    ImmutableList.Builder<Transformation> outputList = ImmutableList.builder();
    Transformation nested = Transformation.always().build();
    Transformation input = Transformation.always().setOrElse(nested).build();
    addOrElses(outputList, input);

    assertThat(outputList.build(), contains(nested, input));
  }

  @Test
  public void testAddOrElsesNoOrElse() {
    ImmutableList.Builder<Transformation> outputList = ImmutableList.builder();
    Transformation input = Transformation.always().build();
    addOrElses(outputList, input);

    assertThat(outputList.build(), contains(input));
  }

  @Test
  public void testChainDiscardsOrElse() {
    Transformation transformation =
        Transformation.always().setOrElse(Transformation.always().build()).build();
    Collection<Transformation> transformations = ImmutableList.of(transformation);

    assertThat(chain(transformations), equalTo(Transformation.always().build()));
  }

  @Test
  public void testChainMultiple() {
    Collection<Transformation> transformations =
        ImmutableList.of(
            Transformation.when(FalseExpr.INSTANCE).build(), Transformation.always().build());

    assertThat(
        chain(transformations),
        equalTo(
            Transformation.always()
                .setOrElse(Transformation.when(FalseExpr.INSTANCE).build())
                .build()));
  }

  @Test
  public void testChainSingleton() {
    Transformation transformation = Transformation.always().build();
    Collection<Transformation> transformations = ImmutableList.of(transformation);

    assertThat(chain(transformations), equalTo(transformation));
  }

  @Test
  public void testOrElseChainEmpty() {
    assertThat(orElseChain(ImmutableList.of()), nullValue());
  }

  @Test
  public void testOrElseChainMultiple() {
    Collection<Transformation> transformations =
        ImmutableList.of(
            Transformation.when(FalseExpr.INSTANCE)
                .setOrElse(Transformation.always().build())
                .build(),
            Transformation.always().build());

    assertThat(
        orElseChain(transformations),
        equalTo(
            Transformation.always()
                .setOrElse(
                    Transformation.when(FalseExpr.INSTANCE)
                        .setOrElse(Transformation.always().build())
                        .build())
                .build()));
  }

  @Test
  public void testOrElseChainSingleton() {
    Transformation t1 = Transformation.always().build();
    assertThat(orElseChain(ImmutableList.of(t1)), equalTo(t1));
  }
}
