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

import it.polimi.gma.entities.*;
import it.polimi.gma.services.*;


@WebServlet("/Home")
public class HomePageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "it.polimi.gma.services/QuestionnaireService")
	private QuestionnaireService questionnaireService;
	
	@EJB(name = "it.polimi.gma.services/ProductService")
	private ProductService productService;
	
	@EJB(name = "it.polimi.gma.services/LeaderboardService")
	private LeaderboardService leaderboardService;
	
	@EJB(name = "it.polimi.gma.services/UserService")
	private UserService usrService;
	
	public HomePageController() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		List<User> users = new ArrayList<>();
		Questionnaire questionnaire = null;
		Product product = null;
		try {
			String date = new java.sql.Date(System.currentTimeMillis()).toString(); 
			questionnaire = questionnaireService.findDailyQuestionnaire(date);
			product = productService.getProduct(questionnaire.getProduct().getPname());
			users = leaderboardService.getUsers(questionnaire.getID());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to get data");
			return;
		}
        
		List<List<Answer>> answers = new ArrayList<>();
		for (User u : users) {
			answers.add(usrService.getAnswers(u, questionnaire.getID()));
		}
		
		session.setAttribute("questionnaireID", questionnaire.getID());
		
		String path = "/WEB-INF/home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("product", product);
		ctx.setVariable("questionnaire", questionnaire.getID());
		ctx.setVariable("reviews", answers);

		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {}
}
