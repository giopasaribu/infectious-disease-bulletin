package com.govtech.infectiousdiseasebulletin.service;

import com.govtech.infectiousdiseasebulletin.data.DiseaseDTO;
import com.govtech.infectiousdiseasebulletin.model.DiseaseRecord;
import com.govtech.infectiousdiseasebulletin.proxy.DiseaseProxy;
import com.govtech.infectiousdiseasebulletin.repository.DiseaseRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // Use Mockito extension only
public class DiseaseServiceTest {

    @Mock
    private DiseaseProxy diseaseProxy;

    @Mock
    private DiseaseRecordRepository diseaseRecordRepository;

    @InjectMocks
    private DiseaseService diseaseService; // Inject mocks into DiseaseService

    @BeforeEach
    public void setup() {
        // Initialize DiseaseService with mocks (done automatically by @InjectMocks)
        diseaseService = new DiseaseService(diseaseProxy, diseaseRecordRepository);
    }

    @Test
    public void testFetchDiseaseData_successfulResponse() {
        // Given
        DiseaseDTO expectedDiseaseDTO = new DiseaseDTO();
        expectedDiseaseDTO.setSuccess(true);

        when(diseaseProxy.fetchDiseaseRecord(any(Map.class)))
                .thenReturn(Optional.of(expectedDiseaseDTO));

        // When
        DiseaseDTO result = diseaseService.fetchDiseaseData("0", "10000");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(diseaseProxy, times(1)).fetchDiseaseRecord(any(Map.class));
    }

    @Test
    public void testFetchDiseaseData_nullResponse() {
        // Given
        when(diseaseProxy.fetchDiseaseRecord(any(Map.class))).thenReturn(Optional.empty());

        // When
        DiseaseDTO result = diseaseService.fetchDiseaseData("0", "10000");

        // Then
        assertNull(result);
        verify(diseaseProxy, times(1)).fetchDiseaseRecord(any(Map.class));
    }

    @Test
    public void testGetProcessedDiseaseData() {
        // Given: Mocking some sample data
        DiseaseRecord record1 = new DiseaseRecord();
        record1.setDiseaseId(1L);
        record1.setDisease("COVID-19");
        record1.setEpiWeek("W01");
        record1.setEpiYear("2022");
        record1.setNumberOfCases(100L);

        DiseaseRecord record2 = new DiseaseRecord();
        record2.setDiseaseId(2L);
        record2.setDisease("COVID-19");
        record2.setEpiWeek("W02");
        record2.setEpiYear("2022");
        record2.setNumberOfCases(150L);

        when(diseaseRecordRepository.streamAll()).thenReturn(Arrays.asList(record1, record2).stream());

        // When
        Map<String, Map<String, List<String>>> result = diseaseService.getProcessedDiseaseData();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("COVID-19"));
        assertEquals(1, result.get("COVID-19").size());
        assertTrue(result.get("COVID-19").containsKey("2022"));
        assertTrue(result.get("COVID-19").get("2022").get(0).contains("W01-W02"));
    }

    @Test
    public void testFetchAllLatestDiseaseData() {
        // Given
        when(diseaseRecordRepository.findMaxDiseaseId()).thenReturn(20060L);

        DiseaseDTO firstResponse = new DiseaseDTO();
        firstResponse.setSuccess(true);
        DiseaseDTO.Result firstResult = new DiseaseDTO.Result();
        DiseaseDTO.Disease disease = new DiseaseDTO.Disease();
        disease.setId(20061L);
        disease.setDisease("COVID-19");
        disease.setEpiWeek("2022-W01");
        disease.setNumberOfCases("100");
        firstResult.setRecords(Collections.singletonList(disease));
        firstResponse.setResult(firstResult);

        DiseaseDTO emptyResponse = new DiseaseDTO();
        emptyResponse.setSuccess(true);
        DiseaseDTO.Result emptyResult = new DiseaseDTO.Result();
        emptyResult.setRecords(Collections.emptyList());
        emptyResponse.setResult(emptyResult);

        when(diseaseProxy.fetchDiseaseRecord(any(Map.class)))
                .thenReturn(Optional.of(firstResponse))  // First call returns data
                .thenReturn(Optional.of(emptyResponse)); // Second call returns empty

        // When
        diseaseService.fetchAllLatestDiseaseData();

        // Then
        verify(diseaseRecordRepository, times(1)).findMaxDiseaseId();
        verify(diseaseRecordRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testInvalidateDiseaseData() {
        // Since invalidateDiseaseData only evicts cache, and we don't have a running cache manager in unit test
        // We can't directly test the cache functionality here.
        // Therefore, we're simply calling the method to ensure there are no exceptions.
        assertDoesNotThrow(() -> diseaseService.invalidateDiseaseData());
    }
}
