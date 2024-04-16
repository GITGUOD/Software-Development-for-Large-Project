package beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectBean {
    private int projectID;
    private String projectName;
    private Date startDate;
    private Date endDate;
    private UserBean projectLeader;
    private boolean isProjectArchived;
    private Map<UserBean, String> projectMembers;
    private Map<UserBean, List<TimeReportBean>> membersReportedTime = new HashMap<>();

    public ProjectBean(int projectID, String projectName, Date startDate, Date endDate, UserBean projectLeader,
            Map<UserBean, String> projectMembers,
            Map<UserBean, List<TimeReportBean>> membersReportedTime, boolean isProjectArchived) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectLeader = projectLeader;
        this.projectMembers = projectMembers;
        this.membersReportedTime = membersReportedTime;
        this.isProjectArchived = isProjectArchived;
    }

    public ProjectBean() {
    }

    /**
     * Returns the projectID of the project
     * 
     * @return the projectID of the project
     */
    public int getProjectID() {
        return projectID;
    }

    public boolean getIsProjectArchived () {
        return isProjectArchived;
    }

    /**
     * Returns the name of the project
     * 
     * @return the name of the project
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the name of the project
     * 
     * @param projectName
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the start date of this project
     * 
     * @return the start date of this project
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of this project
     * 
     * @param startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date of this project
     * 
     * @return the end date of this project
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of this project
     * 
     * @param endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the project leader for this project group
     * 
     * @return the project leader for this project group
     */
    public UserBean getProjectLeader() {
        return projectLeader;
    }

    /**
     * Sets the project leader for this project group
     * 
     * @param projectLeader
     */
    public void setProjectLeader(UserBean projectLeader) {
        this.projectLeader = projectLeader;
    }

    /**
     * Returns the project members and their roles
     * 
     * @return a map with all the users in the project as keys and their roles as
     *         values
     */
    public Map<UserBean, String> getProjectMembers() {
        return projectMembers;
    }

    /**
     * Sets the project members and their roles
     * 
     * @param projectMembers a map with all the users in the project as keys and their roles as
     *         values
     */
    public void setProjectMembers(Map<UserBean, String> projectMembers) {
        this.projectMembers = projectMembers;
    }

    /**
     * Returns the project members and their submitted time reports for this project
     * 
     * @return a map where the project group member is the key and the value is a
     *         list of submitted time reports
     */
    public Map<UserBean, List<TimeReportBean>> getMembersReportedTime() {
        return membersReportedTime;
    }

    /**
     * Sets the project members and their submitted time reports for this project
     * 
     * @param membersReportedTime a map where the project group member is the key
     *                            and the value is a list of submitted time reports
     */
    public void setMembersReportedTime(Map<UserBean, List<TimeReportBean>> membersReportedTime) {
        this.membersReportedTime = membersReportedTime;
    }

    /**
     * Associates a new time report with this project
     * 
     * @param newTimeReport
     * @return the newly created time report or null if the user associated with the
     *         time report doesn't exist
     */
    public TimeReportBean addTimeReport(TimeReportBean newTimeReport) {
        if (newTimeReport == null) {
            return null;
        } else {
            for (UserBean member : membersReportedTime.keySet()) {
                if (member.getUserID() == newTimeReport.getEmployee().getUserID()) {
                    List<TimeReportBean> timeReports = membersReportedTime.get(member);
                    timeReports.add(newTimeReport);
                    membersReportedTime.replace(member, timeReports);
                    return newTimeReport;
                }
            }
            return null;
        }
    }

    /**
     * Calculates the number of minutes reported in this project for the specified
     * work activity
     * 
     * @param workedActivity
     * @return the number of minutes reported in this project for the specified work
     *         activity
     */
    public int getReportedTimeByActivity(int workedActivity) {
        int totalTime = 0;
        List<TimeReportBean> timeReports = listAllTimeReportsByProject();
        for (TimeReportBean timeReport : timeReports) {
            if (timeReport.getActivity() == workedActivity) {
                long timeDifference = timeReport.getStopTime().getTime() - timeReport.getStartTime().getTime();
                timeDifference /= 60000;
                totalTime += (int) timeDifference;
            }
        }
        return totalTime;
    }

    /**
     * Updates a time report
     * 
     * @param oldTimeReport
     * @param newTimeReport
     * @return the updated time report
     */
    public TimeReportBean updateTimeReportBean(TimeReportBean oldTimeReport, TimeReportBean newTimeReport) {
        oldTimeReport = newTimeReport;
        return newTimeReport;
    }

    /**
     * Deletes an unsigned time report from this project
     * 
     * @param timeReport
     * @return the deleted time report or null if it can't be deleted because it's
     *         signed
     */
    public TimeReportBean deleteUnsignedTimeReport(TimeReportBean timeReport) {
        if (!timeReport.getIsSigned()) {
            for (UserBean member : membersReportedTime.keySet()) {
                for (TimeReportBean report : membersReportedTime.get(member)) {
                    if (member.getUserID() == timeReport.getEmployee().getUserID() &&
                            report.getTimeReportID() == timeReport.getTimeReportID()) {
                        List<TimeReportBean> timeReports = membersReportedTime.get(member);
                        timeReports.remove(report);
                        membersReportedTime.replace(member, timeReports);
                        return report;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Signs a time report
     * 
     * @param timeReport
     * @return the signed time report
     */
    public TimeReportBean signTimeReport(TimeReportBean timeReport) {
        if (timeReport == null) {
            return null;
        } else {
            timeReport.setIsSigned(true);
            return timeReport;
        }
    }

    /**
     * Unsigns a time report
     * 
     * @param timeReport
     * @return the unsigned time report
     */
    public TimeReportBean unsignTimeReport(TimeReportBean timeReport) {
        if (timeReport == null) {
            return null;
        } else {
            timeReport.setIsSigned(false);
            return timeReport;
        }
    }

    /**
     * Lists all time reports associated with this project
     * 
     * @return an ArrayList of all time reports associated with this project
     */
    public List<TimeReportBean> listAllTimeReportsByProject() {
        List<TimeReportBean> timeReports = new ArrayList<>();
        if (membersReportedTime != null) {
            for (UserBean employee : membersReportedTime.keySet()) {
                List<TimeReportBean> reports = membersReportedTime.get(employee);
                if (reports != null) {
                    timeReports.addAll(reports);
                }
            }
        }
        return timeReports;
    }

    /**
     * Lists all time reports submitted by a specific user for this project
     * 
     * @param user
     * @return an ArrayList of all time reports that a specific user has submitted
     *         tied to this project
     */
    public List<TimeReportBean> listAllTimeReportsByUser(UserBean user) {
        List<TimeReportBean> timeReports = new ArrayList<>();
        for (UserBean employee : membersReportedTime.keySet()) {
            if (user.getUserID() == employee.getUserID()) {
                timeReports.addAll(membersReportedTime.get(employee));
                return timeReports;
            }
        }
        return timeReports;
    }
}