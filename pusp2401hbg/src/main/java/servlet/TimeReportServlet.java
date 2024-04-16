package servlet;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import formgenerator.FormGeneratorAdministration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import beans.ProjectBean;
import beans.TimeReportBean;
import beans.UserBean;
import formgenerator.FormGeneratorAdministration;
import formgenerator.FormGeneratorTRS;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Survey.
 */
@WebServlet("/TimeReportServlet")
public class TimeReportServlet extends HttpServlet {
    private FormGeneratorTRS formGeneratorTRS = new FormGeneratorTRS();
    private FormGeneratorAdministration fga = new FormGeneratorAdministration();
    private static final long serialVersionUID = 1L;

    // Define states
    private static final int WELCOME_TO_TIMEREPORT = 0;
    private static final int NEW_TIMEREPORT = 1;
    private static final int EDIT_TIMEREPORT = 2;
    private static final int TIMEREPORT_SUMMARY = 3;
    private static final int SIGN_TIMEREPORT = 4;
    private int state = WELCOME_TO_TIMEREPORT;
    private boolean statechecker = false;
    private int statecheckerEDIT = 0;
    private int statecheckerSIGN = 0;
    private boolean emptyProject = false;
    private boolean emptyTimeReport = false;
    private boolean emptyTimeReport2 = false;

    /**
     * Default constructor.
     */
    public TimeReportServlet() {
    }

    /*
     * Checks if a value entered as answer is OK. Answers should be between 1 and
     * 10.
     */
    boolean valueOk(int value) {
        return value > 0 && value < 11;
    }

    boolean dateAndTimeOk(Date start, Date end) {
        return start.compareTo(end) < 0;
    }

    int totalTimeWorked(Date one, Date two) {
        Calendar c = Calendar.getInstance();
        c.setTime(one);
        int hourOne = c.get(Calendar.HOUR_OF_DAY);
        int minutesOne = c.get(Calendar.HOUR_OF_DAY);
        c.setTime(two);
        int hourTwo = c.get(Calendar.HOUR_OF_DAY);
        int minutesTwo = c.get(Calendar.HOUR_OF_DAY);

        return (hourTwo - hourOne) * 60 + (minutesTwo - minutesOne);
    }
    // TRS BÖR FUNGERA NU -- 2024-03-16 ALPHA/BETA 1.0
    // DET SOM FINNS KVAR ATT ÅTGÄRDA ÄR ATT:
    // 1. HANTERA WEBBLÄSARENS BAKÅTKNAPP (OM STATES ÄNDRAS I FEL ORDNING BLIR DET
    // BUGGAR)
    // 1a. STATES BEHÖVER NOG SES ÖVER NÅGON EXTRA GÅNG FÖR ATT KUNNA VARA TOTALT
    // BUGFRIA

    // 2. USER BÖR HÄMTAS IN FRÅN SESSIONEN, VÄNTAR PÅ ATT DENNA SKA FUNGERA VIA
    // INLOGGNING
    // nuvarande lösningen använder mockupuser. LÖST //Albin

    // 3. NÄR EN PL/ADMIN SKA SIGNA RAPPORTER BEHÖVS FRAMFÖRALLT ATT **
    // LISTALLPROJECTS ** FUNGERAR
    // 3a. ALTERNATIVT: BARA PL SOM SIGNAR OCH DÅ BEHÖVS EN
    // 3a. EN FUNGERADE METOD LISTPROJECTSBYPROJECTLEADER()
    // 3a. DETTA ÄR NOG DEN ENKLASTE LÖSNINGEN ATT IMPLEMENTERA
    // 3a. OM ADMIN SKA MED IN I BILDEN HÄR BLIR DET ÄNNU MER KOD
    // för tillfället ser någon med högre clearance än 1 samtliga projekt denna är
    // med i
    // och kan signa dessa timereports

    // 4. vid edit timereport går det att rapportera in baklängestid
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Try with resources to create a connection, will ensure close is always
        // called.
        try (DatabaseHandler db = new DatabaseHandler()) {

            HttpSession session = request.getSession(true);

            UserBean user = (UserBean) session.getAttribute("user");
            if (user == null) {
                response.sendRedirect("/LoginServlet");
                return; // Add this return statement to stop further execution
            }
            // user = new UserBean(2, "John Doe", "john@example.com", 2);
            boolean sessionShouldBeEnded = false;
            // Get a writer, which will be used to write the next page for the user
            PrintWriter out = response.getWriter();

            // Start the page, print the HTML header and start the body part of the page
            out.println("<html>");
            out.println(fga.headLayout("TimeReporting"));
            out.println(formGeneratorTRS.CssStyleForPage());
            out.println("<body>");
            // Decide which state the session is in
            int clear = user.getClearanceLevel();

            out.println(formGeneratorTRS.toMainButton() + "<p>");
            if (request.getParameter("OO") != null) {

                session.setAttribute("state", WELCOME_TO_TIMEREPORT);
                if (clear == 3) {
                    response.sendRedirect("BasicUserMainScreenServlet");
                } else if (clear == 2) {
                    response.sendRedirect("PLMainScreenServlet");
                } else if (clear == 1) {
                    response.sendRedirect("AdminMainScreenServlet");
                }

            }
            if (emptyProject) {
                out.println("<p>You're not apart of any project<p>");
                emptyProject = false;
            }

            if (emptyTimeReport) {
                out.println("<p>There are no editable timereports<p>");
                emptyTimeReport = false;
            }
            Boolean kk = false;
            if (emptyTimeReport2) {
                out.println("<p>You have signed all the timereports of this project<p>");
                emptyTimeReport2 = false;
                kk = true;
            }

            if (request.getParameter("RE") != null) {
                response.sendRedirect("http://localhost:8888/TimeReportServlet");
                state = WELCOME_TO_TIMEREPORT;
                session.setAttribute("state", state);
            }
            if (session.isNew()) {
                state = WELCOME_TO_TIMEREPORT;
                session.setAttribute("state", state);
            } else if ((Integer) session.getAttribute("state") == NEW_TIMEREPORT) {
                if (statechecker) {
                    state = WELCOME_TO_TIMEREPORT;
                    session.setAttribute("state", state);
                    statechecker = false;
                }
            } else if ((Integer) session.getAttribute("state") == EDIT_TIMEREPORT) {

                if (statecheckerEDIT == 2) {
                    state = WELCOME_TO_TIMEREPORT;
                    session.setAttribute("state", state);
                    statecheckerEDIT = 0;
                }

            } else if ((Integer) session.getAttribute("state") == SIGN_TIMEREPORT) {
                if (statecheckerSIGN == 2) {
                    state = WELCOME_TO_TIMEREPORT;
                    session.setAttribute("state", state);
                    if (kk == false) {
                        out.println("<p> ***signedstatus has been updated *** <br>");
                    }

                    statecheckerSIGN = 0;
                }
            } else {
                state = WELCOME_TO_TIMEREPORT;
                session.setAttribute("state", state);
                state = (Integer) session.getAttribute("state");
            }

            switch (state) {
                case WELCOME_TO_TIMEREPORT:
                    String action = request.getParameter("action"); // Retrieve the value of the "action" parameter
                    if (action != null) {
                        if (action.equals("New Timereport")) {

                            List<ProjectBean> pb = db.listProjectsByUser(user);
                            System.out.println(pb.size());
                            if (!pb.isEmpty()) {
                                out.println(formGeneratorTRS.newTimeReportForm(pb));
                                state = NEW_TIMEREPORT;
                                session.setAttribute("state", state);
                                emptyProject = false;
                            } else {
                                emptyProject = true;

                                response.sendRedirect("/TimeReportServlet");

                            }

                        } else if (action.equals("Edit Timereport")) {

                            List<TimeReportBean> trb = db.listTimeReportsByUser(user);
                            if (!trb.isEmpty()) {
                                state = EDIT_TIMEREPORT;
                                session.setAttribute("state", state);
                                out.println(formGeneratorTRS.editTimeReportForm(trb));
                                emptyTimeReport = false;
                            } else {
                                emptyTimeReport = true;
                                response.sendRedirect("/TimeReportServlet");
                            }

                        } else if (action.equals("View Timereports")) {
                            state = TIMEREPORT_SUMMARY;
                            session.setAttribute("state", state);
                            out.println(formGeneratorTRS.viewTimeReportsForm(db.listTimeReportsByUser(user)));
                        } else if (action.equals("Sign Timereports")) {
                            state = SIGN_TIMEREPORT;
                            session.setAttribute("state", SIGN_TIMEREPORT);
                            // i väntan på att listAllProjects blir klar används listProjectByUser
                            out.println(formGeneratorTRS.signTimeReportForm(db.listAllProjects()));
                        }
                    } else {
                        // No action parameter present, probably because no form has been submitted yet.
                        // Display form.

                        state = WELCOME_TO_TIMEREPORT;
                        session.setAttribute("state", state);
                        out.println(formGeneratorTRS.welcomeToTimeReportForm(user.getClearanceLevel()));
                        out.println("<p> " + formGeneratorTRS.refreshButton());
                    }
                    break;

                case NEW_TIMEREPORT:
                    state = NEW_TIMEREPORT;
                    session.setAttribute("state", state);
                    Date startTimeToDate = new Date(0);
                    Date endTimeToDate = new Date(0);
                    String startTime = request.getParameter("startTime");
                    String endTime = request.getParameter("endTime");
                    String activity = request.getParameter("activity");
                    String projectID = request.getParameter("projectID");

                    if (startTime == null)
                        out.println(formGeneratorTRS.newTimeReportForm(db.listProjectsByUser(user))); // first time
                    else {
                        boolean valuesOk = true;
                        try {
                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
                            startTimeToDate = (Date) formatter.parse(startTime);
                            endTimeToDate = (Date) formatter.parse(endTime);

                        } catch (NumberFormatException | ParseException e) {
                            valuesOk = false;
                        }

                        System.out.println("aaaarå");

                        // display the next page
                        if (dateAndTimeOk(startTimeToDate, endTimeToDate)) {
                            out.println("Theese are your inputted values into the timereport");
                            out.println("<p> start date and time: " + startTime);
                            out.println("<p> end date and time: " + endTime);
                            out.println("<p> activity: " + activity);
                            out.println("<p> project: " + projectID + "<br>");
                            Timestamp startTimeToDateSql = new Timestamp(startTimeToDate.getTime());
                            Timestamp endTimeToDateSql = new Timestamp(endTimeToDate.getTime());
                            System.out.println(activity);
                            TimeReportBean n = new TimeReportBean(user,
                                    false, startTimeToDateSql, endTimeToDateSql, Integer.parseInt(activity),
                                    Integer.parseInt(projectID), 0);
                            db.createTimeReport(n);
                            statechecker = true;

                            out.println(formGeneratorTRS.backButton());
                            // sessionShouldBeEnded = true;
                            if (request.getParameter("bb") != null) {
                                response.sendRedirect("http://localhost:8888/TimeReportServlet");
                                state = WELCOME_TO_TIMEREPORT;
                                session.setAttribute("state", state);

                            }
                        } else {
                            out.println("The values you entered were not OK");
                            out.println(formGeneratorTRS.newTimeReportForm(db.listProjectsByUser(user)));
                        }
                    }
                    break;
                case EDIT_TIMEREPORT:
                    // FUNGERAR SOM TÄNKT MEN FÖR ATT ÅTERKOMMA TILL STARTSIDAN EFTER FÄRDIGGJORD
                    // ÄNDRING
                    // MÅSTE ANVÄNDAREN KLICKA PÅ EDIT EN GÅNG TILL.
                    //
                    //
                    //
                    out.println(formGeneratorTRS.editTimeReportForm(db.listTimeReportsByUser(user)));
                    String editbutton = request.getParameter("rr");
                    if (editbutton != null) {

                        state = EDIT_TIMEREPORT;
                        session.setAttribute("state", state);

                        String selectedTimeReport = request.getParameter("selectedTimeReport");
                        String[] values = selectedTimeReport.split("\\|");
                        int reportID = Integer.valueOf(values[0]);
                        int activityOn = Integer.parseInt(values[1]);
                        int projectIDOn = Integer.parseInt(values[2]);
                        boolean isSigned = Boolean.valueOf(values[3]);
                        Timestamp startTimeOn = Timestamp.valueOf(values[4]);
                        Timestamp endTimeOn = Timestamp.valueOf(values[5]);

                        System.out.println("chosen reportID: " + values[0]);
                        System.out.println("chosen activity: " + values[1]);
                        System.out.println("chosen projectID: " + values[2]);
                        System.out.println("chosen isSigned: " + values[3]);
                        System.out.println("chosen startTime: " + values[4]);
                        System.out.println("chosen endTime: " + values[5]);

                        db.deleteUnsignedTimeReport(new TimeReportBean(null, false,
                                null, null, 0, 0, Integer.parseInt(values[0])));
                        List<ProjectBean> pbbb = db.listProjectsByUser(user);
                        if (pbbb.isEmpty()) {
                            state = WELCOME_TO_TIMEREPORT;
                            session.setAttribute("state", state);
                            response.sendRedirect("/TimeReportServlet");
                            emptyProject = true;
                        } else {
                            out.println(formGeneratorTRS.editTimeReportFormStage2(new TimeReportBean(user, isSigned,
                                    startTimeOn, endTimeOn, activityOn, projectIDOn, reportID), pbbb));
                            statecheckerEDIT = 1;
                            emptyProject = false;
                        }

                    }

                    String finalizeButton = request.getParameter("ff");
                    if (finalizeButton != null) {
                        state = EDIT_TIMEREPORT;
                        session.setAttribute("state", state);

                        // hämta alla values från editTimeReportFormStage2 på samma sätt som innan
                        out.println(
                                "<h3 style=\"font-size: 24px; color: #333; margin-top: 20px; margin-left: 40%;\"> success </h3>");
                        String st = request.getParameter("startTimeAndDate");
                        String et = request.getParameter("endTimeAndDate");

                        int a = Integer.valueOf(request.getParameter("activity"));
                        int p = Integer.valueOf(request.getParameter("project"));
                        Date stt = new Date();
                        Date ett = new Date();
                        try {
                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
                            stt = (Date) formatter.parse(st);
                            ett = (Date) formatter.parse(et);
                        } catch (NumberFormatException | ParseException e) {
                            System.out.print("fel i parsing av date");
                        }

                        Timestamp sttSQL = new Timestamp(stt.getTime());
                        Timestamp ettSQL = new Timestamp(ett.getTime());

                        out.println("<p style=\"font-size: 16px; color: #333; margin-top: 20px; margin-left: 40%;\"> startTime: " + stt.toString() + "</p>");
                        out.println("<p style=\"font-size: 16px; color: #333; margin-top: 20px; margin-left: 40%;\"> endTime: " + ett.toString() + "</p>");
                        out.println("<p style=\"font-size: 16px; color: #333; margin-top: 20px; margin-left: 40%;\"> activity: " + a + "</p>");
                        out.println("<p style=\"font-size: 16px; color: #333; margin-top: 20px; margin-left: 40%;\"> projectID: " + p + "</p>");

                        db.createTimeReport(new TimeReportBean(user, false, sttSQL, ettSQL, a, p, 0));
                        statecheckerEDIT = 2;

                        out.println("<p style=\"font-size: 16px; color: #333; margin-top: 20px; margin-left: 40%;\">" + formGeneratorTRS.backButton() + "</p>");
                    }
                    break;

                case TIMEREPORT_SUMMARY:

                    out.println(formGeneratorTRS.viewTimeReportsForm(db.listTimeReportsByUser(user)));

                case SIGN_TIMEREPORT:
                    // i väntan på att ***listAllProjects*** fungerar används listProjectsByUser
                    //
                    //
                    // *********** OBS!!!! **********
                    // FEL LISTA SOM INPUT NEDAN, SKA VARA SAMTLIGA PROJECT, ELLER SAMTLIGA PROJECT
                    // MED FILTER
                    // INTE ÅTGÄRDAT ÄN, SAKNAS FUNGERANDE METODER I DATABASEHANDLER SE NAMN OVAN

                    // out.println(formGeneratorTRS.signTimeReportForm());

                    String choosebutton = request.getParameter("GG");
                    if (choosebutton != null) {

                        // kod för att visa de timereports som tillhör projektet

                        // hämta projectID som blev valt!!!! sätt in i metod.
                        int project = Integer.parseInt(request.getParameter("chosenProject"));
                        List<TimeReportBean> trb2 = db.listTimeReportsByProject(
                                new ProjectBean(project, "", null, null, null, null, null, false));
                        int signedCounter = 0;
                        for (TimeReportBean t : trb2) {
                            if (t.getIsSigned()) {
                                signedCounter++;
                            }
                        }
                        if ((trb2.size() - signedCounter) > 0) {
                            state = SIGN_TIMEREPORT;
                            session.setAttribute("state", state);
                            emptyTimeReport2 = false;

                            out.println(formGeneratorTRS.signTimeReportForm2(trb2));
                            statecheckerSIGN = 1;

                        } else {
                            emptyTimeReport2 = true;
                            statecheckerSIGN = 2;
                            response.sendRedirect("/TimeReportServlet");
                        }

                    }

                    String chooseTimeReport = request.getParameter("PP");
                    if (chooseTimeReport != null) {
                        state = SIGN_TIMEREPORT;
                        session.setAttribute("state", state);
                        String sid = request.getParameter("chosenTimeReport");
                        String[] valuesSID = sid.split("\\|");
                        int ID = Integer.parseInt(valuesSID[0]);
                        boolean signStatus = Boolean.valueOf(valuesSID[1]);

                        if (sid == null) {
                            state = WELCOME_TO_TIMEREPORT;
                            session.setAttribute("state", state);
                            response.sendRedirect("/TimeReportServlet");

                            break;
                        } else {
                            // id
                            
                            // signed status
                            

                            System.out.println("*** chosen id ***" + ID);
                            System.out.println("*** chosen signStatus " + signStatus);

                            db.signTimeReport(new TimeReportBean(null, signStatus, null, null, 0, 0, ID));
                            statecheckerSIGN = 2;
                            response.sendRedirect("/TimeReportServlet");
                        }

                    }

                    break;
            }

            // Print the end of the HTML-page
            out.println("</body></html>");
            // state = (Integer)session.getAttribute("state");
            // session.setAttribute("state", state);

            if (sessionShouldBeEnded) {
                // session.setAttribute("state", state);
                session.invalidate();
            }
        } catch (SQLException ex) {
            // If Database.close for any reason fails.
            throw new ServletException(ex);
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
