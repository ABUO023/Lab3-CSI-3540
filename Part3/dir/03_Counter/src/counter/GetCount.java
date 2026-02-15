package counter;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class GetCount extends HttpServlet {

    private int count = 0;

    public void doGet( HttpServletRequest requete, HttpServletResponse reponse ) 
	throws ServletException, IOException {
	
	// initialisation

	reponse.setContentType( "text/html; charset=\"UTF-8\"" );
	PrintWriter doc = reponse.getWriter();

	// traitement(s)
	count++;


	doc.println( "<!DOCTYPE html " );
	doc.println( "	  PUBLIC \"-//W3C//DTD XHTML Basic 1.0//EN\" " );
	doc.println( " 	  \"http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd\">" );
	doc.println( "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"fr-CA\">" );
	doc.println( "  <head>" );
	doc.println( "    <title>CSI3540. Structures, techniques et normes du Web</title>" );
	doc.println( "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" );
	doc.println( "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/default.css\" media=\"all\" />" );
	doc.println( "  </head>" );
	doc.println( "  <body>" );
	doc.println( "    <p>" );
	doc.println( "      <img src=\"images/uOttawa.png\" alt=\"uOttawa logo\" width=\"221\" height=\"100\"/>" );
	doc.println( "    </p>" );
	doc.println( "    <div class=\"entete\">" );
	doc.println( "      Nombre de visites :" );
	doc.println( "    </div>" );
	doc.println( "    <p>" );
	doc.println( "      " + count );
	doc.println( "    </p>" );
	doc.println( "    <div class=\"entete\">" );
	doc.println( "      <a href=\"index.html\">Citation du jour!</a>" );
	doc.println( "    </div>" );
	doc.println( "  </body>" );
	doc.println( "</html>" );

	// post-traitement(s)

	doc.close();
    }


}
