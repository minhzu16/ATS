package fptu.sba301.ats.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;

@Data
@Builder
public class SystemHealthDTO {
    private boolean databaseHealthy;
    private String databaseStatus; // HEALTHY, WARNING, CRITICAL
    private long databaseConnections;
    private long databaseQueries;
    
    private boolean applicationHealthy;
    private String applicationStatus; // UP, DEGRADED, DOWN
    private double cpuUsage; // percentage
    private double memoryUsage; // percentage
    private long uptime; // milliseconds
    
    private int errorsLast24Hours;
    private int errorsLast7Days;
    private double errorRate; // percentage
    
    private double p95ResponseTime; // milliseconds
    private double p99ResponseTime; // milliseconds
    private double averageResponseTime; // milliseconds
    private long requestsPerSecond;
    
    private int healthScore; // 0-100
    private Instant lastChecked;
}
