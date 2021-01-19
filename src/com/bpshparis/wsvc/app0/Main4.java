package com.bpshparis.wsvc.app0;

import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;

public class Main4 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Authenticator authenticator = new IamAuthenticator("nHzx8w5obn4FNmxzPO5YblMxBixT9LMyZTh9j2zlUndL");
		ToneAnalyzer service = new ToneAnalyzer("2017-09-21", authenticator);
		service.setServiceUrl("https://api.eu-de.tone-analyzer.watson.cloud.ibm.com/instances/91a6b00e-32ab-469e-8a41-2cca18bdc140");
		
		String text = "On en a gros !";
		
		// Call the service and get the tone
		ToneOptions toneOptions = new ToneOptions.Builder()
		  .text(text)
		  .acceptLanguage("fr")
		  .contentLanguage("fr")
		  .build();

		ToneAnalysis tone = service.tone(toneOptions).execute().getResult();
		System.out.println(tone);
		
	}

}
