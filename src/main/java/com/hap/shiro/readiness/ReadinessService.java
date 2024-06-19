package com.hap.shiro.readiness;

import com.cronutils.utils.StringUtils;
import com.hap.shiro.users.UserDTO;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.catalina.core.StandardThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReadinessService implements EnvironmentAware {


    @Inject
    private ConnectionFactory connectionFactory;

    @Inject
    private RedisTemplate redisTemplate;

    @Inject
    @Qualifier("dataSource")
    HikariDataSource dataSource;

    private final Logger log = LoggerFactory.getLogger(ReadinessService.class);

    public static boolean isReadinessDisabled = false;
    public static boolean isLivenessDisabled = false;

    private static long heapMemoryLimit;
    private static long activeMaxThreadLimit;
    private static long memoryLimit;
    private static long dbConnectionLimit;

    @Override
    public void setEnvironment(Environment env) {
        heapMemoryLimit = Long.parseLong(env.getProperty("app.HEAP_MEMORY_LIMIT"));
        activeMaxThreadLimit = Long.parseLong(env.getProperty("app.ACTIVE_MAX_THREAD_LIMIT"));
        memoryLimit = Long.parseLong(env.getProperty("app.MEMORY_LIMIT"));
        dbConnectionLimit = Long.parseLong(env.getProperty("app.DB_CONNECTIONS_LIMIT"));
    }

    private Boolean getDbConnectionStatus(){
        try {
            HikariDataSourcePoolMetadata hikariDataSourcePoolMetadata =new HikariDataSourcePoolMetadata(dataSource);
            int activeConnection = hikariDataSourcePoolMetadata.getActive();
            int maxConnection = hikariDataSourcePoolMetadata.getMax();

            log.debug("activeConnection : "+activeConnection+" .maxConnection : "+maxConnection);
            if (activeConnection < ((dbConnectionLimit*maxConnection )/100)) {
                log.debug("ActiveConnections are less than maxConnections");
                return true;
            }
            else{
                log.debug("ActiveConnections limit reached : "+dbConnectionLimit+" %");
            }

        } catch (Exception e) {
            log.error("Got exception while getting data base connection status. Exception : ", e);
        }
        return false;
        }

    /*  JVM heap metrics.*/
    private Boolean getHeapMemoryStatus() {

        try{
            MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean()
                    .getHeapMemoryUsage();

            /*
            The committed size is the amount of memory guaranteed to be available for use by the Java virtual machine.
            The committed memory size is always greater than or equal to the used size.
            */
            long heapCommittedMemory = memoryUsage.getCommitted();

            /*
            The used space is the amount of memory that is currently occupied by Java objects.
            It's always less than or equal to the max size.
            */
            long heapUsedMemmory = memoryUsage.getUsed();

            long heapMaxMemory = memoryUsage.getMax();

            log.debug("max-Heap-Memory : "+heapMaxMemory+" .total-Committed_Heap-Memory : "+heapCommittedMemory+
                    " .used-Heap-Memory : "+heapUsedMemmory);


            if(heapUsedMemmory >= ((heapMemoryLimit * heapCommittedMemory)/100)){
                log.error("heap-memory limit reached : "+heapMemoryLimit +" %");
                return false;
            }
            else{
                log.debug("Heap memory status is healthy");
            }

        }catch (Exception ex){
            log.error("Got exception while getting heap memory status. Exception : ", ex);
            return false;
        }

        return true;
    }

    private Boolean getMemoryStatus() {

        try{

            //For reference https://docs.oracle.com/javase/6/docs/api/java/lang/Runtime.html

            // Returns the maximum amount of memory that the Java virtual machine will attempt to use.
            long maxMemory = Runtime.getRuntime().maxMemory();

            //Returns the total amount of memory in the Java virtual machine.
            long totalMemory=Runtime.getRuntime().totalMemory();

            //Returns the amount of free memory in the Java Virtual Machine
            long freeMemory=Runtime.getRuntime().freeMemory();

            long usedMemory = totalMemory - freeMemory;

            log.debug("max-Memory : "+maxMemory+" .total-Memory : "+totalMemory+" .free-Memory : "+
                    freeMemory+" .used-Memory : "+usedMemory);

            if(usedMemory >= ((memoryLimit * maxMemory)/100)){
                log.error("memory limit reached "+memoryLimit+" %");
                return false;
            }
            else{
                log.debug("Memory status is healthy");
            }

        }catch (Exception ex){
            log.error("Got exception while getting memory status. Exception : ", ex);
            return false;
        }

        return true;
    }

    private Boolean getActiveAndMaxThreadStatus(){
        try {
            StandardThreadExecutor standardThreadExecutor = new StandardThreadExecutor();
            int activeThreadCount =standardThreadExecutor.getActiveCount();
            int maxThreads =standardThreadExecutor.getMaxThreads();
            int maxQueueSize=standardThreadExecutor.getMaxQueueSize();
            int queueSize=standardThreadExecutor.getQueueSize();

            log.error("Active Threads : "+activeThreadCount+" .Maximum Threads : "+maxThreads+" .Queue Size : "
                    +queueSize+" .Maximum  : "+maxQueueSize);

            if(activeThreadCount >= ((activeMaxThreadLimit * maxThreads)/100)){
                log.error("thread limit reached : "+activeMaxThreadLimit +" %");
                return  false;
            }
            else {
                log.debug("Thread status is healthy");
            }
        }catch (Exception ex){
            log.error("Got exception while getting active-max thread status. Exception : ", ex);
            return false;
        }
        return true;
    }

    private Boolean getRabbitmqConnectionStatus(){
        try {
            org.springframework.amqp.rabbit.connection.Connection con = connectionFactory.createConnection();
            if (!con.isOpen())
                throw new Exception("RabbitMQ Connection is closed");
            con.close();
            return true;
        } catch (Exception e) {
            log.error("Mobi RabbitMQ status failed: {}", e);
            return false;
        }
    }

    private Boolean getRedisConnectionStatus(){
        try {
            RedisConnection redisConnection =redisTemplate.getConnectionFactory().getConnection();
            redisConnection.get("testKey".getBytes());
            if (redisConnection.isClosed())
                throw new Exception("Redis Connection is closed");
            redisConnection.close();
            return true;
        } catch (Exception e) {
            log.error("Mobi Redis status failed: {}", e);
            return false;
        }
    }

    private Boolean getMobiReadinessStatus(Map<String, Boolean> systemStateMap, String endpoint, Boolean disable) {
        boolean isEndpointStatus = true;
        boolean isHealthStatusOk = false;

        if (!StringUtils.isEmpty(endpoint)) {
            switch (endpoint) {
                case "readiness":
                    if (disable != null) {
                        isReadinessDisabled=disable;
                    }
                    isEndpointStatus = !isReadinessDisabled;
                    break;
                case "liveness":
                    if (disable != null) {
                        isLivenessDisabled = disable;
                    }
                    isEndpointStatus = !isLivenessDisabled;
                    break;
                default:
            }
        }

        isHealthStatusOk = systemStateMap.get("db_master_connection_status")
                && systemStateMap.get("heap_memory_status")
                && systemStateMap.get("thread_status")
                && systemStateMap.get("redis_connection_status");

        if (!isHealthStatusOk) {
            log.error("Mobi Readiness/Liveliness Status : {}", systemStateMap);
        }

        return isEndpointStatus && isHealthStatusOk;

    }

    public Map<String, Boolean> getSystemReadinessStateMap(String endpoint, Boolean disable) {
        Map<String, Boolean> systemStateMap = new HashMap<>();
        /*if (StringUtils.isEmpty(endpoint)) {
            systemStateMap.put("rabbitmq_connection_status", getRabbitmqConnectionStatus());
        }*/
//        systemStateMap.put("redis_connection_status", getRedisConnectionStatus());
        systemStateMap.put("db_master_connection_status", getDbConnectionStatus());
        systemStateMap.put("heap_memory_status", getHeapMemoryStatus());
        systemStateMap.put("memory_status", getMemoryStatus());
        systemStateMap.put("thread_status", getActiveAndMaxThreadStatus());
        systemStateMap.put("mobi_readiness_status", getMobiReadinessStatus(systemStateMap, endpoint, disable));
        log.debug("Mobi State status:: {}", systemStateMap);
        return systemStateMap;
    }

    public boolean getMobiReadinessAndLivenessStatus(String endpoint, Boolean disable) {
        Map<String, Boolean> readinessStateMap = getSystemReadinessStateMap(endpoint, disable);
        return readinessStateMap.get("mobi_readiness_status");
    }
}
