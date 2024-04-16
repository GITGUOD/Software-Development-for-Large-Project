<%@ page import="java.util.List" %>
<%@ page import="beans.TimeReportBean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>All Time Reports</title>
</head>
<body>
    <h2>All Time Reports</h2>
    <table border="1">
        <thead>
            <tr>
                <th>User ID</th>
                <th>Project ID</th>
                <th>Is Signed</th>
                <th>Start Time</th>
                <th>Stop Time</th>
                <th>Activity</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${timeReportList}" var="timeReport">
                <tr>
                    <td>${timeReport.userID}</td>
                    <td>${timeReport.projectID}</td>
                    <td>${timeReport.signed ? 'Yes' : 'No'}</td>
                    <td>${timeReport.startTime}</td>
                    <td>${timeReport.stopTime}</td>
                    <td>${timeReport.activity}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>
