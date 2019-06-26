package com.bpshparis.wsvc.app0;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.watson.developer_cloud.discovery.v1.Discovery;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;

/**
 * Application Lifecycle Listener implementation class ContextListener
 *
 */
@WebListener
public class ContextListener implements ServletContextListener {

	InitialContext ic;
	NaturalLanguageUnderstanding nlu;
	String vcap_services;
	String realPath;
	Properties props = new Properties();
	ToneAnalyzer ta;
	VisualRecognition wvc;
	List<Resource> resources;
	Map<String, Object> init = new HashMap<String, Object>();
	
    /**
     * Default constructor. 
     */
    public ContextListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
       	try {
       		
    			ic = new InitialContext();
    			arg0.getServletContext().setAttribute("ic", ic);
    			realPath = arg0.getServletContext().getRealPath("/"); 
    	    	props.load(new FileInputStream(realPath + "/res/conf.properties"));
    			arg0.getServletContext().setAttribute("props", props);
    	    	
    			System.out.println("Context has been initialized...");
    			
    			List<Mail> mails = new ArrayList<Mail>();
    			Path mailsFile = Paths.get(realPath + "/res/mails/mails.json");
    			if(Files.exists(mailsFile)){
    				InputStream is = new ByteArrayInputStream(Files.readAllBytes(mailsFile));
    				mails = Tools.MailsListFromJSON(is);
    			}

    			int mailCount = mails.size();
    			init.put("MAILCOUNT", mailCount);
    			
    			initVCAP_SERVICES();
    			System.out.println("VCAP_SERVICES has been initialized...");

   				initNLU();
    			System.out.println("NLU has been initialized...");
				arg0.getServletContext().setAttribute("nlu", nlu);
				String nlul = props.getProperty("NLU_LANGUAGE");
		    	if(nlul != null && !nlul.trim().isEmpty()){
					arg0.getServletContext().setAttribute("nlul", nlul);
		    	}

    			initTA();
    			System.out.println("TA has been initialized...");
				arg0.getServletContext().setAttribute("ta", ta);
				String tacl = props.getProperty("TA_CONTENT_LANGUAGE");
		    	if(tacl != null && !tacl.trim().isEmpty()){
					arg0.getServletContext().setAttribute("tacl", tacl);
		    	}
				
				initWVC();
    			System.out.println("WVC has been initialized...");
				arg0.getServletContext().setAttribute("wvc", wvc);
				arg0.getServletContext().setAttribute("wvcUrl", props.getProperty("WVC_URL"));
				arg0.getServletContext().setAttribute("wvcClassify", props.getProperty("WVC_CLASSIFY"));
				arg0.getServletContext().setAttribute("wvcDetect_faces", props.getProperty("WVC_DETECT_FACES"));
				
    			
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace(System.err);
    		}    	
       		finally {
    			arg0.getServletContext().setAttribute("init", init);
			}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    	arg0.getServletContext().removeAttribute("ic");
		System.out.println("Context has been destroyed...");    	
    }
    
    @SuppressWarnings("unchecked")
	public void initVCAP_SERVICES() throws Exception{
    	
		vcap_services = System.getenv("VCAP_SERVICES");
		System.out.println("VCAP_SERVICES read from System ENV.");

		System.out.println("vcap_services=" + vcap_services);
		
		if(vcap_services == null) {
			init.put("STATUS", "KO");
			init.put("RESULT","Watson services are not reachable.");
			init.put("TROUBLESHOOTING","VCAP_SERVICES environment variable should display something like: {\"0\":{\"credentials\":[{\"id\":\"0\",\"name\":\"Auto-generated service credentials\",\"apikey\":\"4Pe0RUDXt......");
			String msg = "VCAP_SERVICES is null";
			init.put("MSG", msg);
			throw new Exception(msg);
		}
		
		try {
			Map<String, Resource> json = (Map<String, Resource>) Tools.fromJSON(vcap_services, new TypeReference<Map<String, Resource>>(){});
			resources = Arrays.asList(json.values().toArray(new Resource[0]));
			init.put("STATUS", "OK");
			init.put("VCAP_SERVICES", json);
		}
		catch(Exception e) {
			init.put("STATUS", "KO");
			init.put("RESULT","Watson services are not reachable.");
			init.put("TROUBLESHOOTING","VCAP_SERVICES environment variable should display something like: {\"0\":{\"credentials\":[{\"id\":\"0\",\"name\":\"Auto-generated service credentials\",\"apikey\":\"4Pe0RUDXt......");
			String msg = "No valid resource were set.";
			init.put("MSG", msg);
			throw new Exception(msg);
		}

    }
    
    public void initNLU() throws JsonParseException, JsonMappingException, IOException{

    	String serviceName = props.getProperty("NLU_NAME");
    	
		String url = "";
		String username = "apikey";
		String password = "";
		String version = props.getProperty("NLU_METHOD").split("=")[1];
		
		for(Resource resource: resources) {
			if(resource.getService().equalsIgnoreCase(serviceName)) {
				password = resource.getCredentials().get(0).getApikey();
				url = resource.getCredentials().get(0).getUrl();
			}
		}
		
		nlu = new NaturalLanguageUnderstanding(version, username, password);
		nlu.setEndPoint(url);
		
		System.out.println(nlu.getName() + " " + nlu.getEndPoint());
		
		return;
    }
    
    public void initTA() throws JsonParseException, JsonMappingException, IOException{

    	String serviceName = props.getProperty("TA_NAME");
    	
		String url = "";
		String username = "apikey";
		String password = "";
		String version = props.getProperty("TA_METHOD").split("=")[1];
		
		for(Resource resource: resources) {
			if(resource.getService().equalsIgnoreCase(serviceName)) {
				password = resource.getCredentials().get(0).getApikey();
				url = resource.getCredentials().get(0).getUrl();
			}
		}
		
		try {
		
			ta = new ToneAnalyzer(version, username, password);
			ta.setEndPoint(url);
			
			System.out.println(ta.getName() + " " + ta.getEndPoint());
			
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
		
		return;
    }    

    @SuppressWarnings({ "unused" })
	public void initWVC() throws JsonParseException, JsonMappingException, IOException{
    	
        String serviceName = props.getProperty("WVC_NAME");
        String url = "";
		String username = "apikey";
		String password = "";
        String version = props.getProperty("WVC_CLASSIFY_METHOD").split("=")[1];
    	
		for(Resource resource: resources) {
			if(resource.getService().equalsIgnoreCase(serviceName)) {
				password = resource.getCredentials().get(0).getApikey();
				url = resource.getCredentials().get(0).getUrl();
			}
		}
    	
		IamOptions options = new IamOptions.Builder()
				.apiKey(password)
				.build();

		wvc = new VisualRecognition(version, options);
		
		System.out.println(wvc.getName() + " " + wvc.getEndPoint());
		
		return;    	

    }
    
}
