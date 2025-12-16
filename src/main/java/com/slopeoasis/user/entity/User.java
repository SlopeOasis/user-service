package com.slopeoasis.user.entity;

//to so anotacije
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private Tag theme1;
    @Enumerated(EnumType.STRING)
    private Tag theme2;
    @Enumerated(EnumType.STRING)
    private Tag theme3;

    private String polygonWalletAddress = "";
    private Boolean polygonWalletVerified = false;
    // JPA requires a no-arg constructor
    public User() {
    }

    public User(String clerkId) {// konstruktor
        this.clerkId = clerkId;
    }

    //set themes function
    public void setTheme1(Tag theme) {
        this.theme1 = theme;
    }
    public void setTheme2(Tag theme) {
        this.theme2 = theme;
    }
    public void setTheme3(Tag theme) {
        this.theme3 = theme;
    }

    //get themes functions
    public Tag getTheme1() {
        return theme1;
    }
    public Tag getTheme2() {
        return theme2;
    }
    public Tag getTheme3() {
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

    //get id function
    public Integer getId() {
        return id;
    }
    public String getClerkId() {
        return clerkId;
    }

    //wallet stuff get, set
    public String getPolygonWalletAddress() {
            return polygonWalletAddress;
    }
    public Boolean getPolygonWalletVerified() {
        return polygonWalletVerified;
    }

    public void setPolygonWalletAddress(String polygonWalletAddress) {
        this.polygonWalletAddress = polygonWalletAddress;
    }
    public void setPolygonWalletVerified(Boolean polygonWalletVerified) {
        this.polygonWalletVerified = polygonWalletVerified;
    }

    // Predefined tags/themes (aligned with Posts.Tag)
    public enum Tag {
        ART,
        MUSIC,
        VIDEO,
        CODE,
        TEMPLATE,
        PHOTO,
        MODEL_3D,
        FONT,
        OTHER
    }

}
