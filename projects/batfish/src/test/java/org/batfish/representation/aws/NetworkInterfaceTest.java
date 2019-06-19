package org.batfish.representation.aws;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.CommonUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

public class NetworkInterfaceTest {

  @Test
  public void attachments() throws JSONException {
    String interfaces =
        CommonUtil.readResource("org/batfish/representation/aws/NetworkInterfaceTest.json");

    JSONObject jObj = new JSONObject(interfaces);
    JSONArray ifaceArray = jObj.getJSONArray("NetworkInterfaces");

    NetworkInterface netIface1 = new NetworkInterface(ifaceArray.getJSONObject(0));
    NetworkInterface netIface2 = new NetworkInterface(ifaceArray.getJSONObject(1));

    assertThat(netIface1.getAttachmentInstanceId(), equalTo("i-05f467abe21e9b883"));
    assertThat(netIface2.getAttachmentInstanceId(), equalTo(null));
  }
}
