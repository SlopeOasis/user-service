package com.slopeoasis.user.entity;

//to so anotacije
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vrednost_x")
public class VrednostX{
    @Id
    final private Integer id = 1;
    private int vrednost;
    public VrednostX() {//konstruktor
        this.vrednost = 5;//default vrednost
    }

    //funkcije za delo z vrednostjo
    public int getXId() {//get id
        return id;
    }

    public int getXVrednost() {//get vrednost
        return vrednost;
    }

    public void setXVrednost(int x) {//set vrednost
        this.vrednost = x;
    }

}
