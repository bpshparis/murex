package com.bpshparis.wsvc.app0;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

/**
 * Servlet implementation class AppendSelectionsServlet
 */
@WebServlet(name = "SendMail", urlPatterns = { "/SendMail" })
public class SendMailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendMailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		List<Mail> mails = new ArrayList<Mail>();
		Map<String, Object> datas = new HashMap<String, Object>();

		try {

			Map<String, Object> reqParms = new HashMap<String, Object>();
			
			datas.put("FROM", this.getServletName());
			String mailsPath = getServletContext().getRealPath("/res/mails");
			
			// Let see if mails.json exists in /res/mails and load it
			Path mailsFile = Paths.get(mailsPath + "/mails.json");
			if(Files.exists(mailsFile)){
				InputStream is = new ByteArrayInputStream(Files.readAllBytes(mailsFile));
				mails = Tools.MailsListFromJSON(is);
			}

			int mailId = mails.size();
			
			if(ServletFileUpload.isMultipartContent(request)){
				
				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				Path mailIdPath = Paths.get(mailsPath + "/" + String.valueOf(mailId));
				if(Files.exists(mailIdPath)){
					FileUtils.deleteDirectory(mailIdPath.toFile());
				}
				if(mailIdPath.toFile().mkdir()) {
					mailIdPath.toFile().setExecutable(true);
					mailIdPath.toFile().setWritable(true);
					mailIdPath.toFile().setReadable(true);
					
					

					Mail mail = new Mail();
					mail.setId(mailId);
					for (FileItem item : items) {
						if (!item.isFormField()) {
							// item is the file (and not a field)
							
								if(item.getName().equalsIgnoreCase("attachedImage.jpg")){
									
									Path imageFile = Paths.get(mailIdPath + "/" + item.getName());
									Files.copy(new BufferedInputStream(item.getInputStream()), imageFile, StandardCopyOption.REPLACE_EXISTING);
									imageFile.toFile().setReadable(true);
									datas.put("IMAGE", "OK: save in " +  imageFile);
									mail.setPicture(String.valueOf(mailId) + "/attachedImage.jpg");
		
								}
								if(item.getName().equalsIgnoreCase("attachedFace.jpg")){
									
									Path faceFile = Paths.get(mailIdPath + "/" + item.getName());
									Files.copy(new BufferedInputStream(item.getInputStream()), faceFile, StandardCopyOption.REPLACE_EXISTING);								
									faceFile.toFile().setReadable(true);
									datas.put("FACE", "OK: save in " +  faceFile);
									mail.setFace(String.valueOf(mailId) + "/attachedFace.jpg");
		
								}
							
						}
						if (item.isFormField() && item.getFieldName().equalsIgnoreCase("text")) {
							item.getFieldName();
				            String value = item.getString();
//				            System.out.println("value=" + value);
//				            value = URLEncoder.encode(value, "UTF-8");
//				            System.out.println("value=" + value);
//				            value = URLDecoder.decode(value, "ISO-8859-1");
//				            System.out.println("value=" + value);
				            Map<String, Object> text = Tools.fromJSON(new ByteArrayInputStream(value.getBytes("UTF-8")));
				            mail.setSubject((String) text.get("subject"));
				            mail.setContent((String) text.get("body"));
				            mail.setLanguage((String) text.get("lang"));
				            datas.put("MAIL", mail);
						}
					}
					mails.add(mail);
					Files.write(mailsFile, Tools.toJSON(mails).getBytes("UTF-8"));
//					Files.setPosixFilePermissions(mailsFile, perms);
					mailsFile.toFile().setReadable(true);
					mailsFile.toFile().setWritable(true);
					mailsFile.toFile().setExecutable(true);

					
				}
			}
			
			else {
				reqParms = Tools.fromJSON(request.getInputStream());
				datas.put("REQ_PARMS", reqParms);

				
				if(reqParms.containsKey("mailCount")) {
					int mailCount = (int) reqParms.get("mailCount");

					Mail mail = mails.get(mailCount -1);
					
					mail.setContent((String) reqParms.get("body"));
					mail.setSubject((String) reqParms.get("subject"));
					mail.setLanguage((String) reqParms.get("lang"));
		            datas.put("MAIL", mail);
//					mails.add(mail);
					Files.write(mailsFile, Tools.toJSON(mails).getBytes("UTF-8"));
					mailsFile.toFile().setReadable(true);
					mailsFile.toFile().setWritable(true);
					mailsFile.toFile().setExecutable(true);
				}
				
			}
			
			datas.put("STATUS", "OK");
			datas.put("MAILCOUNT", mails.size());
				
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

}
