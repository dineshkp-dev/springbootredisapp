package com.example.sprinsessionredis2.controllers;

import com.example.sprinsessionredis2.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@Slf4j
public class MainController {

    private static ObjectMapper mapper = new ObjectMapper();

    @PostMapping(path = "/getsession")
    public ResponseEntity<String> getSession(HttpSession session) {
        log.info("In session");
        if (session != null) {
            log.info(session.getId());
            Enumeration<String> sessionAttributeNames = session.getAttributeNames();
            session.setAttribute("sessionAttribute", "sessionAttributeVal");
            while (sessionAttributeNames.hasMoreElements()) {
                String s = sessionAttributeNames.nextElement();
                log.info("Attribute: {}", s);
                try {
                    String attributeStr = (String) session.getAttribute(s);
                    log.info("Attribute value: {}", attributeStr);
                } catch (Exception e) {
                    log.error("Error getting attribute value for {}; {}", s, e.getMessage());
                }
            }
        }
        return new ResponseEntity<String>("obtained session: " + session.getId(), HttpStatus.OK);
    }

    @PostMapping(path = "/adduser")
    public ResponseEntity<String> addUser(@RequestBody(required = true) User user, HttpSession session) {
        log.info("Session id: {}", session.getId());
        log.info("User: {}", user);
        String userStr = "";
        userStr = convertToJson(user);
        log.info("User: {}", userStr);
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute == null) {
            session.setAttribute("user", userStr);
        } else {
            log.info("User data: {}", String.valueOf(sessionAttribute));
            return new ResponseEntity<>("user attribute was already added", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("added user", HttpStatus.OK);
    }

    @PostMapping(path = "/updateuser")
    public ResponseEntity<String> updateUser(@RequestBody(required = true) User user, HttpSession session) {
        log.info("Session id: {}", session.getId());
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute == null) {
            return new ResponseEntity<>("user attribute is not present", HttpStatus.BAD_REQUEST);
        } else {
            String userStr = convertToJson(user);
            log.info("User: {}", userStr);
            session.setAttribute("user", userStr);
        }
        return new ResponseEntity<String>("updated user", HttpStatus.OK);
    }

    @GetMapping(path = "/user")
    public ResponseEntity<String> getUser(HttpSession session) {
        log.info("Session id: {}", session.getId());
        Object sessionAttribute = session.getAttribute("user");
        String userStr = "";
        if (sessionAttribute == null) {
            return new ResponseEntity<>("user attribute is not present", HttpStatus.BAD_REQUEST);
        } else {
            userStr = (String) sessionAttribute;
            log.info("User: {}", userStr);
            session.setAttribute("user", userStr);
        }
        return new ResponseEntity<String>(userStr, HttpStatus.OK);
    }

    @DeleteMapping(path = "/user")
    public ResponseEntity<String> deleteUser(HttpSession session) {
        log.info("Session id: {}", session.getId());
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute == null) {
            return new ResponseEntity<>("user attribute is not present", HttpStatus.BAD_REQUEST);
        } else {
            User user = convertToUser((String) sessionAttribute);
            String username = getUserFirstNameLastName(user);
            log.info("User: {} will be removed.", username);
            session.removeAttribute("user");
            log.info("Removed user attribute");
        }
        return new ResponseEntity<String>("removed user", HttpStatus.OK);
    }

    private String getUserFirstNameLastName(User user) {
        String username = "";
        try {
            Map<String, Object> properties = user.getAdditionalProperties();
            LinkedHashMap<String, Object> o = (LinkedHashMap<String, Object>) properties.get("user");
            ArrayList<Object> results = (ArrayList<Object>) o.get("results");
            LinkedHashMap<String, Object> user1 = (LinkedHashMap<String, Object>) results.get(0);
            LinkedHashMap<String, Object> usernameObj = (LinkedHashMap<String, Object>) user1.get("name");
            String firstName = (String) usernameObj.get("first");
            String lastName = (String) usernameObj.get("last");
            username = firstName + lastName;
            log.info("Obtained username: {}", username);
        } catch (Exception e) {
            log.error("Error getting username from User: {}", e.getMessage());
        }
        return username;
    }

    private User convertToUser(String userStr) {
        User user = null;
        if (StringUtils.isBlank(userStr)) {
            return user;
        }
        try {
            user = mapper.readValue(userStr, User.class);
        } catch (JsonProcessingException e) {
            log.error("Could not convert User string: {} to User object; Error: {}", userStr, e.getMessage());
        }
        return user;
    }

    private String convertToJson(User user) {
        String userStr = "";
        try {
            userStr = mapper.writeValueAsString(user);
        } catch (Exception e) {
            log.error("Error converting User to String: {}", e.getMessage());
        }
        return userStr;
    }
}
