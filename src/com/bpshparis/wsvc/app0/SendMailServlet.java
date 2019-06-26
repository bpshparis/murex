package com.bpshparis.wsvc.app0;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
				
				Set<PosixFilePermission> perms = new HashSet<>();
			    perms.add(PosixFilePermission.OWNER_READ);
			    perms.add(PosixFilePermission.OWNER_WRITE);
			    perms.add(PosixFilePermission.OWNER_EXECUTE);
		
			    perms.add(PosixFilePermission.OTHERS_READ);
			    perms.add(PosixFilePermission.OTHERS_WRITE);
			    perms.add(PosixFilePermission.OTHERS_EXECUTE);
		
			    perms.add(PosixFilePermission.GROUP_READ);
			    perms.add(PosixFilePermission.GROUP_WRITE);
			    perms.add(PosixFilePermission.GROUP_EXECUTE);	

				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				Path mailIdPath = Paths.get(mailsPath + "/" + String.valueOf(mailId));
				if(Files.exists(mailIdPath)){
					FileUtils.deleteDirectory(mailIdPath.toFile());
				}
				if(mailIdPath.toFile().mkdir()) {
					Files.setPosixFilePermissions(mailIdPath, perms);
					Mail mail = new Mail();
					mail.setId(mailId);
					for (FileItem item : items) {
						if (!item.isFormField()) {
							// item is the file (and not a field)
							
								if(item.getName().equalsIgnoreCase("attachedImage.jpg")){
									
									Path imageFile = Paths.get(mailIdPath + "/" + item.getName());
									Files.copy(new BufferedInputStream(item.getInputStream()), imageFile, StandardCopyOption.REPLACE_EXISTING);
									Files.setPosixFilePermissions(imageFile, perms);
									datas.put("IMAGE", "OK: save in " +  imageFile);
									mail.setPicture(String.valueOf(mailId) + "/attachedImage.jpg");
		
								}
								if(item.getName().equalsIgnoreCase("attachedFace.jpg")){
									
									Path faceFile = Paths.get(mailIdPath + "/" + item.getName());
									Files.copy(new BufferedInputStream(item.getInputStream()), faceFile, StandardCopyOption.REPLACE_EXISTING);								
									Files.setPosixFilePermissions(faceFile, perms);
									datas.put("FACE", "OK: save in " +  faceFile);
									mail.setFace(String.valueOf(mailId) + "/attachedFace.jpg");
		
								}
							
						}
						if (item.isFormField() && item.getFieldName().equalsIgnoreCase("text")) {
							item.getFieldName();
				            String value = item.getString();
				            System.out.println("value=" + value);
				            value = URLEncoder.encode(value, "ISO-8859-1");
				            System.out.println("value=" + value);
				            value = URLDecoder.decode(value, "UTF-8");
				            System.out.println("value=" + value);
				            Map<String, Object> text = Tools.fromJSON(new ByteArrayInputStream(value.getBytes()));
				            mail.setSubject((String) text.get("subject"));
				            mail.setContent((String) text.get("body"));
				            mail.setLanguage((String) text.get("lang"));
				            datas.put("MAIL", mail);
						}
					}
					mails.add(mail);
					Files.write(mailsFile, Tools.toJSON(mails).getBytes());
					Files.setPosixFilePermissions(mailsFile, perms);

					
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
