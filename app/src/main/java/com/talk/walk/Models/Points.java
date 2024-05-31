package com.talk.walk.Models;

public class Points {

    private int points = 0;
    private int points_cost = 0;
    private String sku_id;

    Points() {

    }

    public Points(int points, int points_cost, String sku_id) {
        this.points = points;
        this.points_cost = points_cost;
        this.sku_id = sku_id;
    }

    public String getSku_id() {
        return sku_id;
    }

    public void setSku_id(String sku_id) {
        this.sku_id = sku_id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints_cost() {
        return points_cost;
    }

    public void setPoints_cost(int points_cost) {
        this.points_cost = points_cost;
    }
}
