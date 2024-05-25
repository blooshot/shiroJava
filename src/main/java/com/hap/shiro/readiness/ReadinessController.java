package com.hap.shiro.readiness;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class ReadinessController {

    @Inject
    ReadinessService readinessService;

    @RequestMapping(value = "/rest/health",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mobiHealth() {
        Map<String, Boolean> readinessStateMap = readinessService.getSystemReadinessStateMap("", null);
        if (!readinessStateMap.get("mobi_readiness_status")) {
            return new ResponseEntity(readinessStateMap, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity(readinessStateMap, HttpStatus.OK);
    }

    @RequestMapping(value = "/rest/health/{endpoint}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mobiHealthEndpoint(@PathVariable(required = true, name = "endpoint") String endpoint, @RequestParam(value = "disable", required = false) Boolean disable) {
        String message;
        HttpStatus httpStatus;
        if (readinessService.getMobiReadinessAndLivenessStatus(endpoint, disable)) {
            message="up";
            httpStatus=HttpStatus.OK;
        } else {
            message="down";
            httpStatus=HttpStatus.SERVICE_UNAVAILABLE;
        }
        return new ResponseEntity(message, httpStatus);
    }
}
