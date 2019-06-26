package com.bpshparis.wsvc.app0;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class AppendSelectionsServlet
 */
@WebServlet(name = "GetMails", urlPatterns = { "/GetMails" })
public class GetMailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMailsServlet() {
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

			request.getServletContext().setAttribute("mails", mails);
			datas.put("STATUS", "OK");
			datas.put("MAILCOUNT", mails.size());
			datas.put("MAILS", mails);
				
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
