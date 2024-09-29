package com.govtech.infectiousdiseasebulletin.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DiseaseDTO implements Serializable {

    @JsonProperty("help")
    private String help;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("result")
    private Result result;

    @Data
    public static class Result {

        @JsonProperty("resource_id")
        private String resourceId;

        @JsonProperty("fields")
        private List<Field> fields;

        @JsonProperty("records")
        private List<Disease> records;

        @JsonProperty("_links")
        private Links links;

        @JsonProperty("total")
        private int total;

        @JsonProperty("limit")
        private int limit;
    }

    @Data
    public static class Field {

        @JsonProperty("type")
        private String type;

        @JsonProperty("id")
        private String id;
    }

    @Data
    public static class Disease {

        @JsonProperty("_id")
        private Long id;

        @JsonProperty("epi_week")
        private String epiWeek;

        @JsonProperty("disease")
        private String disease;

        @JsonProperty("no._of_cases")
        private String numberOfCases;
    }

    @Data
    public static class Links {

        @JsonProperty("start")
        private String start;

        @JsonProperty("next")
        private String next;
    }
}

