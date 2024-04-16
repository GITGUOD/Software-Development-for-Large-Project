package beans;

import java.util.Date;

public class UserBean {
    private int userID;
    private String username;
    private String email;
    private int clearanceLevel;
    private Date lastInteraction;

    public UserBean(int userID, String username, String email, int clearanceLevel) {
        this.username = username;
        this.email = email;
        this.clearanceLevel = clearanceLevel;
        this.userID = userID;
    }

    public UserBean() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public void setClearanceLevel(int clearanceLevel) {
        this.clearanceLevel = clearanceLevel;
    }

    public Date getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(Date lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
