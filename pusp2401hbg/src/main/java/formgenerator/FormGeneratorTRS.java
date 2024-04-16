package formgenerator;

import java.util.ArrayList;
import java.util.List;

import beans.ProjectBean;
import beans.TimeReportBean;

public class FormGeneratorTRS {

	public String CssStyleForPage() {
		return "<head>\r\n" + //
				"    <meta charset=\"UTF-8\">\r\n" + //
				"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
				"    <title>TimeReporting</title>\r\n" + //
				"    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/pure/2.0.3/pure-min.css\">\r\n"
				+ //
				"    <style>\r\n" + //
				"        body {\r\n" + //
				"            font-family: Arial, sans-serif;\r\n" + //
				"            background-color: #f2f2f2;\r\n" + //
				"            margin: 0;\r\n" + //
				"            padding: 0;\r\n" + //
				"            font-size: 16px;\r\n" + //
				"        }\r\n" + //
				"        form {\r\n" + //
				"            max-width: 400px;\r\n" + //
				"            margin: 50px auto;\r\n" + //
				"            background-color: #fff;\r\n" + //
				"            padding: 20px;\r\n" + //
				"            border-radius: 5px;\r\n" + //
				"            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\r\n" + //
				"        }\r\n" + //
				"        h1 {\r\n" + //
				"            text-align: center;\r\n" + //
				"            color: #333;\r\n" + //
				"        }\r\n" + //
				"        p {\r\n" + //
				"            margin: 10px 0;\r\n" + //
				"            color: #555;\r\n" + //
				"        }\r\n" + //
				"        input[type=\"datetime-local\"],\r\n" + //
				"        select {\r\n" + //
				"            width: 100%;\r\n" + //
				"            padding: 10px;\r\n" + //
				"            margin: 5px 0;\r\n" + //
				"            border: 1px solid #ccc;\r\n" + //
				"            border-radius: 4px;\r\n" + //
				"            box-sizing: border-box;\r\n" + //
				"        }\r\n" + //
				"        input[type=\"submit\"] {\r\n" + //
				"            width: 100%;\r\n" + //
				"            padding: 10px;\r\n" + //
				"            margin: 10px 0;\r\n" + //
				"            background-color: #007bff;\r\n" + //
				"            color: #fff;\r\n" + //
				"            border: none;\r\n" + //
				"            border-radius: 4px;\r\n" + //
				"            cursor: pointer;\r\n" + //
				"        }\r\n" + //
				"        input[type=\"submit\"]:hover {\r\n" + //
				"            background-color: #0056b3;\r\n" + //
				"        }\r\n" + //
				"    </style>\r\n" + //
				"</head>";
	}

	private String formElement(String par) {
		return '"' + par + '"';
	}

	public String newTimeReportForm(List<ProjectBean> projectList) {
		String[][] variables = {
				{ "Start date and time", "startTime" },
				{ "End date and time", "endTime" } };
		String html = "<p>input your worksessionvalues";
		html += "<p> <form name=" + formElement("input2");
		html += " method=" + formElement("get") + "action=" + formElement("TimeReportServlet");
		for (int i = 0; i < 3; i++) {
			if (i < 2) {
				html += "<p> " + variables[i][0];
				html += ": <input type=" + formElement("datetime-local") + "name =" + variables[i][1] + '>';
			} else {
				html += "<p>";
				html += "<label for=" + formElement("activity") + ">Choose an activity:</label>";

				html += "<select id=" + formElement("activity") + "name=" + formElement("activity") + ">";
				int counter = 0;
				for (String p : allActivities()) {
					html += "<option value=" + formElement(String.valueOf(counter)) + ">" + p + "</option>";
					counter++;
				}
			}
			html += "</select>";
		}
		System.out.println(projectList.size());
		html += "<p>";
		html += "<label for=" + formElement("projectID") + ">Choose a project:</label>";
		html += "<select id=" + formElement("projectID") + "name=" + formElement("projectID") + ">";
		for (int i = 0; i < projectList.size(); i++) {
			html += "<option value=" + projectList.get(i).getProjectID() + ">" + projectList.get(i).getProjectName()
					+ "</option>";

			projectList.get(i).getProjectName();
		}
		html += "</select>";
		html += "<p>";
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("RR") + "value="
				+ formElement("Submit") + '>';
		html += "</from><p>";

		html += CssStyleForPage();

		return html;
	}

	public String welcomeToTimeReportForm(int clearance) {
		String html = "Choose a page <p>";
		html += "<form action=" + formElement("TimeReportServlet") + formElement("get");
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("action") + "value="
				+ formElement("New Timereport") + '>';
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("action") + "value="
				+ formElement("Edit Timereport") + '>';
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("action") + "value="
				+ formElement("View Timereports") + '>';
		if (clearance < 3) {
			html += "<p> <input type=" + formElement("submit") + "name=" + formElement("action") + "value="
					+ formElement("Sign Timereports") + '>';
		}

		html += "</form>";

		html += CssStyleForPage();

		return html;
	}

	public String editTimeReportForm(List<TimeReportBean> trb) {
		String html = "<h3>Which timereport would you like to edit? </h3>";
		html += "<form action=" + formElement("TimeReportServlet") + " method=" + formElement("get") + ">";
		html += "<label for=" + formElement("timeReport") + ">Choose a timeReport:</label>";

		html += "<select id=" + formElement("timeReport") + " name=" + formElement("selectedTimeReport") + ">";

		for (TimeReportBean t : trb) {
			html += "<option value='" + t.getTimeReportID() + "|" + t.getActivity() + "|" + t.getProjectID() +
					"|" + t.getIsSigned() + "|" + t.getStartTime().toString() + "|" + t.getStopTime().toString()
					+ "'> from: " +
					t.getStartTime().toString() + " to: " + t.getStopTime().toString() + " activity: " +
					t.getActivity() + "</option>";

		}
		html += "</select>";
		html += "<p>";

		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("rr") + "value="
				+ formElement("edit") + '>';

		html += "</select></form>";

		return html;
	}

	public String editTimeReportFormStage2(TimeReportBean trb, List<ProjectBean> listPBbyUser) {
		String html = "<p>";
		String startTimeAndDate = trb.getStartTime().toString();
		String endTimeAndDate = trb.getStopTime().toString();
		String activity = String.valueOf(trb.getActivity());
		String project = String.valueOf(trb.getProjectID());

		html += "<form>";
		html += "<form action=" + formElement("TimeReportServlet") + " method=" + formElement("get") + ">";
		html += "<label for=" + formElement("startTimeAndDate") + ">start time and date: </label><br>" +
				"<input type=" + formElement("datetime-local") + " id=" + formElement("startTimeAndDate") + " name="
				+ formElement("startTimeAndDate") + " value=" + formElement(startTimeAndDate) + "><br>" +

				"<label for=" + formElement("endTimeAndDate") + ">end time and date:</label><br>" +
				"<input type=" + formElement("datetime-local") + " id=" + formElement("endTimeAndDate") + " name="
				+ formElement("endTimeAndDate") + " value=" + formElement(endTimeAndDate) + ">" +
				"<br>";
		html += "<label for=" + formElement("activity") + ">activity:</label><br>";
		html += "<select id=" + formElement("activity") + " name=" + formElement("activity") + ">";
		// skapar dropdownmenu för aktiviteter
		int count = 0;
		for (String q : allActivities()) {

			html += "<option value=" + formElement(String.valueOf(count)) + ">" + q + "</option>";
			count++;
		}
		html += "</select><br>";

		html += "<label for=" + formElement("project") + ">project:</label><br>";
		html += "<select id=" + formElement("project") + " name=" + formElement("project") + ">";
		// skapar dropdownmenu för projekt som usern är med i
		for (ProjectBean p : listPBbyUser) {
			html += "<option value=" + String.valueOf(p.getProjectID()) + ">" + p.getProjectName() + "</option>";

		}
		html += "</select>";
		html += "<br><input type=" + formElement("submit") + "name=" + formElement("ff") + "value="
				+ formElement("finalize") + ">";

		html += "</form>";

		return html;
	}

	private List<String> allActivities() {
		ArrayList<String> activities = new ArrayList<String>();
		activities.add("SDP");
		activities.add("SRS");
		activities.add("SVVS");
		activities.add("STLDD");
		activities.add("SVVI");
		activities.add("SDDD");
		activities.add("SVVR");
		activities.add("SSD");
		activities.add("Final Report");
		activities.add("Function Test");
		activities.add("System Test");
		activities.add("Regression Test");

		activities.add("Meeting");

		activities.add("Lecture");
		activities.add("Exercise");
		activities.add("Terminal Exercise");
		activities.add("Self-study");
		activities.add("Other");

		return activities;
	}

	public String backButton() {
		String html = "";
		html += "<form action=" + formElement("TimeReportServlet") + formElement("get");
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("bb") + "value="
				+ formElement("TimeReportPage") + '>';

		return html;
	}

	public String refreshButton() {
		String html = "";
		html += "<form action=" + formElement("TimeReportServlet") + formElement("get");
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("RE") + "value="
				+ formElement("Refresh state") + '>';

		return html;
	}

	public String toMainButton() {
		String html = "";
		html += "<form action=" + formElement("TimeReportServlet") + formElement("get");
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("OO") + "value="
				+ formElement("Back to Home") + '>';

		return html;
	}

	public String viewTimeReportsForm(List<TimeReportBean> trb) {
		String html = "<h3>Here is a summary of all your timereports </h3>";
		html += "<style>" +
				"table {" +
				"font-family: arial, sans-serif;" +
				"border-collapse: collapse;" +
				"width: 90%;" +
				"}" +

				"td, th {" +
				"border: 1px solid #dddddd;" +
				"text-align: left;" +
				"padding: 4px;" +
				"}" +

				"tr:nth-child(even) {" +
				"background-color: #dddddd;" +
				"}" +
				"form {\r\n" + //
				"    max-width: 450px;\r\n" + //
				"    margin: 50px auto;\r\n" + //
				"    background-color: #fff;\r\n" + //
				"    padding: 20px;\r\n" + //
				"    border-radius: 5px;\r\n" + //
				"    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\r\n" + //
				"}" +
				"</style>";
		html += "<table>";
		html += "<tr>";
		html += "<th>userID</th>";
		html += "<th>ProjectID</th>";
		html += "<th>startTime</th>";
		html += "<th>stopTime</th>";
		html += "<th>activity</th>";
		html += "<th>isSigned</th>";

		for (TimeReportBean t : trb) {
			html += "<tr>";
			html += "<td>" + t.getEmployee().getUserID() + "</td>";
			html += "<td>" + t.getProjectID() + "</td>";
			html += "<td>" + t.getStartTime() + "</td>";
			html += "<td>" + t.getStopTime() + "</td>";
			html += "<td>" + t.getActivity() + "</td>";
			html += "<td>" + t.getIsSigned() + "</td>";
			html += "</tr>";
		}
		html += "</tr></table>";
		html += backButton();

		return html;
	}

	public String signTimeReportForm(List<ProjectBean> pb) {
		String html = "Which project would you like to sign timereports on? <p>";
		html += "<form action=" + formElement("TimeReportServlet") + " method=" + formElement("get") + ">";
		html += "<label for=" + formElement("projectChoice") + ">Choose a project:</label>";

		html += "<select id=" + formElement("projectChoice") + " name=" + formElement("chosenProject") + ">";

		for (ProjectBean t : pb) {
			html += "<option value=" + String.valueOf(t.getProjectID()) + ">" + t.getProjectName() + "</option>";

		}
		html += "</select>";
		html += "<p>";
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("GG") + "value="
				+ formElement("Choose Project") + '>';

		html += "</select></form>";

		return html;
	}

	public String signTimeReportForm2(List<TimeReportBean> trb) {
		String html = "";
		html += "<form action=" + formElement("TimeReportServlet") + " method=" + formElement("get") + ">";
		html += "<label for=" + formElement("timeReportChoice") + ">Choose a timereport:</label>";

		html += "<select id=" + formElement("timeReportChoice") + " name=" + formElement("chosenTimeReport") + ">";

		for (TimeReportBean t : trb) {
			
				html += "<option value=" + String.valueOf(t.getTimeReportID()) + "|" + t.getIsSigned() + ">" + " startTime: " + t.getStartTime()
						+ "<br> Signed?: " + t.getIsSigned() + "</option>";
			
		}
		html += "</select>";
		html += "<p>";
		html += "<p> <input type=" + formElement("submit") + "name=" + formElement("PP") + "value="
				+ formElement("Sign timeReport/ Unsign timeReport") + '>';

		html += "</select></form>";

		html += CssStyleForPage();
		return html;
	}

}
