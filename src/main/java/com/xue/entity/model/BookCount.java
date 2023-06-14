package com.xue.entity.model;


public class BookCount {

    private String create_time;

    private Float income;

    private Float expenditure;

    private Float week_price;


    public Float getWeek_price() {
        return week_price;
    }

    public void setWeek_price(Float week_price) {
        this.week_price = week_price;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public Float getIncome() {
        return income;
    }

    public void setIncome(Float income) {
        this.income = income;
    }

    public Float getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(Float expenditure) {
        this.expenditure = expenditure;
    }

    
}