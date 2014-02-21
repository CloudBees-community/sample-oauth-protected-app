package org.cloudbees.crp.github;

import com.cloudbees.cloud_resource.auth.guice.CloudbeesAuthModule;
import com.cloudbees.cloud_resource.auth.guice.OauthConfig;
import com.cloudbees.cloud_resource.auth.jersey.AuthResourceFilter;
import com.cloudbees.cloud_resource.auth.jersey.CustomSecurityContext;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.util.HashMap;
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

                        params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");

                        serve("/*").with(GuiceContainer.class, params);
                    }
                }
        );
    }
}
