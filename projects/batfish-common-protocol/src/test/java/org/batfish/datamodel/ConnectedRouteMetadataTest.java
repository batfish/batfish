package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConnectedRouteMetadata.Builder;
import org.junit.Test;

/** Tests of {@link ConnectedRouteMetadata} */
public class ConnectedRouteMetadataTest {
  @Test
  public void testEquals() {
    Builder builder = ConnectedRouteMetadata.builder().setTag(1);
    ConnectedRouteMetadata crm = builder.build();
    new EqualsTester()
        .addEqualityGroup(crm, crm, builder.build())
        .addEqualityGroup(builder.setTag(2).build())
        .addEqualityGroup(builder.setTag(3).build())
        .addEqualityGroup(builder.setAdmin(3).build())
        .addEqualityGroup(builder.setTag(1).build())
        .addEqualityGroup(builder.setGenerateLocalRoutes(true).build())
        .addEqualityGroup(builder.setAdmin(1).setGenerateLocalRoutes(true))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ConnectedRouteMetadata crm =
        ConnectedRouteMetadata.builder().setAdmin(2).setGenerateLocalRoutes(true).setTag(1).build();
    assertThat(SerializationUtils.clone(crm), equalTo(crm));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ConnectedRouteMetadata crm =
        ConnectedRouteMetadata.builder().setAdmin(2).setGenerateLocalRoutes(true).setTag(1).build();
    assertThat(BatfishObjectMapper.clone(crm, ConnectedRouteMetadata.class), equalTo(crm));
  }
}
