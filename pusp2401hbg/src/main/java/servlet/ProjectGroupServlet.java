package servlet;

import beans.ProjectBean;
import beans.TimeReportBean;
import beans.UserBean;
import formgenerator.FormGeneratorPGS;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ProjectGroupServlet")
public class ProjectGroupServlet extends HttpServlet {
    FormGeneratorPGS formGenerator = new FormGeneratorPGS();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(true);
        UserBean user = (UserBean) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("/LoginServlet");
            return; // Add this return statement to stop further execution
        }

        int selectedProjectId = -1;
        String selectedProjectParam = request.getParameter("selectedProject");
        if (selectedProjectParam != null && !selectedProjectParam.isEmpty()) {
            try {
                selectedProjectId = Integer.parseInt(selectedProjectParam);
            } catch (NumberFormatException e) {
                // Log error or notify user of invalid format, if necessary
            }
        }

        try (DatabaseHandler db = new DatabaseHandler()) {
            // Fetch all projects to populate the dropdown
            List<ProjectBean> projects = db.listAllProjects();
            // Assuming db.getProjectMembers(int projectId) returns a list of UserBean
            // objects for the project
            Map<Integer, List<UserBean>> projectMembersMap = new HashMap<>();

            // String selectProject = request.getParameter("selectedProject");
            List<UserBean> members = db.getProjectMembers(selectedProjectId);

            // Ensure the list is not null before putting it in the map
            projectMembersMap.put(selectedProjectId, members != null ? members : new ArrayList<>());

            // Fetch the selected project ID from request parameters
            List<UserBean> users = db.listAllUsers();
            Map<String, List<TimeReportBean>> map = new HashMap<>();

            for (UserBean uB : users) {
                map.put(uB.getUsername(), db.listTimeReportsByUser(uB));
            }
            // Assuming roles are predefined or fetched from the database
            List<String> roles = db.listRoles();

            // Generate HTML content based on selected project
            String action = request.getParameter("action");

            StringBuilder htmlBuilder = new StringBuilder();
            if ("add".equals(action)) {
                // Display the dropdown menu only when "add" action is requested
                // Display the dropdown menu only when "add" action is requested
                htmlBuilder.append("<div style=\"border: 1px solid #000; padding: 10px;\">"); // Adding border and
                                                                                              // padding
                htmlBuilder.append("<h3>New User</h3>");
                htmlBuilder.append("<form action=\"ProjectGroupServlet\" method=\"post\">");
                // Add dropdown menus
                htmlBuilder
                        .append(addDropdownMenu("selectedProjects", projects, "selectedUsers", users, "selectedRoles",
                                roles));
                // Add the add button
                htmlBuilder.append("<button type=\"submit\" name=\"action\" value=\"add\">Add User</button>");
                htmlBuilder.append("</form>");
                htmlBuilder.append("</div>");

                //out.println(htmlBuilder);

            }

            String htmlContent = formGenerator.generateHTMLContent(projects, selectedProjectId, map, roles, htmlBuilder.toString(), user.getClearanceLevel());
            //htmlContent += htmlBuilder.toString();// adding the add form in end of the html
            out.println(htmlContent);

        } catch (SQLException ex) {
            out.println("Database connection error: " + ex.getMessage());
            // Proper exception handling or logging
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(true);
        UserBean user = (UserBean) session.getAttribute("user");

        if (user != null && user.getClearanceLevel() == 1 || Objects.requireNonNull(user).getClearanceLevel() == 2) {
            if ("add".equals(action)) {
                String selectedProjectIdAction = request.getParameter("selectedProjects");
                String selectedUserIdAction = request.getParameter("selectedUsers");
                String roleAction = request.getParameter("selectedRoles");

                if (selectedProjectIdAction != null && !selectedProjectIdAction.isEmpty() &&
                        selectedUserIdAction != null && !selectedUserIdAction.isEmpty() &&
                        roleAction != null && !roleAction.isEmpty()) {

                    try (DatabaseHandler db = new DatabaseHandler()) {
                        if (!db.isUserInProject(Integer.parseInt(selectedUserIdAction),
                                Integer.parseInt(selectedProjectIdAction))) {
                            db.addUserToProject(Integer.parseInt(selectedUserIdAction),
                                    Integer.parseInt(selectedProjectIdAction),
                                    roleAction);
                            System.out.println(Integer.parseInt(selectedUserIdAction) + " " +
                                    Integer.parseInt(selectedProjectIdAction) + " " +
                                    roleAction);
                        } else {
                            System.out.println("User is already assigned to the project.");
                            // You can add additional handling here, like showing a message to the user
                        }
                    } catch (SQLException e) {
                        System.out.println("Error adding user to project: " + e.getMessage());
                        // You can log the exception or show an error message to the user
                    }
                }
            } else if ("Save Changes".equals(action)) {
                String[] deleteOptions = request.getParameterValues("deleteOption");
                String[] editOptions = request.getParameterValues("editOption");
                // Check if editOptions is null and has values. Might have to change this logic,
                // since edit options has probably always values.
                if (editOptions != null && editOptions.length > 0) {
                    try (DatabaseHandler db = new DatabaseHandler()) {
                        for (String optionValue : editOptions) {
                            String[] parts = optionValue.split(":");
                            System.out.println(parts[0]);
                            if (parts.length == 3) {
                                int userId = Integer.parseInt(parts[0]);
                                int projectId = Integer.parseInt(parts[1]);
                                String newRole = parts[2];
                                db.updateUserRole(userId, projectId, newRole);
                            }
                        }
                    } catch (SQLException ex) {
                        // Handle exception
                        System.out.println("Error removing users from projects: " + ex.getMessage());
                    }

                } else {
                    // No checkboxes selected
                    System.out.println("No roles selected.");
                }
                // Check if deleteOptions is not null and has values
                if (deleteOptions != null && deleteOptions.length > 0) {
                    // Print the values to the console or process them as needed
                    System.out.println(deleteOptions.toString());
                    try (DatabaseHandler db = new DatabaseHandler()) {
                        for (String optionValue : deleteOptions) {
                            String[] parts = optionValue.split(":");

                            if (parts.length == 2) {
                                int userId = Integer.parseInt(parts[0]);
                                int projectId = Integer.parseInt(parts[1]);
                                System.out.println(userId + " " + projectId);
                                db.removeUserFromProject(userId, projectId);
                            }
                        }
                    } catch (SQLException ex) {
                        // Handle exception
                        System.out.println("Error removing users from projects: " + ex.getMessage());
                    }
                } else {
                    // No checkboxes selected
                    System.out.println("No checkboxes selected.");
                }

            } else if ("Cancel".equals(action)) {
                session = request.getSession();
                session.removeAttribute("selectedOptions");
            }

            // Redirect to doGet to display the updated data
            // response.sendRedirect("ProjectGroupServlet");
        }else{
            response.sendRedirect("/ProjectGroupServlet"); // it better to redirect to same page to normal user cant each anython rather then redirec to login page
        }
        doGet(request, response);
    }

    private String addDropdownMenu(String projectParameterName, List<ProjectBean> projectList, String userParameterName,
            List<UserBean> userList, String roleParameterName, List<String> rolesList) {
        StringBuilder htmlBuilder = new StringBuilder();
        // projects
        htmlBuilder.append("<label for=\"").append(projectParameterName).append("\">").append("Projects")
                .append(":</label>");
        htmlBuilder.append("<select id=\"").append(projectParameterName).append("\" name=\"")
                .append(projectParameterName).append("\" style=\"margin-right: 10px;\">"); // Add style for right margin
        htmlBuilder.append("<option value=\"").append("").append("\" data-id=\"").append("").append("\">").append("")
                .append("</option>");
        for (ProjectBean pB : projectList) {
            htmlBuilder.append("<option value=\"").append(pB.getProjectID()).append("\" data-id=\"")
                    .append(pB.getProjectID()).append("\">").append(pB.getProjectName()).append("</option>");
        }
        htmlBuilder.append("</select>");

        // users
        htmlBuilder.append("<label for=\"").append(userParameterName).append("\">").append("Users").append(":</label>");
        htmlBuilder.append("<select id=\"").append(userParameterName).append("\" name=\"").append(userParameterName)
                .append("\" style=\"margin-right: 10px;\">"); // Add style for right margin
        htmlBuilder.append("<option value=\"").append("").append("\" data-id=\"").append("").append("\">").append("")
                .append("</option>");
        for (UserBean uB : userList) {
            htmlBuilder.append("<option value=\"").append(uB.getUserID()).append("\" data-id=\"").append(uB.getUserID())
                    .append("\">").append(uB.getUsername()).append("</option>");
        }
        htmlBuilder.append("</select>");

        // roles
        htmlBuilder.append("<label for=\"").append(roleParameterName).append("\">").append("Roles").append(":</label>");
        htmlBuilder.append("<select id=\"").append(roleParameterName).append("\" name=\"").append(roleParameterName)
                .append("\" style=\"margin-right: 10px;\">"); // Add style for right margin
        htmlBuilder.append("<option value=\"").append("").append("\" data-id=\"").append("").append("\">").append("")
                .append("</option>");
        for (String roleString : rolesList) {
            htmlBuilder.append("<option value=\"").append(roleString).append("\" data-id=\"").append(roleString)
                    .append("\">").append(roleString).append("</option>");
        }
        htmlBuilder.append("</select>");

        return htmlBuilder.toString();
    }

}