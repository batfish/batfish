package org.batfish.question;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.batfish.common.BatfishException;
import org.batfish.common.ZipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ProtocolDependenciesAnswerElement;
import org.batfish.datamodel.questions.ProtocolDependenciesQuestion;
import org.batfish.main.Batfish;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;

public class ProtocolDependenciesAnswer extends Answer {

   public ProtocolDependenciesAnswer(Batfish batfish,
         ProtocolDependenciesQuestion question) {
      setQuestion(question);
      setStatus(AnswerStatus.SUCCESS);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      ProtocolDependencyAnalysis analysis = new ProtocolDependencyAnalysis(
            configurations);
      analysis.printDependencies(batfish.getLogger());
      analysis.writeGraphs(batfish.getSettings(), batfish.getLogger());
      String protocolDependencyGraphPath = batfish.getSettings()
            .getProtocolDependencyGraphPath();
      String protocolDependencyGraphZipPath = batfish.getSettings()
            .getProtocolDependencyGraphZipPath();
      ZipUtility.zipFiles(protocolDependencyGraphPath,
            protocolDependencyGraphZipPath);
      byte[] zipBytes;
      try {
         zipBytes = Files.readAllBytes(Paths
               .get(protocolDependencyGraphZipPath));
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
