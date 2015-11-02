package bnf;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

public class DrugParser {
	
	public void parseAllDrugs() throws IOException{
		String baseURL = "http://www.evidence.nhs.uk";
		String url = 
				"http://www.evidence.nhs.uk/formulary/bnf/current/a1-interactions/list-of-drug-interactions/cytotoxics/monoclonal-antibodies/golimumab";
		//http://www.evidence.nhs.uk/formulary/bnf/current/a1-interactions/list-of-drug-interactions/zinc
		//parse the first http://www.evidence.nhs.uk/formulary/bnf/current/a1-interactions/list-of-drug-interactions/abatacept
		
		//get the HTML document
		Document initdoc = Jsoup.connect("http://www.evidence.nhs.uk/formulary/bnf/current/a1-interactions/list-of-drug-interactions/abatacept")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
	               .referrer("http://www.google.com") 
	               .timeout(1000*5)             
	               .get();
		
		String nextDrugURL = parseDrug(initdoc);
		
		while (!nextDrugURL.equals("/formulary/bnf/current/a1-interactions/list-of-drug-interactions/zinc")){
			Document doc = Jsoup.connect(baseURL + nextDrugURL)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		               .referrer("http://www.google.com") 
		               .timeout(1000*5)             
		               .get();
			nextDrugURL = parseDrug(doc);
		}
		Document finaldoc = Jsoup.connect("http://www.evidence.nhs.uk/formulary/bnf/current/a1-interactions/list-of-drug-interactions/zinc")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
	               .referrer("http://www.google.com") 
	               .timeout(1000*5)             
	               .get();
		parseDrug(finaldoc);
	}
	
	public String parseDrug(Document doc) throws IOException {
		
		//Set up Database
		Morphia morphia = new Morphia();
		
		Datastore datastore = morphia.createDatastore(new MongoClient(), "bnf");
		morphia.map(Drug.class).map(Interaction.class);


		
		//Create Drug
		Drug newDrug = new Drug();
		newDrug.setName(doc.select("h1").text());
	    ArrayList<Interaction> newInteractions = new ArrayList<Interaction>();
		
	    //Create interactions
		Elements interactions = doc.select("tr.interaction_row"); 
		for (Element interaction : interactions) {
			Drug interactingDrug = new Drug();
			Interaction newInteraction = new Interaction();
			newInteraction.setConcernedDrug(newDrug);
			
			//get elements arrays
			Elements icname = interaction.getElementsByAttributeValue("class", "interaction_column name");
			Elements ici = interaction.getElementsByAttributeValue("class", "interaction_column interaction");
			Elements iciiad = interaction.getElementsByAttributeValue("class", "interaction_column interaction important alert-danger");
			Elements icnote = interaction.getElementsByAttributeValue("class", "interaction_column note");
			
			
			if(icname != null && !icname.isEmpty()){
				interactingDrug.setName(icname.get(0).text());
				newInteraction.setInteractingDrug(interactingDrug);
				
				if(ici != null && !ici.isEmpty()){
					newInteraction.setExplanation(ici.text());
					newInteraction.setLevel("STANDARD");
				}
				else if(iciiad != null && !iciiad.isEmpty()){
					newInteraction.setExplanation(iciiad.text());
					newInteraction.setLevel("DANGER");

				}
				else 
					newInteraction.setExplanation("NO EXPLANATION");
				
				if (icnote != null && !icnote.isEmpty()){
					newInteraction.setNote(icnote.text());
					if(icnote.text().equals(""))
						newInteraction.setNote("NO NOTES");
				}
				else
					newInteraction.setNote("NO NOTES");
					
				//String level = interaction.getClass()
				System.out.println("CONCERNED: " + newInteraction.getConcernedDrug().getName() + "NOME: " + newInteraction.getInteractingDrug().getName() +
						"  INTERACAO: "  + newInteraction.getExplanation() + 
						" LEVEL: " + newInteraction.getLevel() +
						" NOTES: " + newInteraction.getNote());
			newInteractions.add(newInteraction);
			}
			
        }
		newDrug.setInteractions(newInteractions);
		datastore.save(newDrug);
		return doc.select("a[rel=next]").attr("href");
		
	}

}
