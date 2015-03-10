/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import DataBaseConnection.Credentials;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.simple.JSONValue;

/**
 *
 * @author Kuldeep
 */
@Path("/Product")
public class Product {

    @GET
    @Produces("application/json")
    public String doGet() {

        String result = resultMethod("SELECT * FROM product");
        return result;

    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public String doGet(@PathParam("id") String id) {
        String result = resultMethod("SELECT * FROM product where ProductID=?", id);
        return result;

    }

    @POST
    @Consumes("application/json")
    public void doPost(String strValue) {
        JsonParser jsonParserObj = Json.createParser(new StringReader(strValue));
        Map<String, String> map = new HashMap<>();
        String name = "", value;
        while (jsonParserObj.hasNext()) {
            JsonParser.Event event = jsonParserObj.next();
            switch (event) {
                case KEY_NAME:
                    name = jsonParserObj.getString();
                    break;
                case VALUE_STRING:
                    value = jsonParserObj.getString();
                    map.put(name, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(jsonParserObj.getInt());
                    map.put(name, value);

            }

        }
        System.out.println(map);
        String getName = map.get("name");
        String getDesc = map.get("description");
        String getQuantity = map.get("quantity");

        doUpdate("INSERT INTO product (name,description,quantity) VALUES (?, ?, ?)", getName, getDesc, getQuantity);
    
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public void doPut(@PathParam("id") String id, String strValue) {
        JsonParser jsonParserObj = Json.createParser(new StringReader(strValue));
        Map<String, String> map = new HashMap<>();
        String name = "", value;
        while (jsonParserObj.hasNext()) {
            JsonParser.Event event = jsonParserObj.next();
            switch (event) {
                case KEY_NAME:
                    name = jsonParserObj.getString();
                    break;
                case VALUE_STRING:
                    value = jsonParserObj.getString();
                    map.put(name, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(jsonParserObj.getInt());
                    map.put(name, value);
                     break;
            }

        }
        System.out.println(map);
        String getName = map.get("name");
        String getDesc = map.get("description");
        String getQuantity = map.get("quantity");
        doUpdate("update product set ProductID = ?, name = ?, description = ?, quantity = ? where ProductID = ?", id, getName, getDesc, getQuantity, id);
    }

    @DELETE
    @Path("{id}")
    @Consumes("application/json")
    public void doDelete(@PathParam("id") String id, String strValue) {
        doUpdate("DELETE FROM `product` WHERE `ProductID`=?", id);
    }

    /**
     * resultMethod accepts two arguments It executes the Query get ProductID,
     * name, description, quantity
     *
     * @param query
     * @param params
     * @throws SQLException
     * @return
     */
    private String resultMethod(String query, String... params) {
        StringBuilder sb = new StringBuilder();
        String jsonString = "";
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            List l1 = new LinkedList();
            while (rs.next()) {
                //Refernce Example 5-2 - Combination of JSON primitives, Map and List
                //https://code.google.com/p/json-simple/wiki/EncodingExamples
                Map m1 = new LinkedHashMap();
                m1.put("ProductID", rs.getInt("ProductID"));
                m1.put("name", rs.getString("name"));
                m1.put("description", rs.getString("description"));
                m1.put("quantity", rs.getInt("quantity"));
                l1.add(m1);

            }

            jsonString = JSONValue.toJSONString(l1);
        } catch (SQLException ex) {
            System.err.println("SQL Exception Error: " + ex.getMessage());
        }
        return jsonString.replace("},", "},\n");
    }

    /**
     * doUpdate Method accepts two arguments Update the entries in the table
     * 'product'
     *
     * @param query
     * @param params
     * @return numChanges
     */
    private int doUpdate(String query, String... params) {
        int numChanges = 0;
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numChanges = pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("SQL EXception in doUpdate Method" + ex.getMessage());
        }
        return numChanges;
    }

}
