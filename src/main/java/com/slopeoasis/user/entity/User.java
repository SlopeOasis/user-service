package com.slopeoasis.user.entity;

//to so anotacije
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "clerk_id", unique = true, nullable = false)
    private String clerkId = "";
    private String nickname = "";
    @Column(name = "wallet_address", unique = true, nullable = false)
    private String walletAddress = "";
    private String theme1 = "";
    private String theme2 = "";
    private String theme3 = "";
    // JPA requires a no-arg constructor
    public User() {
    }

    public User(String clerkId) {// konstruktor
        this.clerkId = clerkId;
    }

    //set themes function
    public void setTheme1(String theme) {
        this.theme1 = theme;
    }
    public void setTheme2(String theme) {
        this.theme2 = theme;
    }
    public void setTheme3(String theme) {
        this.theme3 = theme;
    }

    //get themes functions
    public String getTheme1() {
        return theme1;
    }
    public String getTheme2() {
        return theme2;
    }
    public String getTheme3() {
        return theme3;
    }

    //set nickname function
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    //get nickname function
    public String getNickname() {
        return nickname;
    }

    //get wallet address function
    public String getWalletAddress() {
        return walletAddress;
    }

    // set wallet address (used when creating or updating a user)
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    //get id function
    public Integer getId() {
        return id;
    }
    public String getClerkId() {
        return clerkId;
    }

}
