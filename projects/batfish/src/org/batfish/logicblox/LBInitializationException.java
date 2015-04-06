package org.batfish.logicblox;

public class LBInitializationException extends Exception {

   /**
    *
    */
   private static final long serialVersionUID = -7187289109861214346L;

   public LBInitializationException(Throwable cause) {
      super(cause);
   }

   public LBInitializationException(String msg, Exception e) {
      super(msg, e);
   }

   public boolean workspaceDoesNotExist() {
      return this.getMessage().contains("' does not exist.");
   }

}
