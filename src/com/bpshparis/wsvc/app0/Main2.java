package com.bpshparis.wsvc.app0;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main2 {
	
	public static void main(String[] args) {
		
		try {
			
			Mail aMail = new Mail();
			aMail.setSubject("çà paraît pénible.");
			System.out.println(Tools.toJSON(aMail));
			
			Path mailsFile = Paths.get("/opt/wks/simplon/WebContent/res/mails/mails.json");
			if(Files.exists(mailsFile)){
				InputStream is = new ByteArrayInputStream(Files.readAllBytes(mailsFile));
				List<Mail> mails = Tools.MailsListFromJSON(is);
				System.out.println(mails.get(0).getSubject());
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
}
