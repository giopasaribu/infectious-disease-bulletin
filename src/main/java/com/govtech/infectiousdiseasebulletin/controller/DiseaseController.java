package com.govtech.infectiousdiseasebulletin.controller;

import com.govtech.infectiousdiseasebulletin.data.DiseaseDTO;
import com.govtech.infectiousdiseasebulletin.service.DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disease")
public class DiseaseController {

    private final DiseaseService diseaseService;

    @Autowired
    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping("/fetch")
    public ResponseEntity<DiseaseDTO> getDiseaseData() {
        diseaseService.fetchAllLatestDiseaseData();
        return ResponseEntity.ok(null);
    }

    @GetMapping("/fetch-all")
    public ResponseEntity fetchDiseaseData() {
        diseaseService.fetchAllLatestDiseaseDataAsync();
        return ResponseEntity.ok("Data fetching process started successfully.");
    }

    @GetMapping("/get-disease")
    public ResponseEntity<Map<String, Map<String, List<String>>>> getDisease() {
        return ResponseEntity.ok(diseaseService.getProcessedDiseaseData());
    }

}
