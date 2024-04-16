package formgenerator;

import java.sql.SQLException;
import java.util.List;
import beans.ProjectBean;
import beans.TimeReportBean;
import beans.UserBean;
import servlet.DatabaseHandler;

public class FormGeneratorAdministration {

    /**
     * Creates a form element with the given parameter.
     *
     * @param par the parameter value
     * @return the formatted form element
     */
    private String formElement(String par) {
        return '"' + par + '"';
    }

    /**
     * Generates the HTML head layout for a web page.
     * 
     * @param pageTitle the title of the page
     * @return the HTML head layout
     */
    public String headLayout(String pageTitle) {
        String htmlBuilder = "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + pageTitle + "</title>\n" +
                "    <link rel=\"stylesheet\" href=\"/css/pure-min.css\">\n" +
                "</head>";
        return htmlBuilder;
    }

    /**
     * Creates a toolbar HTML for navigation.
     *
     * @param ServletName the name of the servlet
     * @return the HTML code for the toolbar
     */
    public String createToolbar(String ServletName) {
        String html = "<div class=\"toolbar\">\n" +
                "  <ul>\n" +
                "    <li><a href=\"/" + ServletName + "\">Back to Home</a></li>\n" +
                "  </ul>\n" +
                "</div>\n";
        return html;
    }

    /*
     * Generates the top-level form element for the admin interface.
     * 
     * @return the top-level form element
     */
    public String topLevelAdminPage() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\">What would you like to admin today?</h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"get\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"Manage Users\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"Manage Project Groups\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"Manage Roles\">");
        htmlBuilder.append("</form>");
        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for managing users.
     * 
     * @return the HTML for managing users
     */
    public String manageUsersAdminPage() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\"> User Management<h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"get\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"New User\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"Remove user\">");
        htmlBuilder.append("</form>");
        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for managing project groups.
     * 
     * @return the HTML for managing project groups
     */
    public String projectGroupAdminPage() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\"> Project Group Management<h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"get\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"New Project\">");
        htmlBuilder.append("<p> <input type=\"submit\" name=\"action\" value=\"Archive Project\">");
        htmlBuilder.append("</form>");
        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for creating a new user.
     * 
     * @return the HTML for creating a new user
     */
    public String newUserPage() {
        String htmlBuilder = "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\" >New User</h1>"
                + "<form action=\"AdministrationServlet\" method=\"post\">"

                + "<label for=\"username\">Username:</label>"
                + "<input type=\"text\" id=\"username\" name=\"username\"><br>"

                + "<label for=\"email\">Email:</label>"
                + "<input type=\"text\" id=\"email\" name=\"email\"><br>"

                + "<label for=\"clearanceLevel\">Clearance Level:</label>"
                + "<select id=\"clearanceLevel\" name=\"clearancelevel\">"
                + "<option value=\"1\">Admin</option>"
                + "<option value=\"2\">Project Leader</option>"
                + "<option value=\"3\">User</option>"
                + "</select><br>"

                + "<input type=\"submit\" name=\"action\" value=\"Add User\">"
                + "</form>";
        return htmlBuilder;
    }

    /**
     * Generates the HTML for removing a user.
     * 
     * @return the HTML for removing a user
     */
    public String removeUserPage() {
        StringBuilder htmlBuilder = new StringBuilder();
        List<UserBean> userList;
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\">Select users from the list and click remove</h1>");
        htmlBuilder.append("<form action=\"/AdministrationServlet\" method=\"post\">"); // Formulär för att skicka data
                                                                                        // till
        // "/removeUser" när användaren trycker på
        // knappen
        htmlBuilder.append("<table border=\"1\">");
        htmlBuilder.append("<thead>");
        htmlBuilder.append("<tr>");
        htmlBuilder.append("<th></th>");
        htmlBuilder.append("<th>Username</th>");
        htmlBuilder.append("</tr>");
        htmlBuilder.append("</thead>");
        htmlBuilder.append("<tbody>");
        try (DatabaseHandler dbHandler = new DatabaseHandler()) {
            userList = dbHandler.listAllUsers();

            for (UserBean user : userList) {
                htmlBuilder.append("<tr><td>");
                htmlBuilder.append(
                        "<input type=\"radio\" name=\"selectedUsers\" value=\"" + user.getUsername() + "\">");
                htmlBuilder.append("</td><td>");
                htmlBuilder.append(user.getUsername());
                htmlBuilder.append("</td></tr>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlBuilder.append("</tbody>");
        htmlBuilder.append("</table>");
        htmlBuilder.append("<input type=\"submit\" name=\"action\" value=\"Remove\">"); // Knapp för att ta bort
                                                                                        // användaren
        htmlBuilder.append("</form>");

        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for adding a new role.
     * 
     * @return the HTML for adding a new role
     */
    public String addNewRolePage() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\">Create a new role</h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"post\">");
        htmlBuilder.append("<label for=\"newRole\">New Role:</label>");
        htmlBuilder.append("<input type=\"text\" id=\"newRole\" name=\"newRole\"><br>");
        htmlBuilder.append("<input type=\"hidden\" name=\"action\" value=\"AddRole\">"); // Add hidden input for the
                                                                                         // action
        htmlBuilder.append("<input type=\"submit\" value=\"Add new role\">");
        
        htmlBuilder.append("</form>");
        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for showing all roles.
     * 
     * @return the HTML for showing all roles
     */
    public String showAllRoles() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h2 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\">These are the currently existing roles: </h2>");
    
        try (DatabaseHandler db = new DatabaseHandler()) {
            List<String> roles = db.listRoles();
    
            // Check if roles list is not empty
            if (!roles.isEmpty()) {
                htmlBuilder.append("<form method=\"get\">");
                for (String role : roles) {
                    htmlBuilder.append("<input type=\"radio\" name=\"roles\" value=\"").append(role).append("\">")
                            .append(role).append("<br>");
                }
                htmlBuilder.append("<input type=\"submit\" name=\"action\" value=\"Edit role\">");
                htmlBuilder.append("<input type=\"submit\" name=\"action\" value=\"Remove Role\">"); // Knapp för att ta bort
                htmlBuilder.append("</form>");
            } else {
                htmlBuilder.append("<p>No roles found.</p>");
            }
            // this style special for this part
            String cssStyleSpecial = "<style>\r\n" + //
                    "    /* Style for the heading */\r\n" + //
                    "    h2 {\r\n" + //
                    "        font-size: 24px;\r\n" + //
                    "        color: #333;\r\n" + //
                    "        margin-top: 20px;\r\n" + //
                    "        margin-left: 40%;\r\n" + //
                    "    }\r\n" + //
                    "\r\n" + //
                    "    /* Style for the list */\r\n" + //
                    "    #listan {\r\n" + //
                    "        width: 40%;" +
                    "        list-style-type: none;\r\n" + //
                    "        margin: 0;\r\n" + //
                    "        padding: 0;\r\n" + //
                    "        margin-left: 30%;\r\n" + //
                    "    }\r\n" + //
                    "\r\n" + //
                    "    /* Style for list items */\r\n" + //
                    "    li {\r\n" + //
                    "        background-color: #007bff;\r\n" + //
                    "        color: white;\r\n" + //
                    "        padding: 10px;\r\n" + //
                    "        margin-bottom: 5px;\r\n" + //
                    "        border-radius: 5px;\r\n" + //
                    "        cursor: pointer;\r\n" + //
                    "    }\r\n" + //
                    "\r\n" + //
                    "    /* Hover effect for list items */\r\n" + //
                    "    li:hover {\r\n" + //
                    "        background-color: #0056b3;\r\n" + //
                    "    }\r\n" + //
                    "</style>";
            htmlBuilder.append(cssStyleSpecial);
        } catch (Exception e) {
            // Handle exceptions here
            htmlBuilder.append("<p>Error occurred while retrieving roles: ").append(e.getMessage()).append("</p>");
        }
        return htmlBuilder.toString();
    }
    

    /**
     * Generates the HTML for creating a new project group.
     * 
     * @return the HTML for creating a new project group
     */
    public String newProjectGroupPage() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\" >New Project Group</h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"post\">");
        htmlBuilder.append("<label for=\"groupname\">Group name:</label>");
        htmlBuilder.append("<input type=\"text\" id=\"groupname\" name=\"groupname\"><br>");
        htmlBuilder.append("<label for=\"groupleader\">Group Leader:</label>");
        htmlBuilder.append("<select id=\"groupleader\" name=\"groupleader\">");
        try (DatabaseHandler db = new DatabaseHandler()) {
            List<UserBean> list = db.listAllUsers();
            for (UserBean ub : list) {
                htmlBuilder.append("<option value=\"" + ub.getUsername() + "\">" + ub.getUsername() + "</option>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Add more options as needed
        htmlBuilder.append("</select><br>");
        htmlBuilder.append("<input type=\"submit\" name=\"action\" value=\"AddGroup\">");
        htmlBuilder.append("</form>");
        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for archiving a project group.
     * 
     * @return the HTML for archiving a project group
     */
    public String archiveProjectGroupPage() {
        StringBuilder htmlBuilder = new StringBuilder();
        DatabaseHandler dbHandler = new DatabaseHandler();
        List<ProjectBean> projectList = dbHandler.listAllProjects();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\">Select a project from the list and click archive</h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"post\">"); // Formulär för att skicka data
                                                                                       // till "/archiveUser" när
                                                                                       // användaren trycker på knappen
        htmlBuilder.append("<table border=\"1\">");
        htmlBuilder.append("<thead>");
        htmlBuilder.append("<tr>");
        htmlBuilder.append("<th></th>");
        htmlBuilder.append("<th>Projects</th>");
        htmlBuilder.append("</tr>");
        htmlBuilder.append("</thead>");
        htmlBuilder.append("<tbody>");

        for (ProjectBean project : projectList) {
            htmlBuilder.append("<tr><td>");
            htmlBuilder
                    .append("<input type=\"radio\" name=\"selectedGroup\" value=\"" + project.getProjectName() + "\">"); // Kryssruta
                                                                                                                         // för
                                                                                                                         // att
                                                                                                                         // välja
                                                                                                                         // användaren
            htmlBuilder.append("</td><td>");
            if (project.getIsProjectArchived()) {
                htmlBuilder.append("(Archived)");
            }
            htmlBuilder.append(project.getProjectName());
            htmlBuilder.append("</td></tr>");
        }
        try {
            dbHandler.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlBuilder.append("</tbody>");
        htmlBuilder.append("</table>");
        htmlBuilder.append("<input type=\"submit\" name=\"action\"value=\"Archive project\">"); // Knapp för att ta bort
                                                                                                // användaren
        htmlBuilder.append("</form>");

        return htmlBuilder.toString();
    }

    /**
     * Generates the HTML for showing all time reports.
     * 
     * @return the HTML for showing all time reports
     */
    public String showAllTimeReports() {
        StringBuilder htmlBuilder = new StringBuilder();
        DatabaseHandler dbHandler = new DatabaseHandler();
        List<TimeReportBean> timeReportList = dbHandler.listAllTimeReports();
        try {
            dbHandler.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlBuilder.append("<table border=\"1\">");
        htmlBuilder.append("<thead>");
        htmlBuilder.append("<tr>");
        htmlBuilder.append("<th>User ID</th>");
        htmlBuilder.append("<th>Project ID</th>");
        htmlBuilder.append("<th>Is Signed</th>");
        htmlBuilder.append("<th>Start Time</th>");
        htmlBuilder.append("<th>Stop Time</th>");
        htmlBuilder.append("<th>Activity</th>");
        htmlBuilder.append("</tr>");
        htmlBuilder.append("</thead>");
        htmlBuilder.append("<tbody>");

        for (TimeReportBean timeReport : timeReportList) {
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(timeReport.getEmployee().getUserID()).append("</td>");
            htmlBuilder.append("<td>").append(timeReport.getProjectID()).append("</td>");
            htmlBuilder.append("<td>").append(timeReport.getIsSigned()).append("</td>");
            htmlBuilder.append("<td>").append(timeReport.getStartTime()).append("</td>");
            htmlBuilder.append("<td>").append(timeReport.getStopTime()).append("</td>");
            htmlBuilder.append("<td>").append(timeReport.getActivity()).append("</td>");
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</tbody>");
        htmlBuilder.append("</table>");

        return htmlBuilder.toString();
    }

    public String cssStyleForPage() {
        return "<style> body {\r\n" + //
                "    font-family: Arial, sans-serif;\r\n" + //
                "    margin: 0;\r\n" + //
                "    padding: 0;\r\n" + //
                "    background-color: #f4f4f4;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                ".toolbar {\r\n" + //
                "    background-color: #007bff;\r\n" + //
                "    padding: 10px;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                ".toolbar ul {\r\n" + //
                "    list-style-type: none;\r\n" + //
                "    margin: 0;\r\n" + //
                "    paddinroleSelectedg: 0;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                ".toolbar ul li {\r\n" + //
                "    display: inline;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                ".toolbar ul li a {\r\n" + //
                "    color: white;\r\n" + //
                "    text-decoration: none;\r\n" + //
                "    padding: 10px;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                ".toolbar ul li a:hover {\r\n" + //
                "    background-color: #0056b3;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "h1 {\r\n" + //
                "    margin: 20px 0;\r\n" + //
                "    color: #007bff;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "form {\r\n" + //
                "    margin: 20px;\r\n" + //
                "}\r\n" + //finger
                "\r\n" + //
                "input[type=\"submit\"] {\r\n" + //
                "    padding: 10px 20px;\r\n" + //
                "    background-color: #007bff;\r\n" + //
                "    color: white;\r\n" + //
                "    border: none;\r\n" + //
                "    border-radius: 5px;\r\n" + //
                "    cursor: pointer;\r\n" + //
                "    transition: background-color 0.3s ease;\r\n" + //
                "    margin-top: 10px;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "input[type=\"submit\"]:hover {\r\n" + //
                "    background-color: #0056b3;\r\n" + //
                "}" +
                "h1 {\r\n" + //
                "    margin: 20px 0;\r\n" + //
                "    color: #007bff;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "form {\r\n" + //
                "    margin: 20px;\r\n" + //
                "    padding: 20px;\r\n" + //
                "    background-color: #ffffff;\r\n" + //
                "    border-radius: 5px;\r\n" + //
                "    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "label {\r\n" + //
                "    display: block;\r\n" + //
                "    margin-bottom: 10px;\r\n" + //
                "    font-weight: bold;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "input[type=\"text\"],\r\n" + //
                "select {\r\n" + //
                "    width: 100%;\r\n" + //
                "    padding: 10px;\r\n" + //
                "    margin-bottom: 20px;\r\n" + //
                "    border: 1px solid #ddd;\r\n" + //
                "    border-radius: 5px;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "input[type=\"submit\"] {\r\n" + //
                "    padding: 10px 20px;\r\n" + //
                "    background-color: #007bff;\r\n" + //
                "    color: white;\r\n" + //
                "    border: none;\r\n" + //
                "    border-radius: 5px;\r\n" + //
                "    cursor: pointer;\r\n" + //
                "    transition: background-color 0.3s ease;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "input[type=\"submit\"]:hover {\r\n" + //
                "    background-color: #0056b3;\r\n" + //
                "}" +
                "table {\r\n" + //
                "    width: 100%;\r\n" + //
                "    border-collapse: collapse;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "thead {\r\n" + //
                "    background-color: #007bff;\r\n" + //
                "    color: white;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "thead th,\r\n" + //
                "tbody td {\r\n" + //
                "    padding: 10px;\r\n" + //
                "    border: 1px solid #ddd;\r\n" + //
                "    text-align: left;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "tbody tr:nth-child(even) {\r\n" + //
                "    background-color: #f2f2f2;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "tbody tr:hover {\r\n" + //
                "    background-color: #ddd;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "input[type=\"radio\"] {\r\n" + //
                "    margin-right: 5px;\r\n" + //
                "}" +
                "</style>";
    }

    public String editRolePage(String roleSelected){
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(
                "<h1 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\" >Edit Role</h1>");
        htmlBuilder.append("<form action=\"AdministrationServlet\" method=\"post\">");
        htmlBuilder.append("<label for=\"rolename\">Edit the role " + roleSelected +" </label>");
        htmlBuilder.append("<input type=\"text\" id=\"rolename\" name=\"rolename\"><br>");
        htmlBuilder.append("<input type=\"hidden\" name=\"roleSelected\" value=\"" + roleSelected + "\">");
        htmlBuilder.append("<input type=\"hidden\" name=\"action\" value=\"Edit Role\">");
    
        // Add more options as needed
        htmlBuilder.append("</select><br>");
        htmlBuilder.append("<input type=\"submit\" name=\"submit\" value=\"Edit Role\">");
        htmlBuilder.append("</form>");
        return htmlBuilder.toString();
    }
    
}