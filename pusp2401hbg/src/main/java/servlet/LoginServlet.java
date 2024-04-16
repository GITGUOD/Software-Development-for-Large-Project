package servlet;

import beans.UserBean;
import formgenerator.FormGenerator;
import formgenerator.FormGeneratorAdministration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;

import java.io.*;
import java.sql.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Declaring states representing different clearance levels
    private static final int USER_MAINSCREEN_STATE = 1;
    private static final int PL_MAINSCREEN_STATE = 2;
    private static final int ADMIN_MAINSCREEN_STATE = 3;

    // User is signed out if clearance level is -1. Initial state
    private static int clearanceLevel = -1;

    private DatabaseHandler db = new DatabaseHandler();
    private FormGenerator formGenerator = new FormGenerator();

    public LoginServlet() {
    }

    /*
     * @param request, response
     * 
     * @throws ServletException, IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtains the current session
        HttpSession session = request.getSession(true);
        // FormGeneratorAdministration FGA = new FormGeneratorAdministration();
        // The attribute state is null if there's no ongoing session
        // if (session.getAttribute("state") == null) {
        // Declares variables to store current state and entered username and password
        int state = -1;
        String username;
        String password;

        boolean sessionShouldBeEnded = false;

        // Used to add HTML code to the response
        PrintWriter out = response.getWriter();

        // Beginning of HTML code
        /*
         * out.println("<html>");
         * out.println(FGA.headLayout("ePuss+"));
         * out.println("<body>");
         * out.println("<h1>ePuss+</h1>");
         */

        // check if it is post or get
        String httpMethod = request.getMethod();
        if ("GET".equals(httpMethod)) { // if the method is get and already login doesnt need to log in again
            UserBean user = (UserBean) session.getAttribute("user");
            if (user != null) {
                // Updates the session's state and redirects the user to the appropriate servlet
                switch (user.getClearanceLevel()) {
                    case 3:
                        response.sendRedirect("BasicUserMainScreenServlet");
                        break;
                    case 2:
                        response.sendRedirect("PLMainScreenServlet");
                        break;
                    case 1:
                        response.sendRedirect("AdminMainScreenServlet");
                        break;
                    // Unsuccessful login. Displays error message
                    default:
                        out.println(formGenerator.getLoginForm("Please try to sign in"));
                }
            }
            // HTML code for displaying login form
            out.println(formGenerator.getLoginForm(""));
        } else{ // if the client did a post 
            // Retrieves the entered username and password
            username = request.getParameter("username");
            password = request.getParameter("password");
    
            // Performs user authentication if username and password have been entered
            if (username != null && password != null) {
                try {
                    clearanceLevel = -1;
                    // authenticateUser(username, password) : int returns -1 if authentication fails
                    int userID = db.authenticateUser(username, password);
    
                    // Login is successful if -1 wasn't returned
                    if (userID != -1) {
                        // Updates attributes about the user for this session
                        session.setAttribute("name", username);
                        session.setAttribute("userID", userID);
    
                        session.setAttribute("user",
                                new UserBean(userID, username, "", db.selectClearanceLevel(username)));
                        // Queries the database to get the user's clearance level
                        clearanceLevel = db.selectClearanceLevel(username);
                    }
                }
                // Prints stack trace if an error occurs related to the database
                catch (SQLException e) {
                    e.printStackTrace();
                }
    
                // Updates the session's state and redirects the user to the appropriate servlet
                switch (clearanceLevel) {
                    case 3:
                        state = USER_MAINSCREEN_STATE;
                        session.setAttribute("state", state);
                        response.sendRedirect("BasicUserMainScreenServlet");
                        break;
                    case 2:
                        state = PL_MAINSCREEN_STATE;
                        session.setAttribute("state", state);
                        response.sendRedirect("PLMainScreenServlet");
                        break;
                    case 1:
                        state = ADMIN_MAINSCREEN_STATE;
                        session.setAttribute("state", state);
                        response.sendRedirect("AdminMainScreenServlet");
                        break;
                    // Unsuccessful login. Displays error message
                    default:
                        // out.println(
                        // "Incorrect username and/or password. Please try again.");
                        out.println(formGenerator.getLoginForm("Incorrect username and/or password. Please try again."));
                }
            }

        }


        /*
         * // Final closing tags for the HTML code
         * out.println("</body>");
         * out.println("</html>");
         */

        if (sessionShouldBeEnded) {
            session.invalidate();
        }
        // }
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