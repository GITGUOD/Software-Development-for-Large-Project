package formgenerator;

public class FormGeneratorUserServlet {

    private String formElement(String par) {
        return '"' + par + '"';
    }

    public String getLoginForm() {
        String html = "<h1>ePuss+</h1>";
        html += "<br>";
        html += nameRequestForm();
        html += passwordRequestForm();
        html += signInButton();
        return html;
    }

    public String nameRequestForm() {
        String html = "<label for=" + formElement("username") + ">Username</label>";
        html += "<input type=" + formElement("text") + " id=" + formElement("username") + ">";
        return html;
    }

    public String passwordRequestForm() {
        String html = "<label for=" + formElement("password") + ">Password</label>";
        html += "<input type=" + formElement("text") + " id=" + formElement("password") + ">";
        return html;
    }

    public String signInButton() {
        String html = "<input type=" + formElement("submit") + " value=" + formElement("Submit") + ">";
        return html;
    }

    public String getUserMainScreenForm() {
        String html = "<h1>ePuss+</h1>";
        html += "<br>";
        html += "<form method=\"get\">";
        html += "<input type=\"submit\" name=\"time\" value=\"Time\">";
        html += "<input type=\"submit\" name=\"projectGroups\" value=\"Project groups\">";
        html += "<input type=\"submit\" name=\"statistics\" value=\"Statistics\">";
        html += "</form>";
        return html;
    }

    public String getPLMainScreenForm() {
        String html = "<p>Please select an action:</p>";
        html += "<form method=\"get\">";
        html += "<input type=\"submit\" name=\"time\" value=\"Time\">";
        html += "<input type=\"submit\" name=\"projectGroups\" value=\"Project Groups\">";
        html += "<input type=\"submit\" name=\"statistik\" value=\"Statistics\">";
        html += "<input type=\"submit\" name=\"manageUsers\" value=\"Manage Users\">";
        html += "</form>";
        return html;
    }

    public String getAdminMainScreenForm() {
        String html = "<p>Please select an action:</p>";
        html += "<form method=\"post\">";
        html += "<input type=\"submit\" name=\"timereport\" value=\"Time\">";
        html += "<input type=\"submit\" name=\"projectgroup\" value=\"Project Groups\">";
        html += "<input type=\"submit\" name=\"statistik\" value=\"Statistics\">";
        html += "<input type=\"submit\" name=\"admin\" value=\"Administration\">";
        html += "<input type=\"submit\" name=\"logout\" value=\"Sign Out\">";
        html += "</form>";
        return html;
    }
}