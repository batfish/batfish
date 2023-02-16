package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConnectedRouteMetadata.Builder;
import org.junit.Test;

/** Tests of {@link ConnectedRouteMetadata} */
public class ConnectedRouteMetadataTest {
  @Test
  public void testEquals() {
    Builder builder = ConnectedRouteMetadata.builder();
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAdmin(1).build())
        .addEqualityGroup(builder.setGenerateLocalRoute(true).build())
        .addEqualityGroup(builder.setGenerateLocalRoute(false).build())
        .addEqualityGroup(builder.setTag(2).build())
        .addEqualityGroup(builder.setGenerateConnectedRoute(true).build())
        .addEqualityGroup(builder.setGenerateConnectedRoute(false).build())
        .addEqualityGroup(builder.setGenerateLocalNullRouteIfDown(true).build())
        .addEqualityGroup(builder.setGenerateLocalNullRouteIfDown(false).build())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ConnectedRouteMetadata crm =
        ConnectedRouteMetadata.builder()
            .setAdmin(2)
            .setGenerateLocalRoute(true)
            .setGenerateLocalNullRouteIfDown(true)
            .setTag(1)
            .build();
    assertThat(SerializationUtils.clone(crm), equalTo(crm));
  }

  @Test
  public void testJsonSerialization() {
    ConnectedRouteMetadata crm =
        ConnectedRouteMetadata.builder()
            .setAdmin(2)
            .setGenerateConnectedRoute(false)
            .setGenerateLocalRoute(true)
            .setGenerateLocalNullRouteIfDown(true)
            .setTag(1)
            .build();
    assertThat(BatfishObjectMapper.clone(crm, ConnectedRouteMetadata.class), equalTo(crm));
  }
}
