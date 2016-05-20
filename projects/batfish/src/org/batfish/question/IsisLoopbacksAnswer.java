package org.batfish.question;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IsisInterfaceMode;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.IsisLoopbacksAnswerElement;
import org.batfish.datamodel.questions.IsisLoopbacksQuestion;
import org.batfish.main.Batfish;

public class IsisLoopbacksAnswer extends Answer {

	public IsisLoopbacksAnswer(Batfish batfish, IsisLoopbacksQuestion question) {

		Pattern nodeRegex;
		try {
			nodeRegex = Pattern.compile(question.getNodeRegex());
		}
		catch (PatternSyntaxException e) {
			throw new BatfishException(
					"Supplied regex for nodes is not a valid java regex: \""
							+ question.getNodeRegex() + "\"", e);
		}	      

		IsisLoopbacksAnswerElement answerElement = new IsisLoopbacksAnswerElement();
		addAnswerElement(answerElement);
		batfish.checkConfigurations();
		Map<String, Configuration> configurations = batfish.loadConfigurations();
		for (Entry<String, Configuration> e : configurations.entrySet()) {
			String hostname = e.getKey();
			if (!nodeRegex.matcher(hostname).matches())
				continue;
			Configuration c = e.getValue();
			for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
				String interfaceName = e2.getKey();
				Interface iface = e2.getValue();
				if (iface.isLoopback(c.getVendor())) {
					IsisInterfaceMode l1Mode = iface.getIsisL1InterfaceMode();
					IsisInterfaceMode l2Mode = iface.getIsisL2InterfaceMode();
					boolean l1 = false;
					boolean l2 = false;
					boolean isis = false;
					if (l1Mode == IsisInterfaceMode.ACTIVE) {
						l1 = true;
						answerElement.add(answerElement.getL1Active(), hostname,
								interfaceName);
					}
					else if (l1Mode == IsisInterfaceMode.PASSIVE) {
						l1 = true;
						answerElement.add(answerElement.getL1Passive(), hostname,
								interfaceName);
					}
					if (l2Mode == IsisInterfaceMode.ACTIVE) {
						l2 = true;
						answerElement.add(answerElement.getL2Active(), hostname,
								interfaceName);
					}
					else if (l2Mode == IsisInterfaceMode.PASSIVE) {
						l2 = true;
						answerElement.add(answerElement.getL2Passive(), hostname,
								interfaceName);
					}
					if (l1) {
						answerElement.add(answerElement.getL1(), hostname,
								interfaceName);
					}
					if (l2) {
						answerElement.add(answerElement.getL2(), hostname,
								interfaceName);
					}
					if (isis) {
						answerElement.add(answerElement.getRunning(), hostname,
								interfaceName);
					}
					else {
						answerElement.add(answerElement.getInactive(), hostname,
								interfaceName);
					}
				}
			}
		}
	}

}
