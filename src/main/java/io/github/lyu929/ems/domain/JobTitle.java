package io.github.lyu929.ems.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_title")
public class JobTitle {

    @Id
    private Integer id;

    private String title;

    protected JobTitle() {}

    public JobTitle(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
