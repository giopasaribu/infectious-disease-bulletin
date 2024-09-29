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

}
