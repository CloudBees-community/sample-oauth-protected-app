# Sample OAuth protected app

This is a sample OAuth protected app. This is to demonstrate how you can go about developing an app and securing it with
CloudBees OAuth service.


## Steps to Run

### Installed CloudBees SDK tool

Instruction to install Bees SDK: http://wiki.cloudbees.com/bin/view/RUN/BeesSDK

#### Install CloudBees OAuth plugin

    $ bees plugin:install org.cloudbees.sdk.plugins:oauth-plugin

### Register a an app with CloudBees OAuth server

Run the bees SDK command below and note down the client_id and client_secret from the response. Replace
OAUTH_APP_CLIENT_ID and OAUTH_APP_CLIENT_SECRET in the web.xml by the respective values.

* You also need to use a different oauth scope than the one defined inside @Secure annotation

    @Secure(scopes={"https://myapp.example.com/read_account_info"})

Here https://myapp.example.com/read_account_info is globally unique so you must register a different scope URL. You
must change the scope value inside @Secure annotation as well.

    $ bees oauth:app:register --account CLOUDBEES_ACCOUNT_NAME --grant-type client_credentials --url https://localhost/testapp -n "Protected Test App" --callback https://localhost/testapp/callback -S https://myprotectedapp.example.com/account_info="Allow to get account info"

### Start App

This will start app at http://localhost:9090

    $ mvn jetty:run

## Test

### Generate an access_token for testing
    $ bees oauth:token:create -clientId xxx -clientSecret xxxx -scope https://myprotectedapp.example.com/account_info

Or if you do not have clientId and clientSecret

    $ bees oauth:token:create  -note "Test client app" -note-url http://localhost/clientapp -scope https://myprotectedapp.example.com/account_info

### Get account info

    $ curl -v "http://localhost:9090/CLOUDBEES_ACCOUNT_NAME?access_token=xxxx"