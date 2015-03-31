package org.batfish.client;

import org.batfish.common.CoordConsts;

public class Main {

   public static void main(String[] args) {
    
      //default values
      String workMgr = "localhost:" + CoordConsts.SVC_WORK_PORT;
      String poolMgr = "localhost:" + CoordConsts.SVC_POOL_PORT;
      String testrigName = "example";
      String testrigZipfileName = "example.zip";

      //if arguments are supplied
      if (args.length == 4) {         
         workMgr = args[0];
         poolMgr = args[1];
         testrigName = args[2];
         testrigZipfileName = args[3];      
      }
      
      SampleClient client = new SampleClient(workMgr, poolMgr, testrigName, testrigZipfileName);
                 
   }
}



