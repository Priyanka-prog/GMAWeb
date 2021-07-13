package it.polimi.gma.controller;

import java.io.IOException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.gma.entities.User;
import it.polimi.gma.services.ProductService;
import it.polimi.gma.services.QuestionnaireCreationService;
import it.polimi.gma.services.QuestionnaireService;

@WebServlet("/CreateQuestionnaire")
public class CreateQuestionnaire extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "it.polimi.gma.services/QuestionnaireService")
	private QuestionnaireService questionnaireService;
	
	@EJB(name = "it.polimi.gma.services/ProductService")
	private ProductService productService;

	
	public CreateQuestionnaire() {
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
		
		int numberOfQuestions;
		String questionnaireDate = null;
		try {
			numberOfQuestions = Integer.parseInt(request.getParameter("numberOfQuestions"));
			questionnaireDate = StringEscapeUtils.escapeJava(request.getParameter("questionnaireDate"));
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(System.currentTimeMillis());
		String system = formatter.format(date);

        try {
            Date qDate = (Date) new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN)
                    .parse(questionnaireDate);
            Date systemDate = (Date) new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN)
                    .parse(system);

            if (qDate.compareTo(systemDate) < 0) {
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can only create questionnaires for a current or a future date");
    			return;
            } 
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
		QuestionnaireCreationService qcs = (QuestionnaireCreationService) session.getAttribute("QuestionnaireCreationService");
		qcs.addQuestionnaireDate(questionnaireDate);
		
		String[] qwe = new String[numberOfQuestions];
		
		String path = "/WEB-INF/addQuestions.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("numberOfQuestions", qwe);

		templateEngine.process(path, ctx, response.getWriter());
	}

	public void destroy() {}
}
