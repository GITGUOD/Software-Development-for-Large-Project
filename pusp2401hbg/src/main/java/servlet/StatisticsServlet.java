package servlet;

import beans.ProjectBean;
import beans.TimeReportBean;
import beans.UserBean;
import formgenerator.FormGeneratorAdministration;
import formgenerator.FormGeneratorUserServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/StatisticsServlet")
public class StatisticsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Database error: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("DoPost");
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Database error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Handles the request to display project statistics. If a project ID is
     * provided, the details for that project are displayed. Otherwise, a dropdown
     * is displayed to select a project via private helper methods.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @throws SQLException
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession(true);
        UserBean currentUser = (UserBean) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect("/LoginServlet");
            return; // Add this return statement to stop further execution
        }

        Integer userID = currentUser != null ? currentUser.getUserID() : null;

        try (DatabaseHandler db = new DatabaseHandler()) {
            db.listAllProjects();
            List<ProjectBean> allProjects = db.fetchActiveProjects();
            List<ProjectBean> relevantProjects = new ArrayList<>();

            if (userID != null) {
                for (ProjectBean project : allProjects) {
                    // For admins or the project leaders, add the project to the list
                    if (currentUser.getClearanceLevel() == 1 || (project.getProjectLeader() != null
                            && project.getProjectLeader().getUserID() == userID)) {
                        relevantProjects.add(project);
                    }
                }
            }

            String projectId = request.getParameter("projectId");
            if (projectId != null) {
                displayProjectDetails(relevantProjects, projectId, response, currentUser.getClearanceLevel());
            } else {
                displayDropdown(relevantProjects, response, currentUser.getClearanceLevel());
            }
        }
    }

    /**
     * Displays a dropdown to select a project from the list of projects.
     * 
     * @param projects
     * @param response
     * @throws IOException
     */
    private void displayDropdown(List<ProjectBean> projects, HttpServletResponse response, int clearanceLevel)
            throws IOException {
        try (PrintWriter out = response.getWriter()) {
            FormGeneratorAdministration FGA = new FormGeneratorAdministration();

            out.println("<html>");
            out.println(FGA.headLayout("Select Project") + cssStyleForPage()); // injecting the style
            out.println("<body>");

            // Go back to home button
            if (clearanceLevel == 1)
                out.println(FGA.createToolbar("AdminMainScreenServlet"));
            else if (clearanceLevel == 2)
                out.println(FGA.createToolbar("PLMainScreenServlet"));
            else if (clearanceLevel == 3)
                out.println(FGA.createToolbar("BasicUserMainScreenServlet"));

            out.println("<form action='' method='POST'>");
            out.println("<select name='projectId'>");
            for (ProjectBean project : projects) {
                out.printf("<option value='%d'>%s</option>%n", project.getProjectID(), project.getProjectName());
            }
            out.println("</select>");
            out.println("<input type='submit' value='Select Project'>");
            out.println("</form>");
            out.println("</body></html>");
        }
    }

    /**
     * Displays the details for the selected project, including total time worked
     * per project member, total time worked per activity type, and total time
     * worked per role.
     *
     * @param projects
     * @param projectId
     * @param response
     * @throws IOException
     */
    private void displayProjectDetails(List<ProjectBean> projects, String projectId, HttpServletResponse response,
            int clearancelevel) throws IOException {
        ProjectBean selectedProject = projects.stream()
                .filter(project -> String.valueOf(project.getProjectID()).equals(projectId))
                .findFirst()
                .orElse(null);

        try (PrintWriter out = response.getWriter()) {
            out.println("<html>" + cssStyleForPage() + "<body>");
            // Go-back button
            out.println("<nav>");
            out.println("    <ul>");
            out.println("        <li>");
            out.println("            <form action='' method='GET'>");
            out.println("                <button type='submit'>Back to Select Project</button>");
            out.println("            </form>");
            out.println("        </li>");
            out.println("    </ul>");
            out.println("</nav>");

            if (selectedProject != null) {
                out.printf("<h1>Project: %s</h1>%n", selectedProject.getProjectName());
                out.printf("<p>ID: %d</p>%n", selectedProject.getProjectID());
                if (selectedProject.getProjectLeader() != null) {
                    out.printf("<p>Project leader: %s</p>%n", selectedProject.getProjectLeader().getUsername());
                }

                if (selectedProject.getMembersReportedTime() != null) {
                    // Display total time worked per project member
                    out.println("<h2>Total time worked per project member</h2>");
                    out.println("<table border='1'>");
                    out.println("<tr><th>Member</th><th>Total Time Worked (minutes)</th></tr>");
                    selectedProject.getMembersReportedTime().forEach((member, timeReports) -> {
                        long totalMinutes = timeReports.stream()
                                .mapToLong(timeReport -> (timeReport.getStopTime().getTime()
                                        - timeReport.getStartTime().getTime()) / (60 * 1000))
                                .sum();
                        out.printf("<tr><td>%s</td><td>%d</td></tr>%n", member.getUsername(), totalMinutes);
                    });
                    out.println("</table>");

                    // Display total time worked per activity type
                    out.println("<h2>Total time worked per activity type for the project</h2>");
                    out.println("<table border='1'>");
                    out.println("<tr><th>Activity Type</th><th>Total Time Worked (minutes)</th></tr>");
                    Stream<TimeReportBean> allTimeReports = selectedProject.getMembersReportedTime().values().stream()
                            .flatMap(List::stream);
                    Map<Integer, Long> totalMinutesByActivity = allTimeReports.collect(
                            Collectors.groupingBy(
                                    TimeReportBean::getActivity,
                                    Collectors.summingLong(
                                            report -> (report.getStopTime().getTime() - report.getStartTime().getTime())
                                                    / (60 * 1000))));
                    totalMinutesByActivity.forEach((activity, totalMinutes) -> {
                        out.printf("<tr><td>Activity %d</td><td>%d</td></tr>%n", activity, totalMinutes);
                    });
                    out.println("</table>");

                    // Display total time worked per role
                    out.println("<h2>Total time worked per role in the project</h2>");
                    out.println("<table border='1'>");
                    out.println("<tr><th>Role</th><th>Total Time Worked (minutes)</th></tr>");
                    Map<UserBean, String> membersWithRoles = selectedProject.getProjectMembers();
                    Stream<Map.Entry<String, TimeReportBean>> roleToTimeReportStream = selectedProject
                            .getMembersReportedTime().entrySet().stream()
                            .flatMap(entry -> {
                                String role = membersWithRoles.get(entry.getKey());
                                return entry.getValue().stream()
                                        .map(timeReport -> new AbstractMap.SimpleEntry<>(role, timeReport));
                            });
                    Map<String, Long> totalMinutesByRole = roleToTimeReportStream
                            .filter(entry -> entry.getKey() != null)
                            .collect(Collectors.groupingBy(
                                    entry -> Objects.requireNonNullElse(entry.getKey(), "Undefined"),
                                    Collectors.summingLong(entry -> (entry.getValue().getStopTime().getTime()
                                            - entry.getValue().getStartTime().getTime()) / (60 * 1000))));
                    totalMinutesByRole.forEach((role, totalMinutes) -> {
                        out.printf("<tr><td>%s</td><td>%d</td></tr>%n", role, totalMinutes);
                    });
                    out.println("</table>");
                } else {
                    out.println("<p>No time reports found for this project.</p>");
                }
            } else {
                out.println("<p>Project not found.</p>");
            }
            out.println("</body></html>");
        }
    }

    /**
     * 
     * @return string the css string for this servlet
     */
    private String cssStyleForPage() {
        return "<head>\r\n" + //
                "    <title>Project Details</title>\r\n" + //
                "    <style>\r\n" + //
                "        body {\r\n" + //
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            background-color: #f4f4f4;\r\n" + //
                "            color: #333;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        form {\r\n" + //
                "            margin-bottom: 20px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        button[type=\"submit\"] {\r\n" + //
                "            padding: 10px 20px;\r\n" + //
                "            background-color: #007bff;\r\n" + //
                "            color: white;\r\n" + //
                "            border: none;\r\n" + //
                "            border-radius: 5px;\r\n" + //
                "            cursor: pointer;\r\n" + //
                "            transition: background-color 0.3s ease;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        button[type=\"submit\"]:hover {\r\n" + //
                "            background-color: #0056b3;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        h1 {\r\n" + //
                "            margin: 20px 0;\r\n" + //
                "            color: #007bff;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        h2 {\r\n" + //
                "            margin-top: 30px;\r\n" + //
                "            color: #007bff;\r\n" + //
                "            border-bottom: 2px solid #007bff;\r\n" + //
                "            padding-bottom: 5px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        table {\r\n" + //
                "            width: 100%;\r\n" + //
                "            border-collapse: collapse;\r\n" + //
                "            margin-bottom: 30px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        th, td {\r\n" + //
                "            border: 1px solid #ddd;\r\n" + //
                "            padding: 12px;\r\n" + //
                "            text-align: left;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        th {\r\n" + //
                "            background-color: #007bff;\r\n" + //
                "            color: white;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        tr:nth-child(even) {\r\n" + //
                "            background-color: #f2f2f2;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        tr:hover {\r\n" + //
                "            background-color: #ddd;\r\n" + //
                "        }\r\n" + //
                "nav {\r\n" + //
                "    width: 100%" +
                "    background-color: #f4f4f4; /* Background color */\r\n" + //
                "    padding: 10px; /* Padding for spacing */\r\n" + //
                "    border-bottom: 1px solid #ddd; /* Border bottom for separation */\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "nav ul {\r\n" + //
                "    list-style-type: none; /* Remove default list styles */\r\n" + //
                "    margin: 0; /* Remove default margin */\r\n" + //
                "    padding: 0; /* Remove default padding */\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "nav li {\r\n" + //
                "    display: inline; /* Display list items inline */\r\n" + //
                "    margin-right: 10px; /* Add margin between list items */\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "nav li:last-child {\r\n" + //
                "    margin-right: 0; /* Remove margin from the last list item */\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "nav button[type=\"submit\"] {\r\n" + //
                "    background-color: #007bff; /* Button background color */\r\n" + //
                "    color: white; /* Button text color */\r\n" + //
                "    border: none; /* Remove button border */\r\n" + //
                "    border-radius: 5px; /* Add button border radius */\r\n" + //
                "    padding: 10px 20px; /* Add padding to the button */\r\n" + //
                "    cursor: pointer; /* Change cursor to pointer on hover */\r\n" + //
                "    transition: background-color 0.3s ease; /* Smooth transition for background color change */\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "nav button[type=\"submit\"]:hover {\r\n" + //
                "    background-color: #0056b3; /* Change background color on hover */\r\n" + //
                "}\r\n" + //
                ".toolbar {\r\n" + //
                "    background-color: #007bff;\r\n" + //
                "    padding: 10px;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                ".toolbar ul {\r\n" + //
                "    list-style-type: none;\r\n" + //
                "    margin: 0;\r\n" + //
                "    padding: 0;\r\n" + //
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
                "form {\r\n" + //
                "    margin: 20px;\r\n" + //
                "}\r\n" + //
                "\r\n" + //
                "select {\r\n" + //
                "    padding: 10px;\r\n" + //
                "    font-size: 16px;\r\n" + //
                "    border-radius: 5px;\r\n" + //
                "    border: 1px solid #ddd;\r\n" + //
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
                "    </style>\r\n" + //
                "</head>";

    }
}
