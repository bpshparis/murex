package com.bpshparis.wsvc.app0;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.watson.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions;

/**
 * Servlet implementation class AppendSelectionsServlet
 */
@WebServlet(name = "AnalyzeServlet", urlPatterns = { "/Analyze" })
public class AnalyzeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	NaturalLanguageUnderstanding nlu;
	ToneAnalyzer ta;
	VisualRecognition wvc;
	String wvcUrl;
	String wvcClassify;
	String wvcDetect_faces;
	String mailsPath;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AnalyzeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub


		@SuppressWarnings("unused")
		Map<String, Object> reqParms = new HashMap<String, Object>();
		Map<String, Object> datas = new HashMap<String, Object>();

		try {

			datas.put("FROM", this.getServletName());
			mailsPath = getServletContext().getRealPath("/res/mails");
			
			Map<String, Object> init = (Map<String, Object>) request.getServletContext().getAttribute("init");
			
			if(init != null) {
				if(((String) init.get("STATUS")).equalsIgnoreCase("KO")) {
					datas.put("INIT", init);
					throw new Exception("Init KO");
				}
			}


			wvcUrl = (String) request.getServletContext().getAttribute("wvcUrl");
			wvcClassify = (String) request.getServletContext().getAttribute("wvcClassify");
			wvcDetect_faces = (String) request.getServletContext().getAttribute("wvcDetect_faces");

			List<Mail> mails = (List<Mail>) request.getServletContext().getAttribute("mails");
				
			if(mails != null) {
				for(Mail mail: mails){
	
					if(mail.getAnalysis().getTav1() == null) {
						ta = (ToneAnalyzer) request.getServletContext().getAttribute("ta");
						if(mail.getSubject() != null && ta != null){
							callTA(mail);
						}
					}
					
					if(mail.getAnalysis().getNlu() == null) {
						nlu = (NaturalLanguageUnderstanding) request.getServletContext().getAttribute("nlu");
						if(mail.getContent() != null && nlu != null){
							callNLU(mail);
						}
					}
	
					wvc = (VisualRecognition) request.getServletContext().getAttribute("wvc");
	
					if(mail.getAnalysis().getVr() == null) {
						if(mail.getPicture() != null && wvc != null){
							if(Files.exists(Paths.get(mailsPath + "/" + mail.getPicture()))){
								callVR(mail);
							}
						}
					}
	
//					if(mail.getAnalysis().getFr() == null) {
//						if(mail.getFace() != null && wvc != null){
//							if(Files.exists(Paths.get(mailsPath + "/" + mail.getFace()))){
//								callFR(mail);
//							}
//						}
//					}
					
					Path mailsFile = Paths.get(mailsPath + "/mails.json");
					

					Files.write(mailsFile, Tools.toJSON(mails).getBytes("UTF-8"));
					mailsFile.toFile().setReadable(true);
					mailsFile.toFile().setWritable(true);
					mailsFile.toFile().setExecutable(true);
	
				}
			}
			else {
				mails = new ArrayList<Mail>();
			}
			datas.put("STATUS", "OK");
			datas.put("MAILS", mails);
		}
		
		catch(JsonMappingException e){
			e.printStackTrace();

		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			datas.put("STATUS", "KO");
			datas.put("EXCEPTION", e.getClass().getName());
			datas.put("MESSAGE", e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			datas.put("STACKTRACE", sw.toString());
			e.printStackTrace(System.err);
		}

		finally{
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(Tools.toJSON(datas));
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void callVR(Mail mail) throws IOException{

		Path path = Paths.get(mailsPath + "/" + mail.getPicture());
		if(!Files.exists(path)){
			return;
		}
		
		ClassifyOptions classifyImagesOptions = new ClassifyOptions.Builder()
				.acceptLanguage(mail.getLanguage())
				.imagesFile(Files.newInputStream(path))
				.imagesFilename(mail.getPicture())
				.build();
		
		ClassifiedImages visualClassification = wvc.classify(classifyImagesOptions).execute().getResult();
		
		String result = visualClassification.toString();
		
		InputStream is = new ByteArrayInputStream(result.getBytes());
		
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Map<String, Object> svc = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});
        
        List<Object> images = (List<Object>) svc.get("images");
		
		Map<String, Object> images0 = (Map<String, Object>) images.get(0);
		
		List<Object> classifiers = (List<Object>) images0.get("classifiers");
		
		if(classifiers != null){
		
			Map<String, Object> classifiers0 = (Map<String, Object>) classifiers.get(0);
			
			String json = mapper.writeValueAsString(classifiers0.get("classes"));
			
			List<VRClass> classes = Arrays.asList(mapper.readValue(json, VRClass[].class));
			
			for(VRClass vrClass: classes){
				vrClass.setPicture(mail.getPicture());
			}
			
			VR vr = new VR();
			vr.setClasses(classes);
			
			mail.getAnalysis().setVr(vr);
			
		}
		
		return;
	}

//	@SuppressWarnings("unchecked")
//	protected void callFR(Mail mail) throws IOException{
//
//		Path path = Paths.get(mailsPath + "/" + mail.getFace());
//		if(!Files.exists(path)){
//			return;
//		}
//
//		DetectFacesOptions options = new DetectFacesOptions.Builder()
//				.imagesFile(Files.newInputStream(path))
//				.imagesFilename(mail.getFace())
//				.build();
//
//		DetectedFaces detectedFaces = wvc.detectFaces(options).execute();
//		
//		String result = detectedFaces.toString();
//
//		InputStream is = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8.name()));
//		
//		InputStreamReader isr = new InputStreamReader(is);
//		BufferedReader br = new BufferedReader(isr);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        Map<String, Object> svc = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});
//		
//		List<Object> images = (List<Object>) svc.get("images");
//		
//		Map<String, Object> image0 = (Map<String, Object>) images.get(0);
//		
//		List<Map<String, Object>> fs = (List<Map<String, Object>>) image0.get("faces");
//		
//		List<Face> faces= new ArrayList<Face>();
//		
//		for(Map<String, Object> f: fs){
//			Face face = new Face();
//			Map<String, Object> age = (Map<String, Object>) f.get("age");
//			face.setAgeMax((Integer) age.get("max"));
//			face.setAgeMin((Integer) age.get("min"));
//			face.setAgeScore((Double) age.get("score"));
//			Map<String, Object> gender = (Map<String, Object>) f.get("gender");
//			face.setGender((String) gender.get("gender"));
//			face.setGenderScore((Double) gender.get("score"));
//			Map<String, Object> identity = (Map<String, Object>) f.get("identity");
//			if(identity != null){
//				face.setIdentityName((String) identity.get("name"));
//				face.setIdentityScore((Double) identity.get("score"));
//				face.setIdentityTypeHierarchy((String) identity.get("type_hierarchy"));
//			}
//			Map<String, Object> location = (Map<String, Object>) f.get("face_location");
//			face.setLocationHeight((Double) location.get("height"));
//			face.setLocationLeft((Double) location.get("left"));
//			face.setLocationTop((Double) location.get("top"));
//			face.setLocationWidth((Double) location.get("width"));
//			
//			face.setFace(mail.getFace());
//			faces.add(face);
//		}
//		
//		FR fr = new FR();
//		fr.setFaces(faces);
//
//		mail.getAnalysis().setFr(fr);
//
//		return;
//	}

	@SuppressWarnings("unchecked")
	protected ToneAnalysis callTA(Mail mail) throws IOException{
		
		if(mail.getSubject() == null | mail.getSubject().isEmpty()){
			return null;
		}

		ToneOptions options = new ToneOptions.Builder()
				.text(mail.getSubject())
				.contentLanguage(mail.getLanguage())
				.acceptLanguage("en")
				.build();

		ToneAnalysis toneAnalysis = ta.tone(options).execute().getResult();
		
		String result = toneAnalysis.toString();
		
		InputStream is = new ByteArrayInputStream(result.getBytes());
		
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Map<String, Object> json = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});

		Map<String, Object> document_tone = (Map<String, Object>) json.get("document_tone");
		
		List<Tone> documentTones = new ArrayList<Tone>();

		Map<String, Double> emotion = new HashMap<String, Double>();
		Map<String, Double> language = new HashMap<String, Double>();
		
		if(document_tone != null){
			List<Map<String, Object>> tonesForDocument = (List<Map<String, Object>>) document_tone.get("tones");
			
			if(tonesForDocument != null){
				
				documentTones = Arrays.asList(mapper.readValue(Tools.toJSON(tonesForDocument), Tone[].class));
				
				
				for(Tone documentTone: documentTones){
					switch(documentTone.getTone_id().toLowerCase()){
					
					case "joy": case"sadness": case "disgust": case "anger":  case "fear":
						emotion.put(documentTone.getTone_id(), documentTone.getScore());
						break;
					case  "analytical": case "confident": case "tentative":
						language.put(documentTone.getTone_id(), documentTone.getScore());
						break;
					}
				}
				
			}
		}
		
		TAV1 tav1 = new TAV1();
		tav1.setContent(mail.getSubject());
		tav1.setEmotion(emotion);
		tav1.setLanguage(language);

		if(mail != null) {
			mail.getAnalysis().setTav1(tav1);
		}

		return toneAnalysis;
	}

	@SuppressWarnings("unchecked")
	protected AnalysisResults callNLU(Mail mail) throws IOException{

		if(mail.getContent() == null | mail.getContent().isEmpty()){
			return null;
		}
		
		EntitiesOptions entitiesOptions = new EntitiesOptions.Builder()
			.emotion(true)
			.sentiment(true)
			.build();

		KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
			.emotion(true)
			.sentiment(true)
			.build();

		CategoriesOptions categoriesOptions = new CategoriesOptions.Builder()
				.build();

		Features features = new Features.Builder()
			.categories(categoriesOptions)
			.entities(entitiesOptions)
			.keywords(keywordsOptions)
			.build();

		AnalyzeOptions parameters = new AnalyzeOptions.Builder()
			.features(features)
			.language(mail.getLanguage())
			.text(mail.getContent())
			.build();

		AnalysisResults analysisResults = nlu
			.analyze(parameters)
			.execute().getResult();
		
		String result = analysisResults.toString();
		
		InputStream is = new ByteArrayInputStream(result.getBytes());
		
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Map<String, Object> svc = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});
        
//        Map<String, Object> svc = (Map<String, Object>) Tools.fromJSON(result, new TypeReference<Map<String, Object>>(){});
        

		String language = (String) svc.get("language");
		
		String json = Tools.toJSON(svc.get("entities"));
		List<Entity> entities = Arrays.asList(mapper.readValue(json, Entity[].class));
		for(Entity entity: entities){
			entity.setContent(mail.getContent());
			entity.setLanguage(language);
		}
		
		json = mapper.writeValueAsString((List<Object>) svc.get("keywords"));
		List<Keyword> keywords = Arrays.asList(mapper.readValue(json, Keyword[].class));
		for(Keyword keyword: keywords){
			keyword.setContent(mail.getContent());
			keyword.setLanguage(language);
		}

		json = mapper.writeValueAsString((List<Object>) svc.get("categories"));
		List<Category> categories = Arrays.asList(mapper.readValue(json, Category[].class));
		for(Category category: categories){
			category.setContent(mail.getContent());
			category.setLanguage(language);
		}

		NLU nlu = new NLU();
		nlu.setLanguage(language);
		nlu.setEntities(entities);
		nlu.setKeywords(keywords);
		nlu.setCategories(categories);
		
		mail.getAnalysis().setNlu(nlu);

		return analysisResults;
	}

}
