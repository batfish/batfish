package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.referencelibrary.ServiceObject;
import org.junit.Test;

public class ServiceObjectBeanTest {

  @Test
  public void conversionToAndFrom() {
    ServiceObject serviceObject = new ServiceObject(IpProtocol.TCP, "so", new SubRange(1, 2));
    ServiceObjectBean bean = new ServiceObjectBean(serviceObject);
    assertThat(new ServiceObjectBean(bean.toServiceObject()), equalTo(bean));
  }
}
