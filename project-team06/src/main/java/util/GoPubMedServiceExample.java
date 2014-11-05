package util;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.client.ClientProtocolException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;

public class GoPubMedServiceExample {

  public static void main(String[] args) throws ClientProtocolException, IOException,
          ConfigurationException {
    // String text = "Is Rheumatoid Arthritis more common in men or women?";
    String text = "Are there any DNMT3 proteins present in plants?";
    GoPubMedService service = new GoPubMedService(args[0]);
    OntologyServiceResponse.Result diseaseOntologyResult = service
            .findDiseaseOntologyEntitiesPaged(text, 0);
    System.out.println("Disease ontology: " + diseaseOntologyResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(text,
            0, 10);
    System.out.println("Gene ontology: " + geneOntologyResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
    System.out.println("Jochem: " + jochemResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
    System.out.println("MeSH: " + meshResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
    System.out.println("UniProt: " + uniprotResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
            .findLinkedLifeDataEntitiesPaged(text, 0);
    System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
    for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
      System.out.println(" > " + entity.getEntity());
      for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
        System.out.println("   - labels: " + relation.getLabels());
        System.out.println("   - pred: " + relation.getPred());
        System.out.println("   - sub: " + relation.getSubj());
        System.out.println("   - obj: " + relation.getObj());
      }
    }
    PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
    System.out.println(pubmedResult.getSize());
  }
}
