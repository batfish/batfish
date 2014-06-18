package batfish.logicblox;

public class LBInitializationException extends Exception {

   public LBInitializationException(Throwable cause) {
      super(cause);
   }

   public boolean workspaceDoesNotExist() {
      return this.getMessage().contains("' does not exist.");
   }
   
   /**
    * 
    */
   private static final long serialVersionUID = -7187289109861214346L;

}
