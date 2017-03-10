package com.auth.service.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Setter
@Getter
public class MainConfiguration extends Configuration {
  
    @JsonProperty
    @NotEmpty
    public String mongohost;

    @JsonProperty
    public int mongoport = 27017;
    
    @JsonProperty
    public int timeout;
    
    @JsonProperty
    @NotEmpty
    public String mongodb = "userDB";

     
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
   
    @JsonProperty
    private Map<String, Object> defaultHystrixConfig;    

   
    
    
}
