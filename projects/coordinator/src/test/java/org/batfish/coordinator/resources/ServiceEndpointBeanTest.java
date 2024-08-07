package org.batfish.coordinator.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.referencelibrary.ServiceEndpoint;
import org.junit.Test;

public class ServiceEndpointBeanTest {

  @Test
  public void conversionToAndFrom() {
    ServiceEndpoint endpoint = new ServiceEndpoint("address", "sep", "service");
    ServiceEndpointBean bean = new ServiceEndpointBean(endpoint);
    assertThat(new ServiceEndpointBean(bean.toServiceEndpoint()), equalTo(bean));
  }
}
