package org.batfish.client;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.batfish.common.CoordConsts;
import org.codehaus.jettison.json.JSONArray;

public class BfCoordPoolHelper {

   private String _coordPoolMgr;

   public BfCoordPoolHelper(String poolMgr) {
      _coordPoolMgr = poolMgr;
   }

   public boolean addBatfishWorker(String worker) {
      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", _coordPoolMgr,
                     CoordConsts.SVC_BASE_POOL_MGR,
                     CoordConsts.SVC_POOL_UPDATE_RSC))
               .queryParam("add", worker);
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while checking work status: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         System.err.printf("unable to connect to %s: %s\n", _coordPoolMgr, e
               .getStackTrace().toString());
         return false;
      }
      catch (Exception e) {
         System.err.printf("exception: ");
         e.printStackTrace();
         return false;
      }
   }

}
