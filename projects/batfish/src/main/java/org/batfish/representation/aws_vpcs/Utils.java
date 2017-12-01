package org.batfish.representation.aws_vpcs;

import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Utils {

  public static Configuration newAwsConfiguration(String name) {
    Configuration config = new Configuration(name, ConfigurationFormat.AWS_VPC);
    config.setDefaultInboundAction(LineAction.ACCEPT);
    config.setDefaultCrossZoneAction(LineAction.ACCEPT);
    config.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new);
    return config;
  }

  public static boolean tryGetBoolean(JSONObject jsonObject, String key, boolean defaultValue)
      throws JSONException {
    if (jsonObject.has(key)) {
      return jsonObject.getBoolean(key);
    }
    return defaultValue;
  }

  public static int tryGetInt(JSONObject jsonObject, String key, int defaultValue)
      throws JSONException {
    if (jsonObject.has(key)) {
      return jsonObject.getInt(key);
    }
    return defaultValue;
  }

  @Nullable
  public static String tryGetString(JSONObject jsonObject, String key) throws JSONException {
    if (jsonObject.has(key)) {
      return jsonObject.getString(key);
    }
    return null;
  }

  private Utils() {}
}
