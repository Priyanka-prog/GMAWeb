package it.polimi.gma.controller;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.gma.entities.User;
import it.polimi.gma.exceptions.CredentialsException;
import it.polimi.gma.services.UserService;


@WebServlet("/Subscribe")
public class Subscribe extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@EJB(name = "it.polimi.db2.services/UserService")
	private UserService userService;
	
    public Subscribe() {
        super();
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = null;
		String passwd = null;
		String email = null;
		
		try {
			username = request.getParameter("username");
			passwd = request.getParameter("passwd");
			email = request.getParameter("email");
			if (username == null || passwd == null || email == null) {
				throw new CredentialsException("Missing or empty credential value");
			}

		} catch (CredentialsException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
		}
		
		User user = userService.createUser(username, passwd, email);
		
		request.getSession().setAttribute("user", user);
		String path = getServletContext().getContextPath() + "/Home";
		response.sendRedirect(path);
	}
	
	public void destroy() {}
}
