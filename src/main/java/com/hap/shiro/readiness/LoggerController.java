package com.hap.shiro.readiness;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.hap.shiro.config.LoggerDTO;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/app/rest/")
public class LoggerController implements EnvironmentAware {

    private Environment env;
    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @RequestMapping(value = "/getlogs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LoggerDTO> getloglist(){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<LoggerDTO> loggers = new ArrayList<>();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            loggers.add(new LoggerDTO(logger));
        }
        return loggers;
    }

    /*
     * LoggerDTOJson = {"name":"com.hap.shiro.readiness.ReadinessService","level":"ERROR"}
     * */
    @RequestMapping(value = "/setlog", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setloglevel(@RequestBody LoggerDTO LoggerDTO){
        LoggerContext logcontext =(LoggerContext) LoggerFactory.getILoggerFactory();
        logcontext.getLogger(LoggerDTO.getName()).setLevel(Level.valueOf(LoggerDTO.getLevel()));
    }
}
