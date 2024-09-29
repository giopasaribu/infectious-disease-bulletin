package com.govtech.infectiousdiseasebulletin.proxy;

import com.govtech.infectiousdiseasebulletin.data.DiseaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

@FeignClient(name = "diseaseProxy", url ="https://data.gov.sg", contextId = "diseaseProxy")
public interface DiseaseProxy {

    @GetMapping(value="/api/action/datastore_search")
    public Optional<DiseaseDTO> fetchDiseaseRecord(@RequestParam Map<String, Object> queryParams);

}
