package io.github.lyu929.ems.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "division")
public class Division {

    @Id
    private Integer id;

    private String name;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String state;
    private String country;
    private String postalCode;

    protected Division() {}

    public Division(Integer id, String name, String city, String addressLine1, String state, String country,
            String postalCode) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.addressLine1 = addressLine1;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
