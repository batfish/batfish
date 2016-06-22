package org.batfish.question;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.batfish.common.BatfishException;
import org.batfish.common.util.ZipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.ProtocolDependenciesAnswerElement;
import org.batfish.datamodel.questions.ProtocolDependenciesQuestion;
import org.batfish.main.Batfish;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;

public class ProtocolDependenciesAnswer extends Answer {

   public ProtocolDependenciesAnswer(Batfish batfish,
         ProtocolDependenciesQuestion question) {
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      ProtocolDependencyAnalysis analysis = new ProtocolDependencyAnalysis(
            configurations);
      analysis.printDependencies(batfish.getLogger());
      analysis.writeGraphs(batfish, batfish.getLogger());
      Path protocolDependencyGraphPath = batfish.getTestrigSettings()
            .getProtocolDependencyGraphPath();
      Path protocolDependencyGraphZipPath = batfish.getTestrigSettings()
            .getProtocolDependencyGraphZipPath();
      ZipUtility.zipFiles(protocolDependencyGraphPath.toString(),
            protocolDependencyGraphZipPath.toString());
      byte[] zipBytes;
      try {
         zipBytes = Files.readAllBytes(protocolDependencyGraphZipPath);
      }
      catch (IOException e) {
         throw new BatfishException("Could not read zip", e);
      }
      String zipBase64 = Base64.encodeBase64String(zipBytes);
      ProtocolDependenciesAnswerElement answerElement = new ProtocolDependenciesAnswerElement();
      answerElement.setZipBase64(zipBase64);
      addAnswerElement(answerElement);
   }

}
