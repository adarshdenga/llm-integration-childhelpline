package com.bdi.agent.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table  (name = "knowledge")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
@Setter
public class Knowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String knowledge;
    @NonNull
    private String subject;
    @NonNull
    private String attribute;

    @ElementCollection
    @CollectionTable(name = "responses", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "responses")
    private List<String> values = new ArrayList<>();
}
