package org.batfish.answerer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.batfish.common.BatfishException;
import org.batfish.common.util.ZipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ProtocolDependenciesAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;

public class ProtocolDependenciesAnswerer extends Answerer {

   public ProtocolDependenciesAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      
      _batfish.checkConfigurations(testrigSettings);
      Map<String, Configuration> configurations = _batfish.loadConfigurations(testrigSettings);

      ProtocolDependencyAnalysis analysis = new ProtocolDependencyAnalysis(
            configurations);
      analysis.printDependencies(_logger);
      analysis.writeGraphs(_batfish, _logger);
      Path protocolDependencyGraphPath = testrigSettings.getProtocolDependencyGraphPath();
      Path protocolDependencyGraphZipPath = testrigSettings.getProtocolDependencyGraphZipPath();
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
      
      return answerElement;
   }

}
