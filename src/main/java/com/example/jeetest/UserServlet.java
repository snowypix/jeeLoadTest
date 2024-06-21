package com.example.jeetest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static List<User> users = new ArrayList<>();

    static {
        users.add(new User(1, "John Doe", "john@example.com"));
        users.add(new User(2, "Jane Doe", "jane@example.com"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // Return all users
            out.println(usersToJson(users));
        } else {
            // Return user by ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                int id = Integer.parseInt(pathParts[1]);
                for (User user : users) {
                    if (user.getId() == id) {
                        out.println(user.toString());
                        return;
                    }
                }
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\":\"User not found\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString();
        User newUser = jsonToUser(body);
        if (newUser != null) {
            newUser.setId(users.size() + 1);
            users.add(newUser);
            response.setStatus(HttpServletResponse.SC_CREATED);
            PrintWriter out = response.getWriter();
            out.println(newUser.toString());
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.println("{\"error\":\"Invalid request body\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.split("/").length > 1) {
            int id = Integer.parseInt(pathInfo.split("/")[1]);
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            User updateUser = jsonToUser(body);
            for (User user : users) {
                if (user.getId() == id) {
                    if (updateUser != null) {
                        user.setName(updateUser.getName());
                        user.setEmail(updateUser.getEmail());
                        PrintWriter out = response.getWriter();
                        out.println(user.toString());
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        PrintWriter out = response.getWriter();
                        out.println("{\"error\":\"Invalid request body\"}");
                        return;
                    }
                }
            }
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.println("{\"error\":\"User not found\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.split("/").length > 1) {
            int id = Integer.parseInt(pathInfo.split("/")[1]);
            for (User user : users) {
                if (user.getId() == id) {
                    users.remove(user);
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                }
            }
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.println("{\"error\":\"User not found\"}");
        }
    }

    private String usersToJson(List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < users.size(); i++) {
            sb.append(users.get(i).toString());
            if (i < users.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private User jsonToUser(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
            int id = 0;
            String name = null;
            String email = null;
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue[0].trim().equals("name")) {
                    name = keyValue[1].trim();
                } else if (keyValue[0].trim().equals("email")) {
                    email = keyValue[1].trim();
                }
            }
            return new User(id, name, email);
        } catch (Exception e) {
            return null;
        }
    }
}
