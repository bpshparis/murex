package com.bpshparis.wsvc.app0;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions;

public class Main3 {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String version = "2018-03-19";
//		String username = "apikey";
		String password = "3_QCFqpyW6ZcSmnpbY3uRqBIC6KgxxTYGhxPIR1kZKeX";
		String url = "https://gateway.watsonplatform.net/visual-recognition/api";
		Path path = Paths.get("/home/fr054721/slcdw/image0.jpg");
		
		Authenticator authenticator = new IamAuthenticator(password);
		com.ibm.watson.visual_recognition.v3.VisualRecognition service = new VisualRecognition(version, authenticator);
		service.setServiceUrl(url);
		
		System.out.println(service.getName());

		System.out.println("Classify an image");
		ClassifyOptions options = new ClassifyOptions.Builder()
		  .imagesFile(path.toFile()) // replace with path to file
		  .build();
		ClassifiedImages result = service.classify(options).execute().getResult();
		System.out.println(result);
	}

}
