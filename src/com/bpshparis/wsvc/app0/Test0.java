package com.bpshparis.wsvc.app0;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

public class Test0 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub

		Path path = Paths.get("/home/fr054721/slcdw/vcap-cf.json");
		
		if(Files.exists(path)){
			
			Map<String, Object> json = Tools.fromJSON(path.toFile());
			
			List<Resource> resources = new ArrayList<Resource>();
			
			for(Map.Entry<String, Object> entry: json.entrySet()){
				System.out.println(Tools.toJSON(entry.getValue()));
				List<Object> values = (List<Object>) entry.getValue();
				Map<String, Object> value = (Map<String, Object>) values.get(0);
				String name = (String) value.get("name");
				System.out.println(name);
				Credential cred = (Credential) Tools.fromJSON(Tools.toJSON(value.get("credentials")), new TypeReference<Credential>(){});
				System.out.println(Tools.toJSON(cred));
				Resource resource = new Resource();
				resource.setName(name);
				resource.setCredentials(Arrays.asList(cred));
				resources.add(resource);
			}
			
			System.out.println(Tools.toJSON(resources));
			
		}
		
		
	}

}
