package servlet;

import formgenerator.FormGenerator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import formgenerator.FormGeneratorAdministration;
import beans.UserBean;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;

import java.io.*;

@WebServlet("/PLMainScreenServlet")
public class PLMainScreenServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Declaring states representing different clearance levels
   
    private FormGenerator formGenerator = new FormGenerator();
    private FormGeneratorAdministration fga = new FormGeneratorAdministration();

    public PLMainScreenServlet() {
    }

    /*
     * @param request, response
     * 
     * @throws ServletException, IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Used to add HTML code to the response
        PrintWriter out = response.getWriter();
        // Obtains the current session
        HttpSession session = request.getSession(true);
        // Obtains the instance of UserBean representing the user in the current session
        UserBean user = (UserBean) session.getAttribute("user");
        // Obtains action attribute. This represents the button the user clicks
        String action = request.getParameter("action");

        // Redirects to LoginServlet if there is no ongoing session
        if (user == null) {
            response.sendRedirect("/LoginServlet");
            return; // Add this return statement to stop further execution
        }


        /*
         * The clearance level for the user associated with this session corresponds to
         * the current servlet
         */
        else {
            boolean sessionShouldBeEnded = false;

            // Beginning of HTML code
            out.println("<html>");
            out.println(fga.headLayout("LeaderUser"));
            out.println("<head><title>ePuss+</title></head>");
            out.print(fga.cssStyleForPage());
            out.println("<body>");


            int clear = user.getClearanceLevel();
            if(clear == 3){
                out.println(fga.createToolbar("BasicUserMainScreenServlet"));
            }else if(clear == 2){

                out.println(fga.createToolbar("PLMainScreenServlet"));
            }else if (clear == 1){
                out.println(fga.createToolbar("AdminMainScreenServlet"));
            }
            out.println("<h1>You are signed in as " + user.getUsername() + "</h1>");

            out.println(formGenerator.getPlMainScreenForm());

            // If we press the time button, we redirect to TimeReportServlet
            if ("Time".equals(action)) {
                response.sendRedirect("TimeReportServlet");
            }
            // If we press the project groups button, we redirect to ProjectGroupServlet
            else if ("Project groups".equals(action)) {
                response.sendRedirect("ProjectGroupServlet");
            }
            // If we press the statistics button, we redirect to StatisticsServlet
            else if ("Statistics".equals(action)) {
                response.sendRedirect("StatisticsServlet");
            }
            // If we press sign out, we redirect to LoginServlet
            else if ("Sign out".equals(action)) {
                session.invalidate();
                response.sendRedirect("LoginServlet");

            }
            // Final closing tags for the HTML code
            out.println("</body>");
            out.println("</html>");
            
            if (sessionShouldBeEnded) {
                session.invalidate();
            }
        }
    }

    /*
     * @param request, response
     * 
     * @throws ServletException, IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}