package com.govtech.infectiousdiseasebulletin.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "disease_records")
@Data
public class DiseaseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disease_id")
    private Long diseaseId;

    @Column(name = "epi_week")
    private String epiWeek;

    @Column(name = "epi_year")
    private String epiYear;

    @Column(name = "disease")
    private String disease;

    @Column(name = "number_of_cases")
    private Long numberOfCases;
}
