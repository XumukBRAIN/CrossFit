package com.example.AlexFitness.model.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "coach")
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "coach_id_seq", sequenceName = "COACH_ID_SEQ", allocationSize = 1)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "coach")
    private List<Client> clients;

    @OneToMany(mappedBy = "coachId")
    private List<RequestFit> requestFits;

    public Coach() {
    }

    public Coach(Integer id, String name, List<Client> clients, List<RequestFit> requestFits) {
        this.name = name;
        this.clients = clients;
        this.requestFits = requestFits;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<RequestFit> getRequestFits() {
        return requestFits;
    }

    public void setRequestFits(List<RequestFit> requestFits) {
        this.requestFits = requestFits;
    }
}
