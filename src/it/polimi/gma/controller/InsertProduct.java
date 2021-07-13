package it.polimi.gma.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.gma.entities.User;
import it.polimi.gma.services.ProductService;
import it.polimi.gma.services.QuestionnaireCreationService;
import it.polimi.gma.utils.*;

@WebServlet("/InsertProduct")
@MultipartConfig
public class InsertProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "it.polimi.gma.services/ProductService")
	private ProductService productService;

	
	public InsertProduct() {
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

		String productName = null;
		byte[] imgByteArray = null;
		try {
			productName = StringEscapeUtils.escapeJava(request.getParameter("productName"));
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		Part imgFile = request.getPart("imageUpload");
		InputStream imgContent = imgFile.getInputStream();
		imgByteArray = ImageUtils.readImage(imgContent);
		if (imgByteArray.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid photo parameters");
		}

		try {
			QuestionnaireCreationService qcs = (QuestionnaireCreationService) session.getAttribute("QuestionnaireCreationService");
			qcs.addProduct(productName, imgByteArray);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create mission");
			return;
		}
		
		String path = "/WEB-INF/questionnaireInfo.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

	public void destroy() {}
}
