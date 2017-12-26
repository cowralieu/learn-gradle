package com.manning.gia.todo.web;

import com.manning.gia.todo.model.ToDoItem;
import com.manning.gia.todo.repository.InMemoryToDoRepository;
import com.manning.gia.todo.repository.ToDoRepository;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToDoServlet extends HttpServlet {

    private static final String FIND_ALL_SERVLET_PATH = "/all";
    private static final String INDEX_PAGE = "/jsp/todo-list.jsp";

    private ToDoRepository repository = new InMemoryToDoRepository();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        String view = processRequest(servletPath, req);
        RequestDispatcher dispatcher = req.getRequestDispatcher(view);
        dispatcher.forward(req, resp);
    }

    private String processRequest(String servletPath, HttpServletRequest req) {

        if (servletPath.equalsIgnoreCase(FIND_ALL_SERVLET_PATH)) {
            List<ToDoItem> toDoItemList = repository.findAll();
            req.setAttribute("toDoItems", toDoItemList);
            req.setAttribute("stats", determineStats(toDoItemList));
            req.setAttribute("filter", "all");
            return INDEX_PAGE;
        } else if (servletPath.equalsIgnoreCase("/active")) {
            List<ToDoItem> toDoItemList = repository.findAll();
            req.setAttribute("toDoItems", filterBasedOnStatus(toDoItemList, false));
            req.setAttribute("stats", determineStats(toDoItemList));
            req.setAttribute("filter", "active");
            return INDEX_PAGE;
        } else if (servletPath.equalsIgnoreCase("/completed")) {
            List<ToDoItem> toDoItemList = repository.findAll();
            req.setAttribute("toDoItems", filterBasedOnStatus(toDoItemList, true));
            req.setAttribute("stats", determineStats(toDoItemList));
            req.setAttribute("filter", "completed");
            return INDEX_PAGE;
        }

        if (servletPath.equals("/insert")) {
            ToDoItem toDoItem = new ToDoItem();
            toDoItem.setName(req.getParameter("name"));
            repository.insert(toDoItem);
            return "/" + req.getParameter("filter");

        } else if (servletPath.equals("/update")) {
            ToDoItem toDoItem = repository.findById(Long.parseLong(req.getParameter("id")));
            if (toDoItem != null) {
                toDoItem.setName(req.getParameter("name"));
                repository.update(toDoItem);
            }
            return "/" + req.getParameter("filter");

        } else if (servletPath.equals("/delete")) {
            ToDoItem toDoItem = repository.findById(Long.parseLong(req.getParameter("id")));
            if (toDoItem != null) {
                repository.delete(toDoItem);
            }
            return "/" + req.getParameter("filter");

        } else if (servletPath.equals("/toggleStatus")) {
            ToDoItem toDoItem = repository.findById(Long.parseLong(req.getParameter("id")));
            if (toDoItem != null) {
                boolean completed = "on".equals(req.getParameter("toggle"));
                toDoItem.setCompleted(completed);
                repository.update(toDoItem);
            }
            return "/" + req.getParameter("filter");

        } else if (servletPath.equals("/clearCompleted")) {
            List<ToDoItem> toDoItems = repository.findAll();
            for (ToDoItem toDoItem : toDoItems) {
                if (toDoItem.isCompleted()) {
                    repository.delete(toDoItem);
                }
            }
            return "/" + req.getParameter("filter");
        }


        return FIND_ALL_SERVLET_PATH;
    }

    private List<ToDoItem> filterBasedOnStatus(List<ToDoItem> toDoItemList, boolean completed) {
        List<ToDoItem> filteredToDoItems = new ArrayList<ToDoItem>();
        for (ToDoItem toDoItem : toDoItemList) {
            if (toDoItem.isCompleted() == completed) {
                filteredToDoItems.add(toDoItem);
            }
        }
        return filteredToDoItems;
    }

    private ToDoListStats determineStats(List<ToDoItem> toDoItemList) {
        ToDoListStats toDoListStats = new ToDoListStats();
        for (ToDoItem toDoItem : toDoItemList) {
            if (toDoItem.isCompleted()) {
                toDoListStats.addCompleted();
            } else {
                toDoListStats.addActive();
            }
        }
        return toDoListStats;
    }

    public static class ToDoListStats {
        private int active;
        private int completed;
        private void addActive() {
            active++;
        }
        private void addCompleted() {
            completed++;
        }
        public int getActive() {
            return active;
        }
        public int getCompleted() {
            return completed;
        }
        public int getAll() {
            return active + completed;
        }
    }


}
