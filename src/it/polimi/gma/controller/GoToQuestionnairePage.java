package it.polimi.gma.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.naming.InitialContext;
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

import it.polimi.gma.entities.Question;
import it.polimi.gma.entities.User;
import it.polimi.gma.services.AnswerService;
import it.polimi.gma.services.QuestionService;


@WebServlet("/Questionnaire")
public class GoToQuestionnairePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "it.polimi.gma.services/QuestionService")
	private QuestionService questionService;

	public GoToQuestionnairePage() {
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
		
		User user = (User) session.getAttribute("user");
		if (user.getRole().equals("Blocked")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Your account is blocked, you can no longer answer any questionnaire");
			return;
		}
		
		int questionnaire;
		List<Question> questions = new ArrayList<>();
		try {
			questionnaire = (int) session.getAttribute("questionnaireID");
			questions = questionService.findMarketingQuestions(questionnaire);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to get data");
			return;
		}
		
		AnswerService answerService = null;
		try {
			InitialContext ic = new InitialContext();
			answerService = (AnswerService) ic.lookup("java:/openejb/local/AnswerServiceLocalBean");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		answerService.setQuestions(questions);
		request.getSession().setAttribute("AnswerService", answerService);
		
		String path = "/WEB-INF/questionnaire.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("questions", questions);
		ctx.setVariable("questionnaire", "Questionnaire: "+questionnaire);
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {}
}
