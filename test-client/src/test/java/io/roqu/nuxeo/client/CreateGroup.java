package io.roqu.nuxeo.client;

import org.nuxeo.client.NuxeoClient;
import org.nuxeo.client.objects.Document;
import org.nuxeo.client.objects.Documents;
import org.nuxeo.client.objects.Repository;
import org.nuxeo.client.objects.blob.Blob;
import org.nuxeo.client.objects.blob.FileBlob;
import org.nuxeo.client.objects.upload.BatchUpload;
import org.nuxeo.client.objects.user.Group;
import org.nuxeo.client.spi.auth.PortalSSOAuthInterceptor;

import java.io.File;
import java.util.List;

public class CreateGroup {

    public CreateGroup() {



        NuxeoClient clientAdm = new NuxeoClient.Builder()
                .url("http://localhost:8080/nuxeo")
                .authentication(new PortalSSOAuthInterceptor("Administrator", "nuxeo5secretkey"))
                .schemas("*") // fetch all document schemas
                .connect();



        Group readers = new Group();
        readers.setGroupName("xyz_readers");
        readers.setGroupLabel("All readers of xyz workspace");

        clientAdm.userManager().createGroup(readers);

        Group writers = new Group();
        writers.setGroupName("xyz_writers");
        writers.setGroupLabel("All writers of xyz workspace");

        clientAdm.userManager().createGroup(writers);


        Group admins = new Group();
        admins.setGroupName("xyz_admins");
        admins.setGroupLabel("All admins of xyz workspace");
        admins.setMemberUsers(List.of("bsimpson","Administrator"));


        clientAdm.userManager().createGroup(admins);

        Group members = new Group();
        members.setGroupName("xyz_members");
        members.setGroupLabel("All members of xyz workspaces");
        members.setMemberGroups(List.of("xyz_readers", "xyz_writers", "xyz_admins"));

        clientAdm.userManager().createGroup(members);
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

        new CreateGroup();

    }

}