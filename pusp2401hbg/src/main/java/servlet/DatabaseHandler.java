package servlet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import beans.ProjectBean;
import beans.TimeReportBean;
import beans.UserBean;

/*
 * Class for managing the database.
 */
public class DatabaseHandler implements AutoCloseable {

	private static final int PASSWORD_LENGTH = 6;
	// If you have the mysql server on your own computer use "localhost" as server
	// address.
	private static String databaseServerAddress = "pusp.cs.lth.se";
	private static String databaseUser = "pusp2401hbg"; // database login user
	private static String databasePassword = "jkr34pke"; // database login password
	private static String database = "pusp2401hbg"; // the database to use, i.e. default schema
	private Connection conn = null;

	// Private stuff used for listAllProjects()
	// Author André 2024-03-15
	private List<UserBean> allUsers = new ArrayList<>();
	private List<ProjectBean> allProjects = new ArrayList<>();
	private List<TimeReportBean> allTimeReports = new ArrayList<>();
	private List<ProjectBean> allActiveProjects = new ArrayList<>();
	private List<ProjectBean> allArchivedProjects = new ArrayList<>();

	public DatabaseHandler() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + databaseServerAddress + "/" +
					database, databaseUser, databasePassword);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Private helper method for listAllProjects()
	 * Fetches all users from the database and turn them into UserBeans.
	 */
	private void fetchAllUsers() {
		String sql = "SELECT * FROM user";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				allUsers.add(new UserBean(rs.getInt("userID"), rs.getString("userName"),
						rs.getString("email"), rs.getInt("clearanceID")));
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
	}

	/**
	 * Private helper method for listAllProjects()
	 * Fetches all projects from the database and turn them into ProjectBeans.
	 */
	private void fetchAllProjects() {
		String sql = "SELECT * FROM project";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				allProjects.add(new ProjectBean(rs.getInt("projectID"), rs.getString("projectName"),
						null, null, null, null, null, rs.getBoolean("isArchived")));
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
	}

	/**
	 * Private helper method for listAllProjects()
	 * Fetches all time reports from the database and turn them into
	 * TimeReportBeans.
	 */
	private void fetchAllTimeReports() {
		String sql = "SELECT * FROM timereport";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				UserBean user = findUserFromID(rs.getInt("userID"));
				allTimeReports.add(new TimeReportBean(user, rs.getBoolean("isSigned"),
						rs.getTimestamp("startTime"), rs.getTimestamp("stopTime"),
						rs.getInt("activity"), rs.getInt("projectID"), rs.getInt("reportID")));
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
	}

	/**
	 * Private helper method for listAllProjects()
	 * Finds a specific UserBean from its ID.
	 * 
	 * @param userID
	 * @return
	 */
	private UserBean findUserFromList(int userID) {
		for (UserBean user : allUsers) {
			if (user.getUserID() == userID) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Private helper method for listAllProjects()
	 * Finds a specific TimeReportBean by it's ID.
	 * 
	 * @param timeReportID
	 * @return
	 */
	private TimeReportBean findTimeReportFromList(int timeReportID) {
		for (TimeReportBean timeReport : allTimeReports) {
			if (timeReport.getTimeReportID() == timeReportID) {
				return timeReport;
			}
		}
		return null;
	}

	/**
	 * Private helper method for listAllProjects()
	 * Fills a map with UserBeans and their roles in a project.
	 * 
	 * @param project
	 * @return
	 */
	private Map<UserBean, String> fetchProjectMembersWithRoles(ProjectBean project) {
		Map<UserBean, String> projectMembersWithRoles = new HashMap<>();
		String sql = "SELECT * FROM projectmember WHERE projectID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, project.getProjectID());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				UserBean user = findUserFromList(rs.getInt("userID"));
				projectMembersWithRoles.put(user, rs.getString("role"));
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
		return projectMembersWithRoles;
	}

	/**
	 * Private helper method for listAllProjects()
	 * Fills a map with UserBeans and a list of their reported time.
	 * 
	 * @param project
	 * @return
	 */
	private Map<UserBean, List<TimeReportBean>> fetchMembersReportedTime(ProjectBean project) {
		Map<UserBean, List<TimeReportBean>> membersReportedTime = new HashMap<>();
		String sql = "SELECT * FROM timereport WHERE projectID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, project.getProjectID());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				UserBean user = findUserFromList(rs.getInt("userID"));
				TimeReportBean timeReport = findTimeReportFromList(rs.getInt("reportID"));
				if (membersReportedTime.containsKey(user)) {
					membersReportedTime.get(user).add(timeReport);
				} else {
					List<TimeReportBean> timeReports = new ArrayList<>();
					timeReports.add(timeReport);
					membersReportedTime.put(user, timeReports);
				}
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
		return membersReportedTime;
	}

	/**
	 * Author: André Kanakis
	 * 2024-03-15
	 * 4.2.15 listAllProjects() : List<ProjectBean>
	 * - a method that returns a list of all projects.
	 * This utilizes a bunch of private helper methods in order to work properly.
	 * 
	 * @return
	 */
	public List<ProjectBean> listAllProjects() {
		fetchAllUsers();
		fetchAllTimeReports();
		fetchAllProjects();
		for (ProjectBean project : allProjects) {
			project.setProjectMembers(fetchProjectMembersWithRoles(project));
			project.setMembersReportedTime(fetchMembersReportedTime(project));
			Map<UserBean, String> teamMembers = project.getProjectMembers();
			for (UserBean user : teamMembers.keySet()) {
				if (teamMembers.get(user).equals("Project Leader")) {
					project.setProjectLeader(user);
				}
			}
			if (project.getIsProjectArchived()) {
				allArchivedProjects.add(project);
			} else {
				allActiveProjects.add(project);
			}
		}
		return allProjects;
	}

	/**
	 * Method for fetching active projects. Call this method after listAllProjects()
	 * 
	 * @return a list of all active projects
	 */
	public List<ProjectBean> fetchActiveProjects() {
		return allActiveProjects;
	}

	/**
	 * Method for fetching archived projects. Call this method after
	 * listAllProjects()
	 * 
	 * @return a list of all archived projects
	 */
	public List<ProjectBean> fetchArchivedProjects() {
		return allArchivedProjects;
	}

	/**
	 * Albin Olausson, 2024-02-29
	 * 4.2.2 createUser(UserBean user) : int - a method that creates a new user from
	 * the database. Returns the unhashed password
	 * 
	 * @param user
	 * @return password if user was created found"
	 * @throws SQLException
	 */
	public String createUser(UserBean user) throws SQLException {
		String sqlCreateUser = "Insert into user (userName, email, clearanceID) values (?,?,?)";
		String sqlAuthenticate = "Insert into authentication (userID, passwordHash) values (?,?)";
		String sixCharacterPassword = "";
		int latestID = -1;

		conn.setAutoCommit(false);

		try (PreparedStatement psCreateUser = conn.prepareStatement(sqlCreateUser, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement psCreatePassword = conn.prepareStatement(sqlAuthenticate)) {
			psCreateUser.setString(1, user.getUsername());
			psCreateUser.setString(2, user.getEmail());
			psCreateUser.setInt(3, user.getClearanceLevel());

			psCreateUser.executeUpdate();

			ResultSet generatedKey = psCreateUser.getGeneratedKeys();

			if (generatedKey.next()) {
				latestID = generatedKey.getInt(1);

				psCreatePassword.setInt(1, latestID);
				sixCharacterPassword = createPassword();
				psCreatePassword.setString(2, hashPassword(sixCharacterPassword));
				psCreatePassword.executeUpdate();
			}

			conn.commit();

		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
		} finally {
			conn.setAutoCommit(true);
		}
		return sixCharacterPassword;
	}

	/**
	 * Author: Albin Olausson
	 * Creates a random password.
	 * 
	 * @return a randomly chosen password
	 */
	private String createPassword() {
		String result = "";
		Random r = new Random();
		for (int i = 0; i < PASSWORD_LENGTH; i++)
			result += (char) (r.nextInt(26) + 97); // 122-97+1=26
		return result;
	}

	/**
	 * Author: Albin Olausson
	 * 
	 * @param password
	 * @return
	 */
	private static String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes());

			// Convert byte array to a string of hexadecimal values
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Albin Olausson, 2024-02-29
	 * 4.2.5 authenticateUser(String username, String password): int - a method
	 * that checks if user logging information is correct and returns the userID if
	 * authenticated.
	 * 
	 * @param username
	 * @param password
	 * @return the userID if correct credentials, else -1
	 * @throws SQLException
	 */
	public int authenticateUser(String username, String password) throws SQLException {
		String sqlCheckUsername = "SELECT u.userID, a.passwordHash " +
				"FROM user u " +
				"INNER JOIN authentication a ON u.userID = a.userID " +
				"WHERE u.userName = ?";

		try (PreparedStatement psUsername = conn.prepareStatement(sqlCheckUsername)) {
			psUsername.setString(1, username);

			ResultSet rs = psUsername.executeQuery();
			if (rs.next()) {
				String storedHashedPassword = rs.getString("passwordHash");
				// Hash the provided password
				String hashedPassword = hashPassword(password);
				// Compare the hashed passwords
				if (storedHashedPassword.equals(hashedPassword)) {
					return rs.getInt("userID"); // Authentication succeeded, return userID
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1; // Authentication failed
	}

	private void printSqlError(SQLException e) {
		System.out.println("SQLException: " + e.getMessage());
		System.out.println("SQLState: " + e.getSQLState());
		System.out.println("VendorError: " + e.getErrorCode());
		throw new RuntimeException(e);
	}

	public void close() throws SQLException {
		conn.close();
	}

	/**
	 * Alfred Jonasson, 2024-03-05
	 * 4.2.19 deleteUnsignedTimeReport(TimeReportBean timeReport) : int
	 * Deletes an unsigned time report from the database
	 * 
	 * @param timeReport the unsigned time report to remove from the database
	 * @return The timeReportID if deletion was successful or -1 if the time report
	 *         is signed or the action failed
	 */
	public int deleteUnsignedTimeReport(TimeReportBean timeReport) {
		if (!timeReport.getIsSigned()) {
			int timeReportID = timeReport.getTimeReportID();
			String sql = "DELETE FROM timereport WHERE reportID = ? ";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, timeReport.getTimeReportID());
				ps.executeUpdate();
				return timeReportID;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Svante Johansson 6 Mars
	 * 4.2.13 listAllUsersInProject(ProjectBean project): List<UserBean> - a
	 * method
	 * that returns a list of all users in a specific project
	 * 
	 * @param project - the project of which the users work at
	 */
	public List<UserBean> listUsersByProject(ProjectBean project) {
		List<UserBean> uB = new ArrayList<UserBean>();
		String sql = "SELECT * " +
				"FROM user, projectmember" +
				"WHERE user.UserID = projectmember.UserID" +
				"AND projectmember.ProjectID = '?';" +
				"";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, Long.toString(project.getProjectID()));
			ResultSet rs = ps.getResultSet();
			while (rs.next()) {
				uB.add(new UserBean(rs.getInt("userID"), rs.getString("userName"),
						rs.getString("email"), rs.getInt("clearanceID"))); // lastInteraction ska inte vara en
																			// inparameter
			}

			return uB;
		} catch (SQLException e) {
			e.getStackTrace();
			return uB;
		}
	}

	/**
	 * Svante Johansson 6 mars
	 * 4.2.14 listUsersByRole (ProjectBean project, String role) : List<ProjectBean>
	 * - a method that returns a list of all users matching the specified role in
	 * the specified
	 * project
	 * 
	 * @param project - the project u want to get the role from
	 * @param role    - the role that was specified to be sorted by
	 */
	public List<UserBean> listUsersByProjectAndRole(ProjectBean project, String role) {
		List<UserBean> listOfUsersByRole = new ArrayList<UserBean>();
		String sql = "SELECT * " +
				"FROM user, projectmember" +
				"WHERE user.UserID = projectmember.UserID" +
				"AND projectmember.role = '?';" +
				"";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, role);
			ResultSet rs = ps.getResultSet();
			while (rs.next()) {
				listOfUsersByRole.add(new UserBean(rs.getInt("userID"), rs.getString("userName"),
						rs.getString("email"), rs.getInt("clearanceID"))); // lastInteraction ska inte vara en
																			// inparameter
			}

			return listOfUsersByRole;
		} catch (SQLException e) {
			e.getStackTrace();
			return listOfUsersByRole;
		}
	}

	/**
	 * Alex Petrovic, 2024-03-06
	 * 4.2.16 listProjectsByUser(UserBean user) : List<ProjectBean> - a method that
	 * returns a list of all project groups that the specified user is part of.
	 * 
	 * @param user - Object of current active user.
	 * @return List<ProjectBean> - a list with all the projects the current user is
	 *         active in.
	 */
	public List<ProjectBean> listProjectsByUser(UserBean user) {
		List<ProjectBean> listProjectsByUser = new ArrayList<>();

		String sql = "SELECT project.projectName AS projectName, project.projectID AS projectID, project.isArchived " +
				"FROM project, projectmember " +
				"Where project.projectID = projectmember.projectID " +
				"AND projectmember.userID = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, user.getUserID());

			ResultSet rs = ps.executeQuery();
			// System.out.println(rs.getString("projectName"));
			while (rs.next()) {
				Boolean isArchived = rs.getBoolean("isArchived");
				if (!isArchived)
					listProjectsByUser.add(new ProjectBean(
							rs.getInt("projectID"),
							rs.getString("projectName"),
							new Date(0),
							new Date(0),
							new UserBean(),
							new TreeMap<UserBean, String>(),
							new TreeMap<UserBean, List<TimeReportBean>>(), isArchived));
			}
			// System.out.println(listProjectsByUser.size() + "**before return**");
			return listProjectsByUser;
		} catch (SQLException e) {
			e.getStackTrace();
			return listProjectsByUser;
		}
	}

	/**
	 * André Kanakis, 2024-02-29
	 * 4.2.4 deleteUser() : int - a method that deletes a user from the database.
	 * 
	 * @param user
	 * @return userID, returns -1 if the user could not be deleted.
	 */
	public int deleteUser(UserBean user) {
		int userID = user.getUserID();
		String sql = "DELETE FROM user WHERE userID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userID);
			ps.executeUpdate();
			return userID;
		} catch (SQLException e) {
			printSqlError(e);
			return -1;
		}
	}

	/**
	 * André Kanakis, 2024-02-29
	 * 4.2.5 updateClearanceLevelOnUser(UserBean user, int clearanceLevel): boolean
	 * - A method that updates a user’s clearance level
	 * 
	 * @param user
	 * @return true if the clearance level was updated, false otherwise.
	 */
	public boolean updateClearanceLevelOnUser(UserBean user, int clearanceLevel) {
		String sql = "UPDATE user SET clearanceID = ? WHERE userID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, clearanceLevel);
			ps.setInt(2, user.getUserID());
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			printSqlError(e);
			return false;
		}
	}

	/**
	 * Ashraf Alzain, 2024-03-17
	 * 4.2.4 deleteProject() : int - a method that deletes a project from the
	 * database.
	 * 
	 * @param project
	 * @return projectID, returns -1 if the project could not be deleted.
	 */
	public int deleteProject(ProjectBean project) throws SQLException {
		String sql = "delete from project where projectID = ?";
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, project.getProjectID());
			int success = pstmt.executeUpdate();
			conn.commit();
			if (success == 1) {
				return project.getProjectID();
			} else {
				throw new SQLException("operationen mislyckades");
			}
		} catch (SQLException e) {
			conn.rollback();
			printSqlError(e);
			return -1;

		} finally {
			conn.setAutoCommit(true);
		}
	}

	/**
	 * Ashraf Alzain, 2024-03-17
	 * 4.2.4 updateProjectLeader() : int - a method that updates the projectleader
	 * in a Project.
	 * 
	 * @param project, UserBean user
	 * @return projectID, returns -1 if the projectleader was not updated.
	 */

	 public int updateProjectLeader(ProjectBean project, UserBean newProjectLeader) throws SQLException {
		try {
			String sql = "UPDATE user SET clearanceID = ? WHERE clearanceID = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, 3);
				pstmt.setInt(2, 2);
				pstmt.executeUpdate();
			}
	
			String sql2 = "UPDATE user SET clearanceID = ? WHERE userID = ?";
			conn.setAutoCommit(false);
			try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
				pstmt2.setInt(1, 2);
				pstmt2.setInt(2, newProjectLeader.getUserID());
				int success = pstmt2.executeUpdate();
				if (success == 1) {
					conn.commit();
					return project.getProjectID();
				} else {
					throw new SQLException("Uppdateringen misslyckades");
				}
			} catch (SQLException e) {
				conn.rollback();
				printSqlError(e);
				return -1;
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			printSqlError(e);
			return -1;
		}
	}
	

	/**
	 * Tonny Huynh 14 mars
	 * 4.2.3 editUser(UserBean user, String username, String email) : int
	 * method that edits a user's username and email.
	 * 
	 * @param user, username, email
	 * @return Returns the userID or -1 if the user could not be edited
	 * @throws SQLException
	 */
	public int editUser(UserBean user, String username, String email) throws SQLException {
		String updateUser = "UPDATE user SET username = ?, email = ? WHERE username = ? AND email = ?";
	
		try {
			conn.setAutoCommit(false);
	
			try (PreparedStatement statement = conn.prepareStatement(updateUser)) {
				statement.setString(1, username);
				statement.setString(2, email);
				statement.setString(3, user.getUsername());
				statement.setString(4, user.getEmail());
				
				int rowsAffected = statement.executeUpdate();
	
				user.setUsername(username);
				user.setEmail(email);
	
				if (rowsAffected > 0) {
					// Return the user's ID or any other relevant identifier
					return user.getUserID();
				} else {
					// Return -1 to indicate that the user could not be edited
					return -1;
				}
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} finally {
			conn.setAutoCommit(true);
		}
	}
	

	/**
	 * Tonny Huynh 14 mars
	 * 4.2.2 createUser(UserBean user) : boolean
	 * methods that creates a new user from the database.
	 * 
	 * @param project
	 * @return true or false whether the user could or could not be created.
	 * @throws SQLException
	 */
	 public boolean createProject(ProjectBean project) throws SQLException {
		String createProject = "INSERT INTO project (projectName) VALUES (?)";
	
		try {
			conn.setAutoCommit(false);
	
			try (PreparedStatement statement = conn.prepareStatement(createProject, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, project.getProjectName());
	
				int rowsAffected = statement.executeUpdate();
				if (rowsAffected > 0) {
					try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							return true;
						}
					}
				}
				return false;
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} finally {
			conn.setAutoCommit(true);
		}
	}
	

	/**
	 * Joachim Mohn, 2024-02-29
	 * 4.2.21 listAllTimeReports(): List<TimeReports> - - a method that returns a
	 * list of all the time reports in the database
	 * 
	 * @return List of all time reports in the database
	 */
	public List<TimeReportBean> listAllTimeReports() {
		List<TimeReportBean> timeReportList = new ArrayList<>();

		String sql = "SELECT * FROM timereport";

		try {
			conn.setAutoCommit(false);

			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					int userID = rs.getInt("userID");
					int projectID = rs.getInt("projectID");
					boolean isSigned = rs.getBoolean("isSigned");
					Timestamp stopTime = rs.getTimestamp("stopTime");
					Timestamp startTime = rs.getTimestamp("startTime");
					int activity = rs.getInt("activity");
					int timeReportID = rs.getInt("reportID");

					TimeReportBean timeReport = new TimeReportBean(findUserFromID(userID), isSigned, startTime,
							stopTime, activity, projectID, timeReportID);
					timeReportList.add(timeReport);
				}
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
				printSqlError(e);
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			printSqlError(e);
		}

		return timeReportList;
	}

	/**
	 * Joachim Mohn, 2024-02-29
	 * 4.2.18 createTimeReport(TimeReportBean timeReport): int - a method that
	 * creates a time report. Returns the reportID and returns -1 if the time report
	 * could not be created.* @param timeReport
	 * 
	 * @return the time reports ID number
	 * @throws SQLException
	 */
	public int createTimeReport(TimeReportBean timeReport) throws SQLException {
		String sql = "INSERT INTO timereport (userID, projectID, stopTime, startTime, activity, isSigned) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			// int userID = findUserId(timeReport.getEmployee().getUsername());
			ps.setInt(1, timeReport.getEmployee().getUserID());
			// ps.setInt(1, timeReport.getEmployee().getUserID()); // Användar-ID för den
			// anställda
			ps.setInt(2, timeReport.getProjectID());

			ps.setTimestamp(3, new Timestamp(timeReport.getStopTime().getTime()));
			ps.setTimestamp(4, new Timestamp(timeReport.getStartTime().getTime()));
			ps.setInt(5, timeReport.getActivity());
			ps.setBoolean(6, timeReport.getIsSigned());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) {
				return -1;
			}

			// Hämta det genererade rapport-ID:et
			try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				} else {
					return -1;
				}
			}
		}
	}

	/**
	 * André Kanakis, 2024-02-29
	 * 4.2.6 requestNewPassword(UserBean user) : int - a method that requests a new
	 * password
	 * 
	 * @param user
	 * @return userID, returns -1 if the user could not request a new password.
	 */
	public int requestNewPassword(UserBean user) {
		// TODO : Add a way to return the password, that isn't hashed and present it to
		// the user. Not implemented. For the Future.
		String newPassword = createPassword();
		String hashedPassword = hashPassword(newPassword);

		String sql = "UPDATE authentication SET passwordHash = ? WHERE userID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, hashedPassword);
			ps.setInt(2, user.getUserID());
			ps.executeUpdate();
			return user.getUserID();
		} catch (SQLException e) {
			printSqlError(e);
			return -1;
		}
	}

	/**
	 * Alfred Jonasson, 2024-03-05
	 * 4.2.22 listTimeReportsByUser(UserBean user) : List<TimeReportBean>
	 * Shows all time reports that a specific employee has submitted
	 *
	 * @param user the employee to show time reports for
	 * @return An ArrayList of time reports in the form of TimeReportBean objects
	 */
	public List<TimeReportBean> listTimeReportsByUser(UserBean user) {
		List<TimeReportBean> timeReports = new ArrayList<>();

		try {
			String sql = "SELECT * FROM timereport WHERE userID = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, user.getUserID());
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						timeReports.add(new TimeReportBean(
								user,
								rs.getBoolean("isSigned"),
								rs.getTimestamp("startTime"),
								rs.getTimestamp("stopTime"),
								rs.getInt("activity"),
								rs.getInt("projectID"),
								rs.getInt("ReportID")));
					}
				}
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
		return timeReports;
	}

	/**
	 * Alfred Jonasson, 2024-03-05
	 * 4.2.23 listTimeReportsByProject(ProjectBean project) : List<TimeReportBean>
	 * 
	 * @param project the project group to show time reports for
	 * @return A list of time reports in the form of TimeReportBean objects
	 */
	public List<TimeReportBean> listTimeReportsByProject(ProjectBean project) {
		List<TimeReportBean> timeReports = new ArrayList<>();
		String sql = "SELECT * FROM timereport WHERE projectID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, project.getProjectID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					System.out.println("Found Time Report: " + rs.getInt("reportID"));
					UserBean user = findUserFromID(rs.getInt("userID"));
					if (user != null) {
						TimeReportBean newTimeReport = new TimeReportBean(
								user,
								rs.getBoolean("isSigned"),
								rs.getTimestamp("startTime"),
								rs.getTimestamp("stopTime"),
								rs.getInt("activity"),
								project.getProjectID(), rs.getInt("reportID"));
						timeReports.add(newTimeReport);
						// System.out.println("New timereport added: " + newTimeReport);
					} else {
						System.out.println("User not found for ID: " + rs.getInt("userID"));
					}
				}
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
		return timeReports;
	}

	/*
	 * private help method used for creating a userBean object to be used i some
	 * methods
	 * // public for now 2024-03-17, Joachim
	 */
	public UserBean findUser(String userName) {

		String sql = "SELECT * FROM user WHERE userName = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			UserBean user = new UserBean(0, "", "", 0); // Det här stämmer inte
			ps.setString(1, userName);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user.setUserID(rs.getInt("userID"));
				user.setUsername(rs.getString("username"));
				user.setEmail(rs.getString("email"));
				user.setClearanceLevel(rs.getInt("clearanceID"));
				// user.setLastInteraction(rs.getTimestamp("lastInteraction"));// finns ej i
				// databasen Lägga till deti databasen?????

				return user;
			}
		} catch (SQLException e) {
			printSqlError(e);
		}

		// Om användar-ID inte kunde hittas returneras null
		return null;
	}

	// private help method (might become depricated soon)
	// helps create a userbean from having the userID
	private UserBean findUserFromID(int userID) {
		String sql = "SELECT * FROM user WHERE userID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			UserBean user = new UserBean(0, "", "", 0); // Det här stämmer inte
			ps.setInt(1, userID);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user.setUserID(rs.getInt("userID"));
				user.setUsername(rs.getString("username"));
				user.setEmail(rs.getString("email"));
				user.setClearanceLevel(rs.getInt("clearanceID"));
				// user.setLastInteraction(rs.getTimestamp("lastInteraction"));// finns ej i
				// databasen Lägga till deti databasen?????
				return user;
			}
		} catch (SQLException e) {
			printSqlError(e);
		}

		// Om userBean inte kunde skapas returneras null
		return null;
	}

	/*
	 * Joachim Mohn, 2024-03-04
	 * 4.2.13 updateUserRole(UserBean user, ProjectBean project): int - a method
	 * that updates a user’s role. Returns the userID or -1 if the user’s role
	 * couldn’t be updated.
	 * 
	 * @param
	 * 
	 * @return the userID or -1 if the update failed
	 */
	public int updateUserRole(int userID, int projectID, String newRole) {
		int result = -1;
		String sql = "UPDATE projectmember SET role = ? WHERE userID = ? AND projectID = ?";
		try {
			conn.setAutoCommit(false);
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, newRole);
				ps.setInt(2, userID);
				ps.setInt(3, projectID);
				int rowsAffected = ps.executeUpdate();
				if (rowsAffected > 0) {
					result = userID;
				}
			} catch (SQLException e) {
				conn.rollback();
				e.printStackTrace();
			} finally {
				conn.commit();
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Tonny Huynh 14 mars
	 * 4.2.8 listAllUsers() : List<UserBean>
	 * 
	 * @return returns a list of all users in the database.
	 * @throws SQLException
	 */
	public List<UserBean> listAllUsers() throws SQLException {
		String selectAllUsers = "SELECT * FROM user";
		ArrayList<UserBean> listOfUsers = new ArrayList<>();
	
		try (PreparedStatement statement = conn.prepareStatement(selectAllUsers);
			 ResultSet results = statement.executeQuery()) {
			
			while (results.next()) {
				String username = results.getString("userName");
				String email = results.getString("email");
				int clearanceID = results.getInt("clearanceID");
				int userID = results.getInt("userID");
	
				UserBean user = new UserBean(userID, username, email, clearanceID);
				listOfUsers.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listOfUsers;
	}
	

	/**
	 * Kristmann Thorsteinsson, 2024-03-16
	 * 4.1.19 signTimeReport(TimeReportBean timeReport) : boolean - a method that
	 * signs a users time-report
	 * 
	 * Metod fixad av Svante 24-03-16
	 * 
	 * @param timeReport
	 * @return returns true if time-report was successfully signed otherwise false.
	 **/
	public boolean signTimeReport(TimeReportBean timeReport) throws SQLException {

		String sql = "UPDATE timereport SET isSigned = ? WHERE reportID = ? ";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(2, timeReport.getTimeReportID());
			if(!timeReport.getIsSigned()){
				ps.setInt(1, 1);
			}else{
				ps.setInt(1, 0);
			}
			
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Kristmann Thorsteinsson, 2024-03-16
	 * 4.1.19 editTimeReport(TimeReportBean timeReport) : boolean - a method that
	 * edits a users time-report
	 *
	 * @param oldTimeReport
	 * @param newTimeReport
	 * @return returns true if time-report was successfully editet otherwise false.
	 **/
	public boolean editTimeReport(TimeReportBean oldTimeReport, TimeReportBean newTimeReport) throws SQLException {
		String updateReportQuery = "UPDATE timereport SET startTime = ?, stopTime = ?, activity = ?, projectID = ? WHERE reportID = ?";

		try (PreparedStatement updateStatement = conn.prepareStatement(updateReportQuery)) {
			// Set parameters for the update query
			updateStatement.setTimestamp(1, newTimeReport.getStartTime());
			updateStatement.setTimestamp(2, newTimeReport.getStopTime());
			updateStatement.setInt(3, newTimeReport.getActivity());
			updateStatement.setInt(4, newTimeReport.getProjectID());

			// Execute the update query
			int updatedRows = updateStatement.executeUpdate();

			// If updatedRows is greater than 0, it means the time report was successfully
			// updated
			if (updatedRows > 0) {
				return true;
			}

		} catch (SQLException e) {
			// Handle SQLException appropriately, log or throw as needed
			e.printStackTrace();
		}

		// Return false if an exception occurred or the report is not updated
		return false;
	}

	/**
	 * Kristmann Thorsteinsson, 2024-03-14
	 * 4.1.21 projectMemberInformation(String projectName) : List<ProjectBean> -a
	 * method that returns a list of project members and their information
	 *
	 *
	 * @return returns a list of all a project members with information including
	 *         their username, role and email.
	 **/
	public List<ProjectBean> projectMemberInformation(int projectId) throws SQLException {

		List<ProjectBean> projectMemberList = new ArrayList<>();

		String queryProjectMember = "SELECT u.username, pm.role, u.email " +
				"FROM projectmember pm " +
				"JOIN project p ON pm.projectID = p.projectID " +
				"JOIN user u ON pm.userID = u.userID " +
				"WHERE p.projectID = ?";

		try (PreparedStatement statement = conn.prepareStatement(queryProjectMember)) {

			statement.setInt(1, projectId);

			// Execute query
			try (ResultSet resultSet = statement.executeQuery()) {
				// Iterate through the result set and create ProjectBean objects
				while (resultSet.next()) {
					ProjectBean projectMember = new ProjectBean();
					UserBean member = new UserBean();

					member.setUsername(resultSet.getString("username"));
					member.setEmail(resultSet.getString("email"));

					// Initialize projectMembers map if it's null
					if (projectMember.getProjectMembers() == null) {
						projectMember.setProjectMembers(new HashMap<>());
					}

					// Put member and role into projectMembers map
					projectMember.getProjectMembers().put(member, resultSet.getString("role"));

					// Add ProjectBean object to the list
					projectMemberList.add(projectMember);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return projectMemberList;
	}

	/**
	 * Kristmann Thorsteinsson, 2024-03-14
	 * 4.1.22 calculateTotalTimeForUserOnProject(ProjectBean project) :
	 * List<Integer>
	 * -a method that returns a list of total time worked on a project by each
	 * project member
	 *
	 * @param project
	 * @return returns a list of all total time worked on project according to each
	 *         project member
	 **/
	public List<Integer> calculateTotalTimeForUserOnProject(ProjectBean project) throws SQLException {
		List<Integer> totalTimes = new ArrayList<>();

		String sqlQuery = "SELECT SUM(TIMESTAMPDIFF(MINUTE, startTime, stopTime)) AS total_time_min " +
				"FROM timereport " +
				"JOIN project ON timereport.projectID = project.projectID " +
				"JOIN projectmember ON timereport.userID = projectmember.userID " +
				"WHERE project.projectID = ? " +
				"GROUP BY projectmember.userID";

		try (PreparedStatement statement = conn.prepareStatement(sqlQuery)) {
			// Set the parameters
			statement.setInt(1, project.getProjectID());
			// Execute the query
			try (ResultSet resultSet = statement.executeQuery()) {
				// Retrieve the total times from the result set
				while (resultSet.next()) {
					int totalTime = resultSet.getInt("total_time_min");
					totalTimes.add(totalTime);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return totalTimes;
	}

	/**
	 * Alfred Jonasson, 2024-03-14
	 * 4.2.24 selectClearanceLevel(String userName) : int
	 *
	 * @param userName the username of the employee to get the clearance level of
	 * @return an int representing the clearance level for the employee with the
	 *         provided username or -1 if the user couldn't be found
	 */
	public int selectClearanceLevel(String userName) {
		int clearanceLevel = -1;
		String sql = "SELECT clearanceID FROM user WHERE userName = ?";		
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userName);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					clearanceLevel = rs.getInt("clearanceID");
					return clearanceLevel;
				}
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
		return -1;
	}
	

	/**
	 * 
	 * Method to add a user to a project.
	 *
	 * @param userID    The ID of the user to add.
	 * @param projectID The ID of the project to which the user will be added.
	 * @param role      The role of the user in the project.
	 */
	public void addUserToProject(int userID, int projectID, String role) {
		try {
			// Create a prepared statement to insert the user into the project.
			String sql = "INSERT INTO projectmember (projectID, userID, role) VALUES (?, ?, ?)";
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setInt(1, projectID);
				statement.setInt(2, userID);
				statement.setString(3, role);
				// Execute the update.
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error adding user to project: " + e.getMessage());
		}
	}

	/**
	 * 
	 * Method to remove a user from a project.
	 *
	 * @param userID    The ID of the user to remove.
	 * @param projectID The ID of the project from which the user will be removed.
	 */
	public void removeUserFromProject(int userID, int projectID) {
		try {
			// Create a prepared statement to delete the user from the project.
			String sql = "DELETE FROM projectmember WHERE userID = ? AND projectID = ?";
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setInt(1, userID);
				statement.setInt(2, projectID);
				// Execute the update.
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error removing user from project: " + e.getMessage());
		}
	}

	/**
	 * Finds the user ID corresponding to the given username in the database.
	 *
	 * @param username the username of the user whose ID is to be found
	 * @return the user ID if found, otherwise -1
	 */
	public int findIDfromUser(String username) {
		String sql = "SELECT * FROM user WHERE userName = ?";
		int userID = -1;
	
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					userID = rs.getInt("userID");
					return userID;
				}
			}
		} catch (SQLException e) {
			printSqlError(e);
		}
		return -1;
	}
	

	/**
	 * Author: Albin Olausson
	 * Method to add roles.
	 *
	 * @return A list of role names.
	 */
	public void addRole(String role) {
		try {
			String sql = "INSERT INTO role (type) VALUES (?)";
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setString(1, role);

				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error adding role: " + e.getMessage());
		}
	}

	/**
	 * Author: Albin Olausson
	 * Method to list all available roles.
	 *
	 * @return A list of role names.
	 */
	public List<String> listRoles() {
		List<String> roles = new ArrayList<>();
		try {
			String sql = "SELECT type FROM role";
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				ResultSet resultSet = statement.executeQuery();
				while (resultSet.next()) {
					roles.add(resultSet.getString("type"));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing roles: " + e.getMessage());
		}
		return roles;
	}

	/**
	 * Author: Albin Olausson
	 * Updates the archived status of a project in the database.
	 *
	 * @param setArchived true if the project is to be archived, false otherwise
	 * @param projectId   the ID of the project to update
	 */

	public void updateProjectArchivedStatus(boolean setArchived, int projectId) {
		String updateQuery = "UPDATE project SET isArchived = ? WHERE projectID = ?";

		try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
			ps.setBoolean(1, setArchived);
			ps.setInt(2, projectId);

			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Author: Albin Olausson
	 * Checks if a project is archived based on its ID.
	 *
	 * @param projectId the ID of the project to check
	 * @return true if the project is archived, false otherwise
	 */
	public boolean isProjectArchived(int projectId) {
		String selectQuery = "SELECT isArchived FROM project WHERE projectID = ?";

		try (PreparedStatement ps = conn.prepareStatement(selectQuery)) {
			ps.setInt(1, projectId);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getBoolean("isArchived");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // If project not found or in case of exception
	}

	/**
	 * Retrieves the project ID associated with the given project name from the
	 * database.
	 *
	 * @param project The name of the project for which the ID is to be retrieved.
	 * @return The project ID if found, or -1 if not found or an error occurs.
	 */
	public int getProjectID(String project) {
		String sql = "SELECT projectID FROM project WHERE projectName = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, project);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getInt("projectID"); // Returning the projectID if found
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 
	 * @param projectID
	 * @return
	 */
	public List<UserBean> getProjectMembers(int projectID) {
		List<UserBean> uB = new ArrayList<UserBean>();
		String sql = "SELECT * " +
				"FROM  project, user, projectmember " +
				"WHERE projectmember.UserID = user.UserID " +
				"AND project.projectID = projectmember.projectID " +
				"AND project.projectID = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, projectID);
			ResultSet rs = ps.getResultSet();

			if(rs == null ){
				return uB;
			}
			while (rs.next()) {
				uB.add(new UserBean(rs.getInt("userID"), rs.getString("userName"),
						rs.getString("email"), rs.getInt("clearanceID"))); // lastInteraction ska inte vara en
			}

			return uB;
		} catch (SQLException e) {
			e.getStackTrace();
			return uB;
		}

	}
	/**
	 * Checks if a user is already assigned to a project.
	 * @param userId The ID of the user to check.
	 * @param projectId The ID of the project to check.
	 * @return {@code true} if the user is already assigned to the project, {@code false} otherwise.
	 * @throws SQLException If a database error occurs.
	 */
	public boolean isUserInProject(int userId, int projectId) throws SQLException {
		String query = "SELECT COUNT(*) FROM projectmember WHERE userID = ? AND projectID = ?;";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, projectId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int count = rs.getInt(1);
					return count > 0;
				}
			}
		}
		return false;
	}

	public boolean removeRole(String role) {
		String sql = "Delete from role where type = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, role);
			if (ps.executeUpdate() == 1) return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param newRole what the old role should be changed to
	 * @param oldRole what role should be updated
	 * @return true if the role was updated, false otherwise.
	 */
	public boolean updateRole(String newRole, String oldRole){
		String sql = "UPDATE role SET type = ? WHERE type = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, newRole);
			ps.setString(2, oldRole);
			if ( ps.executeUpdate() == 1) return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}