package io.roqu.nuxeo.client;

import org.nuxeo.client.NuxeoClient;
import org.nuxeo.client.objects.Document;
import org.nuxeo.client.objects.Documents;
import org.nuxeo.client.objects.Repository;
import org.nuxeo.client.objects.blob.Blob;
import org.nuxeo.client.objects.blob.FileBlob;
import org.nuxeo.client.objects.upload.BatchUpload;
import org.nuxeo.client.objects.user.User;
import org.nuxeo.client.spi.auth.PortalSSOAuthInterceptor;

import java.io.File;

public class CreateDoc {

    public CreateDoc() {



        NuxeoClient clientAdm = new NuxeoClient.Builder()
                .url("http://localhost:8081/nuxeo")
                .authentication(new PortalSSOAuthInterceptor("Administrator", "nuxeo5secretkey"))
                .schemas("*") // fetch all document schemas
                .connect();

        clientAdm.userManager().searchGroup("");

        Document user = clientAdm.operation("User.Get")
                .param("login", "bsimpson")
                .execute();

        if(user == null) {
            clientAdm.operation("User.CreateOrUpdate")
                    .param("username", "bsimpson")
                    .param("email", "bart.simpson@ac-rennes.fr")
                    .param("firstName", "Bart")
                    .param("lastName", "Simpson")
                    .param("mode", "create")
                    .execute();

        }

//        NuxeoClient client = new NuxeoClient.Builder()
//                .url("http://localhost:8081/nuxeo")
//                .authentication(new PortalSSOAuthInterceptor("bsimpson", "nuxeo5secretkey"))
//                .schemas("*") // fetch all document schemas
//                .connect();

        Repository moduloReqpo = clientAdm.repository("modulo");

        Documents root = moduloReqpo.fetchChildrenByPath("/");
        Document defaultDomain = root.getDocuments().get(0);

        Document workspaces = Document.createWithName("workspaces", "WorkspaceRoot");
        workspaces = readOrCreate(moduloReqpo, defaultDomain, workspaces);

        Document espaceEcole = Document.createWithName("e-0440654U", "Workspace");
        espaceEcole.setPropertyValue("dc:title","Ecole primaire publique Longchamp");
        espaceEcole = readOrCreate(moduloReqpo, workspaces, espaceEcole);

        Document documents = Document.createWithName("documents", "Folder");
        documents = readOrCreate(moduloReqpo, espaceEcole, documents);

        Document room = Document.createWithName("cp-a", "Room");
        room.setPropertyValue("dc:title","Salle de CP-A");
        documents = readOrCreate(moduloReqpo, espaceEcole, room);

        // create a new batch
        BatchUpload batchUpload = clientAdm.batchUploadManager().createBatch();

        Blob fileBlob = new FileBlob(new File("/home/loic/doublons.csv"));
        batchUpload = batchUpload.upload("0", fileBlob);

        Document ficAccueil = Document.createWithName("fic", "File");
        ficAccueil.setPropertyValue("file:content", batchUpload.getBatchBlob());
        ficAccueil = readOrCreate(moduloReqpo, documents, ficAccueil);


    }



    private Document readOrCreate(Repository repository, Document parentDoc, Document doc) {

        Documents children = repository.fetchChildrenById(parentDoc.getId());

        // Get
        if(children != null) {
            for (Document child : children.getDocuments()) {

                String expected = parentDoc.getPath().concat("/").concat(doc.getName());

                if (expected.equals(child.getPath())) {
                    System.out.println("Found " + child.getId() + " " + child.getPath());
                    doc = child;
                    return doc;
                }
            }
        }

        // If not found, create it.
        Document created = repository.createDocumentById(parentDoc.getId(), doc);
        System.out.println("Create "+created.getId()+ " "+created.getPath());

        return created;

    }


    public static void main(String[] args) {

        new CreateDoc();

    }

}