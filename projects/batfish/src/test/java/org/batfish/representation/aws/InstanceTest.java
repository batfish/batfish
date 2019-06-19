package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INSTANCES;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RESERVATIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.Instance.Status;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

public class InstanceTest {

  @Test
  public void reservations() throws JSONException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/InstanceTest.json");

    JSONObject jObj = new JSONObject(text);
    JSONArray reservationArray = jObj.getJSONArray(JSON_KEY_RESERVATIONS);
    List<Instance> instances = new LinkedList<>();

    for (int index = 0; index < reservationArray.length(); index++) {
      JSONArray instanceArray =
          reservationArray.getJSONObject(index).getJSONArray(JSON_KEY_INSTANCES);
      for (int instIndex = 0; instIndex < instanceArray.length(); instIndex++) {
        instances.add(new Instance(instanceArray.getJSONObject(instIndex)));
      }
    }

    assertThat(instances.size(), equalTo(2));
    assertThat(instances.get(0).getInstanceId(), equalTo("i-08e529f98f5659289"));
    assertThat(instances.get(0).getStatus(), equalTo(Status.PENDING));
    assertThat(instances.get(1).getInstanceId(), equalTo("i-05f467abe21e9b883"));
    assertThat(instances.get(1).getStatus(), equalTo(Status.STOPPED));
  }
}
