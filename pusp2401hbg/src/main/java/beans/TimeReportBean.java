package beans;

import java.sql.Timestamp;

public class TimeReportBean {
    private UserBean employee;
    private boolean isSigned;
    private Timestamp startTime;
    private Timestamp stopTime;
    private int activity;
    private int timeReportID;
    private int projectID;

    public TimeReportBean(UserBean employee, boolean isSigned, Timestamp startTime, Timestamp stopTime,
            int activity, int projectID, int timeReportID) {
        this.employee = employee;
        this.isSigned = isSigned;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.activity = activity;
        this.projectID = projectID;
        this.timeReportID = timeReportID;
    }

    public TimeReportBean() {
    }

    public UserBean getEmployee() {
        return employee;
    }

    public void setEmployee(UserBean employee) {
        this.employee = employee;
    }

    public boolean getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(boolean isSigned) {
        this.isSigned = isSigned;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStopTime(Timestamp stopTime) {
        this.stopTime = stopTime;
    }

    public Timestamp getStopTime() {
        return stopTime;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getActivity() {
        return activity;
    }

    public int getTimeReportID() {
        return timeReportID;
    }

    public void setTimeReportID(int timeReportID){
        this.timeReportID = timeReportID;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    @Override
    public String toString() {
        return "TimeReportBean{" +
                "employee=" + employee +
                ", isSigned=" + isSigned +
                ", startTime=" + startTime +
                ", stopTime=" + stopTime +
                '}';
    }
}