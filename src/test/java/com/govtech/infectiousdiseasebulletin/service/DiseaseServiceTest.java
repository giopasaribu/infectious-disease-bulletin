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

@ExtendWith(MockitoExtension.class)
public class DiseaseServiceTest {

    @Mock
    private DiseaseProxy diseaseProxy;

    @Mock
    private DiseaseRecordRepository diseaseRecordRepository;

    @InjectMocks
    private DiseaseService diseaseService;

    @BeforeEach
    public void setup() {
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
}
