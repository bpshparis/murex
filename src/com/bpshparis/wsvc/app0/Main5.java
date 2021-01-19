package com.bpshparis.wsvc.app0;

import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;

public class Main5 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Authenticator authenticator = new IamAuthenticator("-VQoTtHmjAFj_uRVbINpttXmbQJ9PNf2ljD6U5iTRqjm");
		NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2019-07-12", authenticator);
		service.setServiceUrl("https://api.eu-de.natural-language-understanding.watson.cloud.ibm.com/instances/36876ed1-cc91-4236-8eb4-09c6ab867706");

		EntitiesOptions entities = new EntitiesOptions.Builder()
		  .sentiment(true)
		  .limit(1L)
		  .build();
		Features features = new Features.Builder()
		  .entities(entities)
		  .build();
		AnalyzeOptions parameters = new AnalyzeOptions.Builder()
		  .url("www.cnn.com")
		  .features(features)
		  .build();

		AnalysisResults results = service.analyze(parameters).execute().getResult();
		System.out.println(results);
		
	}

}
