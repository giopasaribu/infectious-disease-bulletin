package com.govtech.infectiousdiseasebulletin.service;

import com.govtech.infectiousdiseasebulletin.data.DiseaseDTO;
import com.govtech.infectiousdiseasebulletin.model.DiseaseRecord;
import com.govtech.infectiousdiseasebulletin.proxy.DiseaseProxy;
import com.govtech.infectiousdiseasebulletin.repository.DiseaseRecordRepository;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DiseaseService {

    private static final Logger LOG = LoggerFactory.getLogger(DiseaseService.class);

    private final DiseaseProxy diseaseProxy;
    private final DiseaseRecordRepository diseaseRecordRepository;

    private final String LIMIT_STRING = "10000";
    private final Long LIMIT = 10000L;

    @Value("${infectious.disease.resource-id}")
    private String resourceId;

    @Autowired
    public DiseaseService(DiseaseProxy diseaseProxy, DiseaseRecordRepository diseaseRecordRepository) {
        this.diseaseProxy = diseaseProxy;
        this.diseaseRecordRepository = diseaseRecordRepository;
    }

    public DiseaseDTO fetchDiseaseData(String offset, String limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("resource_id", resourceId);
        params.put("limit", limit);
        params.put("offset", offset);
        return diseaseProxy.fetchDiseaseRecord(params).orElse(null);
    }

    @Async
    public void fetchAllLatestDiseaseDataAsync() {
        LOG.info("Running on-demand data fetch...");
        fetchAllLatestDiseaseData();
        LOG.info("Running on-demand data fetch... (Finished)");
    }

    @CacheEvict(value = {"diseaseData"}, allEntries = true)
    public void fetchAllLatestDiseaseData() {
        Long maxDiseaseId = diseaseRecordRepository.findMaxDiseaseId();
        Long initialOffset, remaining;
        if (maxDiseaseId == null) {
            initialOffset = 0L;
            remaining = 0L;
        } else {
            initialOffset = (maxDiseaseId / LIMIT) * LIMIT;
            remaining = maxDiseaseId % LIMIT;
        }

        while (true) {
            try {
                // Fetch data from the API using the current offset and limit
                DiseaseDTO response = fetchDiseaseData(initialOffset.toString(), LIMIT_STRING);

                // Break the loop if the response is null or not successful
                if (response == null) {
                    LOG.error("Error: No response from API.");
                    break;
                }

                if (!response.isSuccess()) {
                    LOG.error("Error: API response was not successful. Stopping the data fetch.");
                    break;
                }

                if (response.getResult().getRecords().isEmpty()) {
                    LOG.info("No more records to fetch.");
                    break;
                }

                // Save the fetched data
                Long dataOffset = initialOffset + remaining;
                saveDiseaseData(response.getResult().getRecords(), dataOffset);

                // Increment the offset by 10,000 for the next loop iteration
                initialOffset += LIMIT;

                LOG.info("Fetched and saved data for offset: " + initialOffset);

            } catch (Exception e) {
                // Handle any exceptions thrown during the API call or data processing
                LOG.error("Error occurred while fetching data: " + e.getMessage());
                break; // Exit the loop in case of an exception
            }

        }
    }

    private void saveDiseaseData(List<DiseaseDTO.Disease> diseaseList, Long dataOffset) {
        List<DiseaseRecord> diseaseRecordList = new ArrayList<DiseaseRecord>();
        if (!diseaseList.isEmpty()) {
            diseaseList.forEach(disease -> {
                if (disease.getId() > dataOffset) {
                    DiseaseRecord diseaseRecord = new DiseaseRecord();
                    diseaseRecord.setDiseaseId(disease.getId());
                    diseaseRecord.setDisease(disease.getDisease());
                    if (!StringUtils.isEmpty(disease.getEpiWeek())) {
                        String[] epiData = disease.getEpiWeek().split("-");
                        diseaseRecord.setEpiWeek(epiData[1]);
                        diseaseRecord.setEpiYear(epiData[0]);
                    }
                    diseaseRecord.setNumberOfCases(Long.valueOf(disease.getNumberOfCases()));
                    diseaseRecordList.add(diseaseRecord);
                }
            });
            diseaseRecordRepository.saveAll(diseaseRecordList);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "diseaseData", unless = "#result == null || #result.isEmpty()")
    public Map<String, Map<String, List<String>>> getProcessedDiseaseData() {
        try (Stream<DiseaseRecord> stream = diseaseRecordRepository.streamAll()) {
            // Filter, group, and sort the data
            Map<String, Map<String, List<DiseaseRecord>>> processedData = stream
                    .filter(record -> record.getNumberOfCases() > 0) // Exclude records with zero cases
                    .collect(Collectors.groupingBy(
                            DiseaseRecord::getDisease, // Group by disease name
                            TreeMap::new, // Maintain sorted order by disease name
                            Collectors.groupingBy(
                                    DiseaseRecord::getEpiYear, // Group by epi_year within each disease
                                    TreeMap::new, // Maintain sorted order by epi_year
                                    Collectors.toList()
                            )
                    ));

            // Process each group to generate continuous ranges with case counts
            Map<String, Map<String, List<String>>> result = new TreeMap<>();
            for (String disease : processedData.keySet()) {
                Map<String, List<String>> yearData = new TreeMap<>();
                for (String year : processedData.get(disease).keySet()) {
                    List<DiseaseRecord> records = processedData.get(disease).get(year);
                    List<String> ranges = getContinuousRangesWithCases(records);
                    yearData.put(year, ranges);
                }
                result.put(disease, yearData);
            }

            return result;
        }
    }

    // Updated helper method to find continuous week ranges and calculate total cases
    private List<String> getContinuousRangesWithCases(List<DiseaseRecord> records) {
        // Convert to a list of week numbers and keep a mapping of week to cases
        List<Integer> weekNumbers = records.stream()
                .map(record -> Integer.parseInt(record.getEpiWeek().substring(1))) // Convert "W01" to 1, "W08" to 8, etc.
                .sorted()
                .collect(Collectors.toList());

        Map<Integer, Long> weekToCasesMap = records.stream()
                .collect(Collectors.toMap(
                        record -> Integer.parseInt(record.getEpiWeek().substring(1)), // Key is the week number
                        DiseaseRecord::getNumberOfCases // Value is the number of cases
                ));

        List<String> ranges = new ArrayList<>();
        int start = weekNumbers.get(0);
        int prev = start;
        long totalCases = weekToCasesMap.get(start);

        for (int i = 1; i < weekNumbers.size(); i++) {
            int current = weekNumbers.get(i);
            if (current != prev + 1) { // If there's a gap, process the range
                if (start == prev) {
                    ranges.add("W" + String.format("%02d", start) + "," + totalCases); // Single week with cases
                } else {
                    ranges.add("W" + String.format("%02d", start) + "-W" + String.format("%02d", prev) + "," + totalCases); // Range of weeks with total cases
                }
                start = current;
                totalCases = 0;
            }
            totalCases += weekToCasesMap.get(current); // Accumulate the cases
            prev = current;
        }

        // Add the last range
        if (start == prev) {
            ranges.add("W" + String.format("%02d", start) + "," + totalCases);
        } else {
            ranges.add("W" + String.format("%02d", start) + "-W" + String.format("%02d", prev) + "," + totalCases);
        }

        return ranges;
    }

    @Scheduled(cron = "${data.fetch.cron}", zone = "Asia/Singapore")
    public void runScheduledDataFetch() {
        LOG.info("Running scheduled data fetch...");
        fetchAllLatestDiseaseData();
        LOG.info("Running scheduled data fetch...(Finished)");
    }
}
