package com.example.diy_simulator;

/**
 * 구매자의 정보를 담는 클래스
 * 이메일, 이름, 전화번호, 주소
 */
public class Customer {

    private String email;
    private String username;
    private String phonenumber;
    private String address;
    private String cart;
    private String account_number;
    private String bank_name;
    private String orderinfo;

    //구매자 생성자
    public Customer(String email, String username, String phonenumber, String address, String account_number, String bank_name) {
        this.email = email;
        this.username = username;
        this.phonenumber = phonenumber;
        this.address = address;
        this.account_number = account_number;
        this.bank_name = bank_name;
        this.cart = "";
        this.orderinfo = "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getOrderinfo() {
        return orderinfo;
    }

    public void setOrderinfo(String orderinfo) {
        this.orderinfo = orderinfo;
    }
}


