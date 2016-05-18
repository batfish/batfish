package org.batfish.datamodel.answers;

public class ProtocolDependenciesAnswerElement implements AnswerElement {

   private String _zipBase64;

   public String getZipBase64() {
      return _zipBase64;
   }

   public void setZipBase64(String zipBase64) {
      _zipBase64 = zipBase64;
   }

}
