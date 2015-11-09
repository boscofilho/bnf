package neo4j;

import java.net.UnknownHostException;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.mongodb.MongoClient;

import bnf.Drug;
import bnf.Interaction;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;



public class Mongo2Neo {
	private static enum RelTypes implements RelationshipType
	{
		INTERACTS,
		STRONGLY_INTERACTS;
	}
	private static enum NodeTypes implements Label
	{
		DRUG
	}
	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub

		//GET info from mongoDB
		Morphia morphia = new Morphia();		
		Datastore datastore = morphia.createDatastore(new MongoClient(), "bnf");
		List<Drug> drugs = datastore.find(Drug.class).asList();

		GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
		@SuppressWarnings("deprecation")
		GraphDatabaseService graphDb= dbFactory.newEmbeddedDatabase("/home/bosco/Documents/neo4j-community-2.3.0/data/drugInteractions.db");

		try (Transaction tx = graphDb.beginTx()) {
			//Populate Nodes
			for (Drug drug : drugs) {
				Node registeredDrug = graphDb.findNode(NodeTypes.DRUG, "name", drug.getName());

				if(registeredDrug == null){
					registeredDrug = graphDb.createNode(NodeTypes.DRUG);
					registeredDrug.setProperty("name", drug.getName());
				}
				//Check for each interaction if the drug is already registered
				if(drug.getInteractions() != null)
				for (Interaction inter : drug.getInteractions()) {


					Node registeredDrugInter = graphDb.findNode(NodeTypes.DRUG, "name", inter.getInteractingDrug().getName());


					if(registeredDrugInter == null){
						registeredDrugInter = graphDb.createNode(NodeTypes.DRUG);
						registeredDrugInter.setProperty("name", inter.getInteractingDrug().getName());
					}
					//Check type of interaction
					RelTypes rt;
					if(inter.getLevel().equals("DANGER"))
						rt = RelTypes.STRONGLY_INTERACTS;
					else
						rt = RelTypes.INTERACTS;

					//Create interaction
					Relationship interaction = registeredDrug.createRelationshipTo(registeredDrugInter, rt);
					interaction.setProperty("explanation", inter.getExplanation());
					interaction.setProperty("note", inter.getNote());

				}
			}
			tx.success();
		}

		System.out.println("Done successfully");

		//		~/Documents/neo4j-community-2.3.0/data


	}

}
