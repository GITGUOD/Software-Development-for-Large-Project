package formgenerator;

import beans.ProjectBean;
import beans.TimeReportBean;
import beans.UserBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.jni.User;

public class FormGeneratorPGS {
    private String formElement(String par) {
        return '"' + par + '"';
    }

    public String generateHTMLContent(List<ProjectBean> projects, int selectedProjectId,
            Map<String, List<TimeReportBean>> map,
            List<String> rolesList, 
            String addNewUserAtEnd, int clearanceLevel)
            throws SQLException { /// New Argument added because of New user form have be at the end of the page

        // CSS styles
        StringBuilder cssStyles = new StringBuilder();
        cssStyles.append("<style>")
                .append("table {")
                .append("font-family: Arial, sans-serif;")
                .append("border-collapse: collapse;")
                .append("width: 100%;")
                .append("}")
                .append("td, th {")
                .append("border: 1px solid #dddddd;")
                .append("text-align: left;")
                .append("padding: 8px;")
                .append("}")
                .append("tr:nth-child(even) {")
                .append("background-color: #f2f2f2;")
                .append("}")
                .append("th {")
                .append("background-color: rgb(0, 123, 255);")
                .append("color: white;")
                .append("}")
                .append("</style>");

        // Common header
        StringBuilder html = new StringBuilder();
        FormGeneratorAdministration FGA = new FormGeneratorAdministration();

        int clear = clearanceLevel;
        String redirectTo = "AdminMainScreenServlet";
            if(clear == 3){
                redirectTo = "BasicUserMainScreenServlet";
            }else if(clear == 2){
                redirectTo = "PLMainScreenServlet";
            }else if (clear == 1){
                redirectTo = "AdminMainScreenServlet";
            }
        html.append("<html>")
                .append(cssStyles)
                .append(FGA.headLayout("Project Groups"))
                .append("<body>")
                .append(FGA.createToolbar(redirectTo))
                .append("<h2>Project Groups</h2>"); // A found in here which Home button was länked to PLserverlet which was wrong

        // Dropdown for project selection (Filter option)
        html.append("<form method='get' action='ProjectGroupServlet'>")
                .append("<select name='selectedProject'>")
                .append("<option value='-1'>Select a project</option>");

        for (ProjectBean project : projects) {
            if (project != null) {
                html.append(String.format("<option value='%s' %s>%s</option>",
                        project.getProjectID(),
                        project.getProjectID() == selectedProjectId ? "selected" : "",
                        project.getProjectName()));
            }
        }

        html.append("</select>")
                .append("<input type='submit' name=\"action\" value='Filter'/>")
                .append("</form>");

        // Table for displaying project members
        html.append("<table>")
                .append("<tr>")
                .append("<th>Name</th>")
                .append("<th>Role</th>")
                .append("<th>Total Time</th>")
                .append("<th>Contact Details</th>")
                .append("<th>Delete</th>")
                .append("</tr>");

        // Populate table with project members

        if (selectedProjectId != -1) {
            for (ProjectBean pb : projects) {
                if (pb.getProjectID() == selectedProjectId) {
                    Map<UserBean, String> projectMembers = pb.getProjectMembers();
                    if (projectMembers != null) {
                        for (Map.Entry<UserBean, String> entry : projectMembers.entrySet()) {
                            UserBean user = entry.getKey();
                            String role = entry.getValue();
                            html.append("<tr>")
                                    .append("<td>").append(user.getUsername()).append("</td>") // Name
                                    .append("<td>").append(role)
                                    .append(createDropDownMenuRoles(user.getUserID(), pb.getProjectID(), rolesList))
                                    .append("</td>") // Role
                                    .append("<td>").append(getTime(user, map)).append("</td>") // Total Time
                                    .append("<td>").append(user.getEmail()).append("</td>") // Contact Details
                                    .append("<td>").append(deleteButton(user.getUserID(), pb.getProjectID()))
                                    .append("</td>") // Delete
                                    .append("</tr>");
                        }
                    }
                }
            }
        }

        // Close table
        html.append("</table>");
        String htmlForm = "<style>" +
                "  /* Style for forms */" +
                "  form {" +
                "    display: inline-block;" +
                "    margin: 10px;" +
                "  }" +
                "  input[type='submit'], button {" + // Apply styles to all input[type='submit'] and button elements
                "    padding: 10px 20px;" +
                "    background-color: rgb(0, 123, 255);" +
                "    color: white;" +
                "    border: none;" +
                "    border-radius: 4px;" +
                "    cursor: pointer;" +
                "    transition: background-color 0.3s ease;" +
                "  }" +
                "  input[type='submit']:hover, button:hover {" + // Apply hover effect to all input[type='submit'] and
                                                                 // button elements
                "    background-color: rgb(0, 123, 255);" +
                "  }" +
                "</style>" +
                "</head>" +
                "<body>" +
                " <form method='post' action='ProjectGroupServlet'>\r\n" + //
                "        <input type='hidden' name='action' value='add'>\r\n" + //
                "        <input type='submit' value='Add'>\r\n" + //
                "    </form>\r\n" + //
                "\r\n" + //
                "    <button id='myButton'>Save Changes</button>\r\n" + // Apply the button styling to the button
                                                                        // element
                "\r\n" + //
                "    <form method='post' action='ProjectGroupServlet'>\r\n" + //
                "        <input type='submit' name='action' value='Cancel'>\r\n" + //
                "    </form>\r\n" + //
                "\r\n" + //
                "\r\n" + //
                " <form id=\"hiddenForm\" style=\"display: none;\" method=\"post\" action=\"ProjectGroupServlet\">\n" +
                "    <input type=\"hidden\" name=\"action\" value=\"Save Changes\">\n" +
                "    <input type=\"hidden\" name=\"editOption\" id=\"editOption\" value=\"\">\n" +
                "</form>\n" +
                "\n" +
                addNewUserAtEnd + // added the new user at end of the page
                "<script>\n" +
                "    const hiddenForm = document.getElementById('hiddenForm');\n" +
                "    const myButton = document.getElementById('myButton');\n" +
                "    const checkboxes = document.querySelectorAll('input[type=\"checkbox\"]');\n" +
                "    const roleDropdowns = document.querySelectorAll('select[name=\"editOption\"]');\n" +
                "\n" +
                "    myButton.addEventListener('click', function(event) {\n" +
                "        event.preventDefault();\n" +
                "        hiddenForm.submit();\n" +
                "    });\n" +
                "\n" +
                "    checkboxes.forEach(function(checkbox) {\n" +
                "        checkbox.addEventListener('change', function() {\n" +
                "            console.log(hiddenForm);\n" +
                "            if (checkbox.checked) {\n" +
                "                const clonedCheckbox = checkbox.cloneNode(true);\n" +
                "                clonedCheckbox.setAttribute('style', 'display: none;');\n" +
                "                hiddenForm.appendChild(clonedCheckbox);\n" +
                "            } else {\n" +
                "                const clonedCheckbox = hiddenForm.querySelector('input[type=\"checkbox\"][value=\"' + checkbox.value + '\"]');\n"
                +
                "                if (clonedCheckbox) {\n" +
                "                    hiddenForm.removeChild(clonedCheckbox);\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    });\n" +
                "\n" +
                "    roleDropdowns.forEach(function(roleDropdown) {\n" +
                "    roleDropdown.addEventListener('change', function() {\n" +
                "        const selectedRole = roleDropdown.value;\n" +
                "        const userId = roleDropdown.options[roleDropdown.selectedIndex].getAttribute('data-userid');\n"
                +
                "        const projectId = roleDropdown.options[roleDropdown.selectedIndex].getAttribute('data-projectid');\n"
                +
                "        const combinedValue = userId + \":\" + projectId + \":\" + selectedRole;\n" +
                "        console.log('Combined value:', combinedValue);\n" +
                "        document.getElementById('editOption').value = combinedValue;\n" +
                "    });\n" +
                "});\n" +
                "</script>";

        html.append(htmlForm);

        html.append("</body></html>");

        return html.toString();
    }

    protected String getTime(UserBean user, Map<String, List<TimeReportBean>> map) throws SQLException {
        long time = 0;
        try {

            for (TimeReportBean tRB : map.get(user.getUsername())) {

                long start = (tRB.getStopTime().getTime()) - (tRB.getStartTime().getTime());
                time += start;
            }

        } catch (Exception e) {
            System.out.println("Error in FormGeneratorPGS.java");
            e.printStackTrace();
        }

        return String.valueOf(((time / 1000) / 60) / 60);

    }

    private String createDropDownMenuRoles(int userId, int projectId, List<String> roles) {
        StringBuilder html = new StringBuilder();
        html.append("<p>");
        html.append("<select id=\"").append(formElement("userId_" + userId + "_projectId_" + projectId))
                .append("\" name=\"editOption\">"); // Change the name attribute to "editOption"

        for (String role : roles) {
            // Add data-userid and data-projectid attributes to each option element
            html.append("<option value=\"").append(role)
                    .append("\" data-userid=\"").append(userId)
                    .append("\" data-projectid=\"").append(projectId)
                    .append("\">").append(role).append("</option>");
        }

        html.append("</select>");
        html.append("</p>"); // Close the <p> tag

        return html.toString();
    }

    private String deleteButton(int userId, int projectId) {
        StringBuilder html = new StringBuilder();
        html.append("<p>");
        // Combine userId and projectId in the checkbox value, separated by a special
        // character
        html.append("<input type=\"checkbox\" name=\"deleteOption\" value=\"").append(userId).append(":")
                .append(projectId).append("\">");
        html.append("</p>");
        return html.toString();
    }

}