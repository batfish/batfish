package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.EmptyIpSpace;
import org.junit.Test;

public class AllSourcesTest {
  @Test
  public void testResolve() {
    Location src = new InterfaceLinkLocation("n1", "i1");
    Location nonSrc = new InterfaceLinkLocation("n2", "i2");
    SpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setLocationInfo(
                ImmutableMap.of(
                    src, new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE),
                    nonSrc, new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE)))
            .build();
    assertThat(AllSources.ALL_SOURCES.resolve(ctxt), contains(src));
  }
}
