package bnf;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity
public class Drug {
    @Id 
	private String name;
	@Embedded
	private List<Interaction> interactions;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Interaction> getInteractions() {
		return interactions;
	}
	public void setInteractions(List<Interaction> interactions) {
		this.interactions = interactions;
	}

}
