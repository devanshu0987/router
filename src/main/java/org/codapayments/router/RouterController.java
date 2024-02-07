package org.codapayments.router;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouterController {

    // We should get any request and then try to pass it onto the downstream.
    // We accept any path.
    // We accept any media type
    // TODO: How to accept any Object here. No bar on what you send?
    @PostMapping(value = "/**")
    public String index(@RequestBody String message) {
        return message;
    }
}
