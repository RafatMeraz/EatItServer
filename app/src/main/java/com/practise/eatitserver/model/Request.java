package com.practise.eatitserver.model;

import java.util.List;

public class Request {
    private String phone, name, address, total, status, userId;
    private List<Order>  foods; //list of food order

    public Request() {
    }

    public Request(String phone, String name, String address, String total, List<Order> foods, String userId) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.foods = foods;
        this.userId = userId;
        this.status = "0";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
