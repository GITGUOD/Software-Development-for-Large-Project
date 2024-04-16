package formgenerator;

/*
 * This class provides forms to be displayed to the user
 */
public class FormGenerator {

	/**
	 * @param par String
	 * @return String surrounded by quotation marks
	 */
	public String formElement(String par) {
		return '"' + par + '"';
	}

	/**
	 * @return HTML code for displaying login form
	 */
	public String getLoginForm(String loginErrorMessage) {
		/*
		 * StringBuilder htmlForm = new StringBuilder();
		 * htmlForm.append("<form action=\"LoginServlet\" method=\"post\">");
		 * htmlForm.append("<label for=\"username\">Username:</label>");
		 * htmlForm.append("<input type=\"text\" id=\"username\" name=\"username\"><br>"
		 * );
		 * htmlForm.append("<label for=\"password\">Password:</label>");
		 * htmlForm.
		 * append("<input type=\"password\" id=\"password\" name=\"password\"><br>");
		 * htmlForm.append("<input type=\"submit\" value=\"Login\">");
		 * htmlForm.append("</form>");
		 */
		String htmlForm = "<!DOCTYPE html>\r\n" + //
				"<html lang=\"en\">\r\n" + //
				"<head>\r\n" + //
				"<meta charset=\"UTF-8\">\r\n" + //
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
				"<title>Login</title>\r\n" + //
				"<style>\r\n" + //
				"    body {\r\n" + //
				"        font-family: Arial, sans-serif;\r\n" + //
				"        background-color: #f2f2f2;\r\n" + //
				"        margin: 0;\r\n" + //
				"        padding: 0;\r\n" + //
				"    }\r\n" + //
				"    .container {\r\n" + //
				"        max-width: 400px;\r\n" + //
				"        margin: 50px auto;\r\n" + //
				"        text-align: center;\r\n" + //
				"        background-color: #fff;\r\n" + //
				"        padding: 20px;\r\n" + //
				"        border-radius: 5px;\r\n" + //
				"        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\r\n" + //
				"    }\r\n" + //
				"    .logo {\r\n" + //
				"        width: 150px;\r\n" + //
				"        height: auto;\r\n" + //
				"        margin-bottom: 10px;\r\n" + //
				"    }\r\n" + //
				"    .brand-text {\r\n" + //
				"        font-size: 20px;\r\n" + //
				"        color: #555;\r\n" + //
				"    }\r\n" + //
				"    h1 {\r\n" + //
				"        color: #333;\r\n" + //
				"    }\r\n" + //
				"    label {\r\n" + //
				"        display: inline-block;\r\n" + //
				"        width: 100px;\r\n" + //
				"        text-align: left;\r\n" + //
				"    }\r\n" + //
				"    input[type=\"text\"],\r\n" + //
				"    input[type=\"password\"],\r\n" + //
				"    input[type=\"submit\"] {\r\n" + //
				"        width: 100%;\r\n" + //
				"        padding: 10px;\r\n" + //
				"        margin: 5px 0;\r\n" + //
				"        border: 1px solid #ccc;\r\n" + //
				"        border-radius: 4px;\r\n" + //
				"        box-sizing: border-box;\r\n" + //
				"    }\r\n" + //
				"    input[type=\"submit\"] {\r\n" + //
				"        background-color: #007bff;\r\n" + //
				"        color: white;\r\n" + //
				"        cursor: pointer;\r\n" + //
				"    }\r\n" + //
				".error-message {\r\n" + //
				"        color: red;\r\n" + //
				"        font-size: 14px;\r\n" + //
				"        margin-top: 5px;\r\n" + //
				"    }" +
				"    input[type=\"submit\"]:hover {\r\n" + //
				"        background-color: #0056b3;\r\n" + //
				"    }\r\n" + //
				"</style>\r\n" + //
				"</head>\r\n" + //
				"<body>\r\n" + //
				"<div class=\"container\">\r\n" + //
				"    <img src=\"./docIcon.png\" alt=\"Logo\" class=\"logo\">\r\n" + //
				"    <div class=\"brand-text\">ePuss+</div>\r\n" + //
				"    <h1>Login</h1>\r\n" + //
				"    <form action=\"LoginServlet\" method=\"post\">\r\n" + //
				"        <label for=\"username\">Username:</label>\r\n" + //
				"        <input type=\"text\" id=\"username\" name=\"username\"><br>\r\n" + //
				"        <label for=\"password\">Password:</label>\r\n" + //
				"        <input type=\"password\" id=\"password\" name=\"password\"><br>\r\n" + //
				"        <input type=\"submit\" value=\"Login\">\r\n" + //
				"        <div id='error-message' class='error-message'>"+loginErrorMessage+"</div>" +
				"    </form>\r\n" + //
				"</div>\r\n" + //
				"</body>\r\n" + //
				"</html>\r\n" + //
				"";
		return htmlForm.toString();
	}

	/**
	 * @return HTML code for displaying the main screen for admins
	 */
	public String getAdminMainScreenForm(String username) {
		String html = "<!DOCTYPE html>\r\n" + //
				"<html>\r\n" + //
				CssStyleForPage() +
				"<body>\r\n" + //
				"<div class=\"toolbar\">\r\n" + //
				"    <ul>\r\n" + //
				"        <li><a href=\"/AdminMainScreenServlet\">Home</a></li>\r\n" + //
				"    </ul>\r\n" + //
				"</div>\r\n" + //
				"\r\n" + //
				"<h1>You are signed in as " + username + "</h1>\r\n" + //
				"<p>Welcome to ePUSS+ bar menu</p>\r\n" + //
				"<form action=\"AdminMainScreenServlet\" method=\"get\">\r\n" + //
				"    <input type=\"submit\" name=\"action\" value=\"Time\">\r\n" + //
				"    <input type=\"submit\" name=\"action\" value=\"Project groups\">\r\n" + //
				"    <input type=\"submit\" name=\"action\" value=\"Statistics\">\r\n" + //
				"    <input type=\"submit\" name=\"action\" value=\"Administration\">\r\n" + //
				"    <input type=\"submit\" name=\"action\" value=\"Sign out\">\r\n" + //
				"</form>\r\n" + //

				"</body>\r\n" + //
				"</html>\r\n" + //
				"";
		return html;
	}

	/**
	 * @return HTML code for displaying the main screen for basic users
	 */
	public String getUserMainScreenForm() {
		String html = CssStyleForPage();
		html += "<p>Welcome to ePUSS+ bar menu</p>";
		html += "<form action=\"BasicUserMainScreenServlet\" method=\"get\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Time\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Project groups\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Sign out\">";

		html += "</form>";
		return html;
	}

	/**
	 * @return HTML code for displaying the main screen for project leaders
	 */
	public String getPlMainScreenForm() {
		String html = CssStyleForPage();
		html += "<p>Welcome to ePUSS+ bar menu</p>";
		html += "<form action=\"PLMainScreenServlet\" method=\"get\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Time\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Project groups\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Statistics\">";

		html += "<input type=\"submit\" name=\"action\" value=\"Sign out\">";

		html += "</form>";
		return html;
	}

	public String CssStyleForPage() {
		return "<head>\r\n" + //
				"    <meta charset=\"UTF-8\">\r\n" + //
				"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
				"    <title>ePuss+</title>\r\n" + //
				"    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/pure/2.0.3/pure-min.css\">\r\n"
				+ //
				"    <style>\r\n" + //
				"        body {\r\n" + //
				"            font-family: Arial, sans-serif;\r\n" + //
				"            background-color: #f2f2f2;\r\n" + //
				"            margin: 0;\r\n" + //
				"            padding: 0;\r\n" + //
				"        }\r\n" + //
				"        .toolbar {\r\n" + //
				"            background-color: #007bff;\r\n" + //
				"            color: #fff;\r\n" + //
				"            padding: 10px 0;\r\n" + //
				"            text-align: center;\r\n" + //
				"        }\r\n" + //
				"        .toolbar ul {\r\n" + //
				"            list-style-type: none;\r\n" + //
				"            padding: 0;\r\n" + //
				"        }\r\n" + //
				"        .toolbar ul li {\r\n" + //
				"            display: inline;\r\n" + //
				"            margin-right: 20px;\r\n" + //
				"        }\r\n" + //
				"        .toolbar ul li a {\r\n" + //
				"            color: #fff;\r\n" + //
				"            text-decoration: none;\r\n" + //
				"        }\r\n" + //
				"        h1 {\r\n" + //
				"            text-align: center;\r\n" + //
				"            margin-top: 30px;\r\n" + //
				"            color: #333;\r\n" + //
				"        }\r\n" + //
				"        p {\r\n" + //
				"            text-align: center;\r\n" + //
				"            margin-top: 20px;\r\n" + //
				"            color: #555;\r\n" + //
				"        }\r\n" + //
				"        form {\r\n" + //
				"            text-align: center;\r\n" + //
				"            margin-top: 20px;\r\n" + //
				"        }\r\n" + //
				"        input[type=\"submit\"] {\r\n" + //
				"            background-color: #007bff;\r\n" + //
				"            color: #fff;\r\n" + //
				"            border: none;\r\n" + //
				"            padding: 10px 20px;\r\n" + //
				"            border-radius: 5px;\r\n" + //
				"            cursor: pointer;\r\n" + //
				"            margin-right: 10px;\r\n" + //
				"        }\r\n" + //
				"        input[type=\"submit\"]:hover {\r\n" + //
				"            background-color: #0056b3;\r\n" + //
				"        }\r\n" + //
				"    </style>\r\n" + //
				"\r\n" + //
				"<script src=\"https://code.jquery.com/jquery-3.6.0.min.js\"></script>\r\n" + //
				"<script>\r\n" + //
				"    // Example jQuery code (you can add more as needed)\r\n" + //
				"    $(document).ready(function() {\r\n" + //
				"        // Add some effects on hover\r\n" + //
				"        $('input[type=\"submit\"]').hover(function() {\r\n" + //
				"            $(this).css('background-color', '#0056b3');\r\n" + //
				"        }, function() {\r\n" + //
				"            $(this).css('background-color', '#007bff');\r\n" + //
				"        });\r\n" + //
				"    });\r\n" + //
				"</script>\r\n" + //
				"</head>\r\n";
	}
}
