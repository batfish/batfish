package org.batfish.client;

import org.batfish.common.CoordConsts;

public class Main {

   public static void main(String[] args) {
    
      //default values
      String coordinator = "localhost:" + CoordConsts.SVC_WORK_PORT;
      String testrigName = "sample";
      String testrigZipfileName = "sample.zip";

      //if arguments are supplied
      if (args.length == 3) {
         
         coordinator = args[0];
         testrigName = args[1];
         testrigZipfileName = args[2];      
      }
      
      SampleClient client = new SampleClient(coordinator, testrigName, testrigZipfileName);
                 
   }
}



