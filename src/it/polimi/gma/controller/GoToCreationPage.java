package it.polimi.gma.controller;

import java.io.IOException;

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

import it.polimi.gma.entities.User;
import it.polimi.gma.services.QuestionnaireCreationService;


@WebServlet("/CreationPage")
public class GoToCreationPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	public GoToCreationPage() {
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
		if ((!((User) session.getAttribute("user")).getRole().equals("Admin"))) {
			response.sendRedirect(loginpath);
			return;
		}
		
		QuestionnaireCreationService questionnaireCreationService = null;
		try {
			InitialContext ic = new InitialContext();
			questionnaireCreationService = (QuestionnaireCreationService) ic.lookup("java:/openejb/local/QuestionnaireCreationServiceLocalBean");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		request.getSession().setAttribute("QuestionnaireCreationService", questionnaireCreationService);
		
		
		String path = "/WEB-INF/creationPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
