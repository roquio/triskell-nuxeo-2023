package io.roqu.nuxeo.client;

import org.nuxeo.client.NuxeoClient;
import org.nuxeo.client.objects.Document;
import org.nuxeo.client.objects.Documents;
import org.nuxeo.client.objects.Repository;
import org.nuxeo.client.objects.user.Group;
import org.nuxeo.client.spi.auth.PortalSSOAuthInterceptor;

import java.util.List;

public class CreateWks {

    public CreateWks() {



        NuxeoClient clientAdm = new NuxeoClient.Builder()
                .url("http://localhost:8081/nuxeo")
                .authentication(new PortalSSOAuthInterceptor("Administrator", "nuxeo5secretkey"))
                .schemas("*") // fetch all document schemas
                .connect();

        clientAdm.operation("Repository.WorkspaceCreation")
                .param("title","Un espace pour démarrer")
                .param("owner", "Administrator")
                .execute();


    }




    public static void main(String[] args) {

        new CreateWks();

    }

}