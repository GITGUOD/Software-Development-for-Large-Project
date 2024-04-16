package servlet;

import formgenerator.FormGeneratorAdministration;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beans.ProjectBean;
import beans.UserBean;

@WebServlet("/AdministrationServlet")
public class AdministrationServlet extends HttpServlet {
    private FormGeneratorAdministration FGA = new FormGeneratorAdministration();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String formHtml;

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println(FGA.headLayout("Administration"));
        out.println("<body>");
        out.println(FGA.createToolbar("AdminMainScreenServlet"));

        HttpSession session = request.getSession(true);
        UserBean user = (UserBean) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/LoginServlet");
            return; // Add this return statement to stop further execution
        }
        if (user != null && user.getClearanceLevel() == 1) {
            out.print(FGA.cssStyleForPage());
            if (action == null) {
                // Default action: Show welcome page
                formHtml = FGA.topLevelAdminPage();
            } else if (action.equals("Manage Users")) {
                // Action: Show manage users page
                formHtml = FGA.manageUsersAdminPage();
            } else if (action.equals("Manage Project Groups")) {
                // Action: Show project group management page
                formHtml = FGA.projectGroupAdminPage();
            } else if (action.equals("New User")) {
                // Action: Show new user page
                formHtml = FGA.newUserPage();
            } else if (action.equals("Remove user")) {
                // Action: Show remove user page
                formHtml = FGA.removeUserPage();
            } else if (action.equals("Manage Roles")) {
                // Action: Show manage role page
                formHtml = FGA.addNewRolePage();
                formHtml += FGA.showAllRoles();
            } else if (action.equals("New Project")) {
                // Action: Show new project group page
                formHtml = FGA.newProjectGroupPage();
            } else if (action.equals("Archive Project")) {
                // Action: Show archive project group page
                formHtml = FGA.archiveProjectGroupPage();
            } else if (action.equals("Remove Role")) {
                String selectedRole = request.getParameter("roles");
                boolean success = false;
                try (DatabaseHandler db = new DatabaseHandler()) {
                    success = db.removeRole(selectedRole);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(success) formHtml = "The role was removed!";
                else formHtml = "This role has constraints. There might be users associated with this role.";
            }
            else if (action.equals("Edit role")) {
                String roleSelected = request.getParameter("roles");
                formHtml = FGA.editRolePage(roleSelected);

            }
            
            else {
                // Handle other actions
                formHtml = "Action not supported";
            }

            response.setContentType("text/html");
            response.getWriter().println(formHtml);
            
            out.println("</body>");
            out.println("</html>");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        UserBean user = (UserBean) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/LoginServlet");
            return; // Add this return statement to stop further execution
        }

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println(FGA.headLayout("Administration"));
        out.println("<body>");
        out.println(FGA.createToolbar("AdminMainScreenServlet"));

        // Check if user is logged in and has admin clearance
        if (user != null && user.getClearanceLevel() == 1) {
            String action = request.getParameter("action");
            String formHtml;
            
            if (action == null) {
                // Default action: Show welcome page
                formHtml = FGA.topLevelAdminPage();
            } else if (action.equals("Add User")) {
                
                String username = request.getParameter("username");
                String email = request.getParameter("email");
                String clearance = request.getParameter("clearancelevel");

               UserBean userB = new UserBean(1, username, email, Integer.parseInt(clearance));    
                String generatedPassword = "";
                try (DatabaseHandler db = new DatabaseHandler()) {
                    generatedPassword = db.createUser(userB);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                formHtml = "<p>User created: " + username + "</p>";
                formHtml += "<p>Given password: " + generatedPassword + "</p>" ;
                formHtml += "<p>Your password cannot be changed!</p>";
            }
            else if (action.equals("AddRole")) {
                // Action: Add a new role
                String newRole = request.getParameter("newRole");
                DatabaseHandler db = new DatabaseHandler();
                try {
                    db.addRole(newRole);
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Refreshes the page to show the updated list of roles
                formHtml = FGA.addNewRolePage();
                //formHtml += FGA.showAllRoles();
            } else if (action.equals("AddGroup")) {
                // Add a new group
                String groupName = request.getParameter("groupname");
                String groupLeader = request.getParameter("groupleader");
                DatabaseHandler db = new DatabaseHandler();
                ProjectBean PB = new ProjectBean();
                PB.setProjectName(groupName);
                try {
                    int userID = db.findIDfromUser(groupLeader);
                    db.createProject(PB);
                    int groupID = db.getProjectID(groupName);
                    db.addUserToProject(userID, groupID, "Project Leader");
                    db.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                formHtml = "Group added with name: " + groupName + " and leader: " + groupLeader;
            } else if (action.equals("Archive project")) {
                String selectedGroup = request.getParameter("selectedGroup");
                if (selectedGroup != null) {
                        try (DatabaseHandler db = new DatabaseHandler()){
                            if(!db.isProjectArchived(db.getProjectID(selectedGroup)))
                                db.updateProjectArchivedStatus(true, db.getProjectID(selectedGroup));
                            else
                                db.updateProjectArchivedStatus(false, db.getProjectID(selectedGroup));

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    formHtml = "Selected project group archived successfully.";
                } else {
                    formHtml = "No project group selected for removal.";
                }

            } else if (action.equals("Remove")) {
                String selectedUser = request.getParameter("selectedUsers");
                int ok = -1;
                try (DatabaseHandler db = new DatabaseHandler()) {
                    UserBean userBean = db.findUser(selectedUser);
                    ok = db.deleteUser(userBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (ok == -1) {
                    formHtml = "The user could not be deleted due to constraints. It might be attached to a project!";
                }
                else {
                    formHtml = selectedUser + " is no more!";
                }   
            }

            else if (action.equals("Edit Role")) {
                String newRoleName = request.getParameter("rolename");
                String oldRoleName = request.getParameter("roleSelected");
                boolean success = false;
                try (DatabaseHandler db = new DatabaseHandler()) {
                    success = db.updateRole(newRoleName, oldRoleName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (success) {
                    formHtml = "Role updated successfully!";
                } else {
                    formHtml = "Unable to update a role that is already in use by members of a project";
                }
            }
            
             else {
                // Handle other actions
                formHtml = "Action not supported";
            }

            response.setContentType("text/html");
            response.getWriter().println(formHtml);
        }
    }
}
