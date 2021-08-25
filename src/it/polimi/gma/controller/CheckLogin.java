package it.polimi.gma.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
	
import org.apache.commons.lang.StringEscapeUtils;
import javax.ejb.EJB;
import javax.persistence.NonUniqueResultException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.gma.entities.User;
import it.polimi.gma.exceptions.CredentialsException;
import it.polimi.gma.services.UserService;


@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@EJB(name = "it.polimi.gma.services/UserService")
	private UserService usrService;
	
    public CheckLogin() {
        super();
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String usr = null;
		String pwd = null;
		
		try {
			usr = StringEscapeUtils.escapeJava(request.getParameter("username"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			if (usr == null || pwd == null || usr.isEmpty() || pwd.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}

		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
		}
		
		User user;
		try {
			user = usrService.checkCredentials(usr, pwd);
		} catch (CredentialsException | NonUniqueResultException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not check credentials");
			return;	
		}
		
		String path;
		if (user == null) {
			PrintWriter out = response.getWriter();
			out.println("<p>Incorrect username or password</p>");
		} 
		else {
			request.getSession().setAttribute("user", user);
			Date date = new Date(); 
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			user.setLastLogin(formatter.format(date));
			try {
				usrService.updateUser(user);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Timestamp update error");
				return;
			}
			if (user.getRole().equals("Admin")) {
				path = getServletContext().getContextPath() + "/AdminPage";
			}
			else if (user.getRole().equals("ActiveUser")){
				path = getServletContext().getContextPath() + "/Home";
			}
			else{
				path= getServletContext().getContextPath() +"/Blocked";
			}
			response.sendRedirect(path);
		}
	}
	
	public void destroy() {}
}
