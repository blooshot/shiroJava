package com.hap.shiro.JSRunner;

import org.springframework.stereotype.Service;

@Service
public class Multiplier {

    public String stringScript(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(JSContants.VARIABLE_COMPANY_ID_CONSTANT).append(300).append(JSContants.NEW_LINE_CHARACTER);
        stringBuilder.append(JSContants.VARIABLE_BOUNDARY_EVENT_RESPONSE_NULL_INITIALIZATION_CONSTANT);
        stringBuilder.append(JSContants.RETURNED_OBJECT);
        return stringBuilder.toString();
    }



}
