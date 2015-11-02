package bnf;

import org.mongodb.morphia.annotations.Embedded;

import org.mongodb.morphia.annotations.Reference;

@Embedded
public class Interaction {
	private String level;
	private String note;
	@Reference
	private Drug concernedDrug;
	@Reference
	private Drug interactingDrug;
	private String explanation;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Drug getConcernedDrug() {
		return concernedDrug;
	}

	public void setConcernedDrug(Drug concernedDrug) {
		this.concernedDrug = concernedDrug;
	}

	public Drug getInteractingDrug() {
		return interactingDrug;
	}

	public void setInteractingDrug(Drug interactingDrug) {
		this.interactingDrug = interactingDrug;
	}
	public String getExplanation() {
		return explanation;
	}
	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

}
