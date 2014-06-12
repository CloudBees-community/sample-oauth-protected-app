package org.cloudbees.crp.github;

import com.cloudbees.cloud_resource.auth.guice.CloudbeesAuthModule;
import com.cloudbees.cloud_resource.auth.guice.OauthConfig;
import com.cloudbees.cloud_resource.auth.jersey.AuthResourceFilter;
import com.cloudbees.cloud_resource.auth.jersey.CorsConfig;
import com.cloudbees.cloud_resource.auth.jersey.CorsFilter;
import com.cloudbees.cloud_resource.auth.jersey.CustomSecurityContext;
import com.cloudbees.cloud_resource.auth.jersey.HstsFilter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class Main extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
                new JpaPersistModule("sampleapp"),
                new CloudbeesAuthModule(),
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        //Injects CloudbeesPrincipal using @Principal annotation
                        bind(CustomSecurityContext.class).asEagerSingleton();

                        bind(SampleApp.class);

                        //get your oauth token from init params
                        String clientId = getServletContext().getInitParameter("client_id");
                        String clientSecret = getServletContext().getInitParameter("client_secret");

                        /** Allows injection of config object in to {@link CloudbeesAuthModule} module **/
                        bind(OauthConfig.class).toInstance(new OauthConfig(clientId,clientSecret));

                        filter("/*").through(PersistFilter.class);

                        Map<String, String> params = new HashMap<String, String>();

                        /**
                         * Enable security via {@link AuthResourceFilter}
                         */
                        params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                                AuthResourceFilter.class.getName());


                        /** restrict Cors for *.cloudbees.com domain */
                        params.put(CorsConfig.ALLOW_ORIGIN, "*.cloudbees.com");


                        /** Must disable WADL to avoid wadl crap in the response */
                        params.put(ResourceConfig.FEATURE_DISABLE_WADL, "true");


                        List<String> responseFilters = new ArrayList<>();
                        List<String> requestFilters = new ArrayList<>();

                        responseFilters.add(CorsFilter.class.getName());

                        /** HSTS support */
                        if(!Boolean.getBoolean("local")){ // Don't enable HSTS if running locally
                            requestFilters.add(HstsFilter.class.getName());
                            responseFilters.add(HstsFilter.class.getName());
                        }


                        if(!requestFilters.isEmpty()){
                            params.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, StringUtils.join(requestFilters,","));
                        }

                        if(!responseFilters.isEmpty()) {
                            params.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, StringUtils.join(responseFilters,","));
                        }


                        params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");

                        serve("/*").with(GuiceContainer.class, params);
                    }
                }
        );
    }
}
