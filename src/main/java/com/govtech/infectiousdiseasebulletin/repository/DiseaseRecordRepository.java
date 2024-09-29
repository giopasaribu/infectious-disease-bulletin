package com.govtech.infectiousdiseasebulletin.repository;

import com.govtech.infectiousdiseasebulletin.model.DiseaseRecord;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface DiseaseRecordRepository extends JpaRepository<DiseaseRecord, Long> {

    @Query("SELECT MAX(d.diseaseId) FROM DiseaseRecord d")
    Long findMaxDiseaseId();

    @QueryHints(value = @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "1000"))
    @Query("SELECT d FROM DiseaseRecord d ORDER BY d.epiYear ASC, d.disease ASC, d.diseaseId ASC")
    Stream<DiseaseRecord> streamAll();

}
