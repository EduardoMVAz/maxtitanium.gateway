package max.gateway.security;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouterValidator {
    
    @Value("${api.endpoints.open}")
    private List<String> openApiEndpoints;

    public final Predicate<ServerHttpRequest> isSecured = 
        request -> openApiEndpoints
            .stream()
            .noneMatch(uri -> {
                String[] parts = uri.replaceAll("[^a-zA-Z0-9// *]", "").split(" ");
                if (parts.length != 2) return false;
                final String method = parts[0];
                final String path = parts[1];
                final boolean deep = path.endsWith("/**");
                
                return (request.getMethod().toString().equalsIgnoreCase(method) || method.equalsIgnoreCase("ANY")) 
                    && (request.getURI().getPath().equals(path)) || (deep && request.getURI().getPath().startsWith(path.replace("/**", "")));
            });
    
    public final Predicate<ServerHttpRequest> isOpenApi =
        request -> request.getURI().getPath().contains("swagger-ui") || request.getURI().getPath().contains("api-docs");
}
