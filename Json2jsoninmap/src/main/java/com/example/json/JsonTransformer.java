package com.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTransformer {

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> recordsList = new ArrayList<>();

        try {
            File inputFile = new File("C:\\Users\\BSIT-021\\Documents\\workspace-spring-tool-suite-4-4.23.1.RELEASE\\JsonTask\\src\\main\\resources\\input.json");
            JsonNode inputJson = objectMapper.readTree(inputFile);

            JsonNode batchRequest = inputJson.path("ServiceRequest").path("BatchRequest").path("ServiceRequest");

            if (!batchRequest.isArray()) {
                System.out.println("BatchRequest->ServiceRequest is not an array");
                return;
            }

            for (JsonNode serviceRequest : batchRequest) {
                JsonNode posting = serviceRequest.path("Posting");

                if (posting.isMissingNode()) {
                    System.out.println("Posting node is missing");
                    continue;
                }

                String accountId = extractField(posting, "AccountNumber");
                String amountValue = extractField(posting, "PostingAmount");
                String currencyCode = extractField(posting, "PostingCcy");

                System.out.println("Extracted Account ID: " + accountId);
                System.out.println("Extracted Amount Value: " + amountValue);
                System.out.println("Extracted Currency Code: " + currencyCode);

                Map<String, String> recordMap = new HashMap<>();
                recordMap.put("AcctId", accountId);
                recordMap.put("amountValue", amountValue);
                recordMap.put("currencyCode", currencyCode);
                recordsList.add(recordMap);
            }

            Map<String, String> hardcodedRecord = new HashMap<>();
            hardcodedRecord.put("AcctId", "HARDCODED_ACCT_ID");
            hardcodedRecord.put("amountValue", "HARDCODED_AMOUNT");
            hardcodedRecord.put("currencyCode", "HARDCODED_CURRENCY");

            recordsList.add(hardcodedRecord);

            Map<String, Object> outputMap = new HashMap<>();
            Map<String, Object> fixmlMap = new HashMap<>();
            Map<String, Object> bodyMap = new HashMap<>();
            Map<String, Object> xferTrnAddRequestMap = new HashMap<>();
            Map<String, Object> xferTrnAddRqMap = new HashMap<>();
            Map<String, Object> xferTrnDetailMap = new HashMap<>();
            List<Map<String, Object>> partTrnRecList = new ArrayList<>();

            for (Map<String, String> recordMap : recordsList) {
                Map<String, Object> partTrnRec = new HashMap<>();
                partTrnRec.put("AcctId", recordMap.get("AcctId"));
                Map<String, String> trnAmt = new HashMap<>();
                trnAmt.put("amountValue", recordMap.get("amountValue"));
                trnAmt.put("currencyCode", recordMap.get("currencyCode"));
                partTrnRec.put("TrnAmt", trnAmt);
                partTrnRecList.add(partTrnRec);
            }

            xferTrnDetailMap.put("PartTrnRec", partTrnRecList);
            xferTrnAddRqMap.put("XferTrnDetail", xferTrnDetailMap);
            xferTrnAddRequestMap.put("XferTrnAddRq", xferTrnAddRqMap);
            bodyMap.put("XferTrnAddRequest", xferTrnAddRequestMap);
            fixmlMap.put("Body", bodyMap);
            outputMap.put("FIXML", fixmlMap);

            JsonNode outputJsonNode = objectMapper.valueToTree(outputMap);

            File outputFile = new File("C:\\Users\\BSIT-021\\Documents\\workspace-spring-tool-suite-4-4.23.1.RELEASE\\JsonTask\\src\\main\\resources\\output.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, outputJsonNode);

            System.out.println("Transformation complete. Output written to output.json");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractField(JsonNode parentNode, String fieldName) {
        JsonNode fieldNode = parentNode.path(fieldName).path("__text");
        if (fieldNode.isMissingNode()) {
            System.out.println(fieldName + " field is missing");
            return "";
        }
        return fieldNode.asText();
    }
}
