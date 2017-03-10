package com.auth.service.configuration;

import com.auth.service.dao.UserDAO;
import com.auth.service.health.MongoHealthCheck;
import com.auth.service.resources.UserResource;
import com.codahale.metrics.MetricRegistry;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.Arrays;

import org.apache.commons.configuration.MapConfiguration;
import org.bson.Document;

public class AuthService extends Application<MainConfiguration> {
  
    public static void main(String[] args) throws Exception {
        new AuthService().run(args);
    }

    @Override
    public void initialize(Bootstrap<MainConfiguration> bootstrap) {
        bootstrap.addBundle(new SwaggerBundle<MainConfiguration>() {
            @Override
            public SwaggerBundleConfiguration getSwaggerBundleConfiguration(MainConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(MainConfiguration configuration, Environment environment) throws Exception {
        MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(100).minConnectionsPerHost(0).threadsAllowedToBlockForConnectionMultiplier(5)
                .connectTimeout(30000).maxWaitTime(120000).maxConnectionIdleTime(0).maxConnectionLifeTime(0).connectTimeout(10000).socketTimeout(0)
                .socketKeepAlive(false).heartbeatFrequency(10000).minHeartbeatFrequency(500).heartbeatConnectTimeout(20000).localThreshold(15)
                .build();
        //Create Mongo instance      
        MongoClient mongo = new MongoClient(Arrays.asList(
                new ServerAddress(configuration.mongohost, configuration.mongoport)),
                options);

        //Add Managed for managing the Mongo instance
        MongoManaged mongoManaged = new MongoManaged(mongo);

        environment.jersey().register(mongoManaged);

        environment.healthChecks().register("mongo", new MongoHealthCheck(mongo));

        // Configuration des commandes Hystrix
        ConfigurationManager.install(new MapConfiguration(configuration.getDefaultHystrixConfig()));

        MetricRegistry metricRegistry = new MetricRegistry();
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixCodaHaleMetricsPublisher(metricRegistry));
        MongoDatabase  db = mongo.getDatabase(configuration.mongodb);

        MongoCollection<Document> userCollection = db.getCollection("user");

        final UserDAO userDAO = new UserDAO(userCollection);

        environment.jersey().register(new UserResource(userDAO));

    }
}
