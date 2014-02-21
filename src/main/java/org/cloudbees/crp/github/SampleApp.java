package org.cloudbees.crp.github;

import com.cloudbees.cloud_resource.auth.CloudbeesPrincipal;
import com.cloudbees.cloud_resource.auth.Principal;
import com.cloudbees.cloud_resource.auth.Secure;
import org.apache.commons.collections.map.HashedMap;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Map;

/**
 * Sample OAuth protected app
 *
 *
 * @author Vivek Pandey
 */
@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class SampleApp{
    @Context
    UriInfo uriInfo;


    @Path("/{account}")
    @Secure(scopes={"https://myapp.example.com/read_account_info"})
    @GET
    public Map getAccountDetails(@PathParam("account") String account, @Principal CloudbeesPrincipal principal) throws IOException {
        Map contet = new HashedMap();

        //Check if the token was granted to this account and also check if the user with oauth token is admin of this account
        principal.authorizeAccount(account, true);

        contet.put("account", account);
        contet.put("is_admin", principal.isInAdminRole());
        contet.put("email", principal.getEmail());
        return contet;
    }
}
