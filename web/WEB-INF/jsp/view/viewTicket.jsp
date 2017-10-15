<%--@elvariable id="ticketId" type="java.lang.String"--%>
<%--@elvariable id="ticket" type="com.wrox.Ticket"--%>s
<!DOCTYPE html>
<html>
    <head>
        <title>Customer Support</title>
    </head>
    <body>
    <a href="<c:url value="/login?logout" />">Logout</a>
        <h2>Ticket #${ticketId}: ${ticket.subject}</h2>
        <i>Customer Name - ${ticket.customerName}</i><br /><br />
        ${ticket.body}<br /><br />

        <c:if test="${ticket.numberOfAttachments>0}">
            Attachments:
            <c:forEach var="attachment" items="${ticket.attachments}" varStatus="status">
                <c:if test="${!status.first}">, </c:if>
                <a href="<c:url value="/tickets">
                        <c:param name="action" value="download" />
                        <c:param name="ticketId" value="${ticketId}" />
                        <c:param name="attachment" value="${attachment.name}" />
                    </c:url>">${attachment.name}</a>
            </c:forEach>

        </c:if>
        <a href="<c:url value="/tickets" />">Return to list tickets</a>

    </body>
</html>
