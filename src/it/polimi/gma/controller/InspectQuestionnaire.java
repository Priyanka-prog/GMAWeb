package it.polimi.gma.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.gma.entities.Answer;
import it.polimi.gma.entities.User;
import it.polimi.gma.services.LeaderboardService;
import it.polimi.gma.services.QuestionnaireService;
import it.polimi.gma.services.UserService;


@WebServlet("/InspectQuestionnaire")
public class InspectQuestionnaire extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "it.polimi.gma.services/QuestionnaireService")
	private QuestionnaireService questionnaireService;
	
	@EJB(name = "it.polimi.gma.services/UserService")
	private UserService userService;
	
	@EJB(name = "it.polimi.gma.services/LeaderboardService")
	private LeaderboardService leaderboardService;

	
	public InspectQuestionnaire() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String loginpath = getServletContext().getContextPath() + "/index.html";
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		if ((!((User) session.getAttribute("user")).getRole().equals("Admin"))) {
			response.sendRedirect(loginpath);
			return;
		}
		
		int ID_questionnaire;
		try {
			ID_questionnaire = Integer.parseInt(request.getParameter("questionnaireID"));
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		// List of users whose submitted the questionnaire
		List<User> users = new ArrayList<>();
		try {
			users = leaderboardService.getUsers(ID_questionnaire);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		
		// List of users whose canceled the questionnaire
		List<User> us = new ArrayList<>();
		try {
			us = leaderboardService.getUsersCancelled(ID_questionnaire);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		
		// Questionnaire answers of each user
		List<List<Answer>> answers = new ArrayList<>();
		for (User u : users) {
			answers.add(userService.getAnswers(u, ID_questionnaire));
		}
		
		
		String path = "/WEB-INF/questionnaireView.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("users", users);
		ctx.setVariable("answers", answers);
		ctx.setVariable("usersCanc", us);

		templateEngine.process(path, ctx, response.getWriter());
	}

	public void destroy() {}
}
