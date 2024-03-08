package max.gateway;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouterValidator {
    
    private List<String> openApiEndpoints = List.of(
        "/auth/login",
        "/auth/register"
    );

    public final Predicate<ServerHttpRequest> isSecured = 
        request -> openApiEndpoints
            .stream()
            .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
