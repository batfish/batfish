package org.batfish.client;

public class Main {

   public static void main(String[] args) {
    
      String coordinator = (args.length == 0)? "localhost:9998" : args[0];
        
      SampleClient client = new SampleClient(coordinator);
                 
   }
}



