package org.batfish.main;

import com.google.common.base.Throwables;
import java.util.Map;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.codehaus.jettison.json.JSONArray;

/** Helper class that implements (some) communication between Batfish worker and coordinator */
public final class CoordinatorClient {

  public static Object talkToCoordinator(
      String url, Map<String, String> params, Settings settings, BatfishLogger logger) {
    Client client = null;
    try {
      client = CommonUtil.createHttpClientBuilder(true).build();
      WebTarget webTarget = client.target(url);
      for (Map.Entry<String, String> entry : params.entrySet()) {
        webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
      }
      JSONArray array;
      try (Response response = webTarget.request(MediaType.APPLICATION_JSON).get()) {

        logger.debug(
            "BF: " + response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
          logger.error("Did not get an OK response\n");
          return null;
        }

        String sobj = response.readEntity(String.class);
        array = new JSONArray(sobj);
      }
      logger.debugf("BF: response: %s [%s] [%s]\n", array, array.get(0), array.get(1));

      if (!array.get(0).equals(CoordConsts.SVC_KEY_SUCCESS)) {
        logger.errorf(
            "BF: got error while talking to coordinator: %s %s\n", array.get(0), array.get(1));
        return null;
      }

      return array.get(1);
    } catch (ProcessingException e) {
      if (CommonUtil.causedBy(e, SSLHandshakeException.class)
          || CommonUtil.causedByMessage(e, "Unexpected end of file from server")) {
        throw new BatfishException("Unrecoverable connection error", e);
      }
      logger.errorf("BF: unable to connect to coordinator pool mgr at %s\n", url);
      logger.debug(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    } catch (Exception e) {
      logger.errorf("exception: " + Throwables.getStackTraceAsString(e));
      return null;
    } finally {
      if (client != null) {
        client.close();
      }
    }
  }

  private CoordinatorClient() {}
}
