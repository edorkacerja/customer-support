package com.wrox;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(
        name = "ticketServlet",
        urlPatterns = {"/tickets"},
        loadOnStartup = 1
)
@MultipartConfig(
        fileSizeThreshold = 5_242_880, //5MB
        maxFileSize = 20_971_520L, //20MB
        maxRequestSize = 41_943_040L //40MB
)
public class TicketServlet extends HttpServlet {
    // volatile id in order to store all the Tickets in the database with different IDs.
    private volatile int TICKET_ID_SEQUENCE = 1;

    private Map<Integer, Ticket> ticketDatabase = new LinkedHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // checks if a user is logged in or not
        if (request.getSession().getAttribute("username") == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null)
            action = "list";
        switch (action) {
            case "create":
                this.showTicketForm(request, response);
                break;
            case "view":
                this.viewTicket(request, response);
                break;
            case "download":
                this.downloadAttachment(request, response);
                break;
            case "list":
            default:
                this.listTickets(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // checks if a user is logged in or not
        if (request.getSession().getAttribute("username") == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null)
            action = "list";
        switch (action) {
            case "create":
                this.createTicket(request, response);
                break;
            case "list":
            default:
                response.sendRedirect("tickets");
                break;
        }
    }

    private void showTicketForm(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException {
        // forward request to ticketForm (it is not a redirect)
        request.getRequestDispatcher("/WEB-INF/jsp/view/ticketForm.jsp")
                .forward(request, response);
    }

    private void viewTicket(HttpServletRequest request,
                            HttpServletResponse response)
            throws ServletException, IOException {
        String idString = request.getParameter("ticketId");

        // the reason behind response being the second argument is in case of a failure of the method getTicket, to send a redirect.
        Ticket ticket = this.getTicket(idString, response);
        if (ticket == null)
            return;

        request.setAttribute("ticketId", idString);
        request.setAttribute("ticket", ticket);

        request.getRequestDispatcher("/WEB-INF/jsp/view/viewTicket.jsp")
                .forward(request, response);
    }

    private void downloadAttachment(HttpServletRequest request,
                                    HttpServletResponse response)
            throws ServletException, IOException {
        String idString = request.getParameter("ticketId");
        Ticket ticket = this.getTicket(idString, response);
        if (ticket == null)
            return;

        String name = request.getParameter("attachment");
        if (name == null) {
            response.sendRedirect("tickets?action=view&ticketId=" + idString);
            return;
        }

        Attachment attachment = ticket.getAttachment(name);
        if (attachment == null) {
            response.sendRedirect("tickets?action=view&ticketId=" + idString);
            return;
        }

        // this block of code is the way to download an Attachment.
        response.setHeader("Content-Disposition",
                "attachment; filename=" + attachment.getName());
        response.setContentType("application/octet-stream");

        ServletOutputStream stream = response.getOutputStream();
        stream.write(attachment.getContents());
    }

    private void listTickets(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {
        // adding attributes to a request (not params)
        request.setAttribute("ticketDatabase", this.ticketDatabase);
        request.setAttribute("edorsAttribute", "sampleAttribute");

        // forwarding the request to a jsp to handle (not redirecting it)
        request.getRequestDispatcher("/WEB-INF/jsp/view/listTickets.jsp")
                .forward(request, response);
    }

    //the only method which handles the POST ticket so far.
    private void createTicket(HttpServletRequest request,
                              HttpServletResponse response)
            throws ServletException, IOException {
        Ticket ticket = new Ticket();
        ticket.setCustomerName((String) request.getSession().getAttribute("username"));
        ticket.setSubject(request.getParameter("subject"));
        ticket.setBody(request.getParameter("body"));

        Part filePart = request.getPart("file1");
        if (filePart != null && filePart.getSize() > 0) {
            Attachment attachment = this.processAttachment(filePart);
            if (attachment != null)
                ticket.addAttachment(attachment);
        }

        int id;
        synchronized (this) {
            id = this.TICKET_ID_SEQUENCE++;
            this.ticketDatabase.put(id, ticket);
        }

        // this one redirects it to the specified url and adds two query params.
        response.sendRedirect("tickets?action=view&ticketId=" + id);
    }


    // the method which processes a Part into an attachment (name and string of bytes)
    private Attachment processAttachment(Part filePart)
            throws IOException {
        InputStream inputStream = filePart.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read;
        final byte[] bytes = new byte[1024];

        // writes the bytes of this input stream into the "bytes" array
        while ((read = inputStream.read(bytes)) != -1) {
            // writes the bytes of the array "bytes" into this outputStream.
            outputStream.write(bytes, 0, read);
        }

        Attachment attachment = new Attachment();
        attachment.setName(filePart.getSubmittedFileName());
        attachment.setContents(outputStream.toByteArray());

        return attachment;
    }

    private Ticket getTicket(String idString, HttpServletResponse response)
            throws ServletException, IOException {
        if (idString == null || idString.length() == 0) {
            response.sendRedirect("tickets");
            return null;
        }

        try {
            Ticket ticket = this.ticketDatabase.get(Integer.parseInt(idString));
            if (ticket == null) {
                response.sendRedirect("tickets");
                return null;
            }
            return ticket;
        } catch (Exception e) {
            response.sendRedirect("tickets");
            return null;
        }
    }
}
