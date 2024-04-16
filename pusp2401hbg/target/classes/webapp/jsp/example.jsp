<%@ page import="model.HelloJspViewModel" %>
<html>
<body>
<h2>Hello Jsp, simple data from Servlet: <%=request.getAttribute("simple")%></h2>
    <ol>
    <%
        HelloJspViewModel model = (HelloJspViewModel)request.getAttribute("complex");
        for(int el : model.getData()) { %>
            <li>
                <%=el%>
                <% out.print(":" + el); // or as ordinary java code %>
            </li>
        <% } %>
    </ol>  
</body>
</html>
