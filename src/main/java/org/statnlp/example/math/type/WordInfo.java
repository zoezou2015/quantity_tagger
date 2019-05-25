package org.statnlp.example.math.type;

import java.io.Serializable;

public class WordInfo implements Serializable {
	private static final long serialVersionUID = 4625892280057511668L;
	private String _name;
	private String _posTag;
	private String _lemma;
	private String _ne;
	private boolean _isQuantity = false;

	public WordInfo(String name, String lemma, String posTag, String ne) {

	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getPosTag() {
		return _posTag;
	}

	public void setPosTag(String posTag) {
		this._posTag = posTag;
	}

	public String getLemma() {
		return _lemma;
	}

	public void setLemma(String lemma) {
		this._lemma = lemma;
	}

	public String getNe() {
		return _ne;
	}

	public void setNe(String ne) {
		this._ne = ne;
	}

	public boolean getIsQuantity() {
		return this._isQuantity;
	}

	public void setIsQuantity(boolean isQuantity) {
		this._isQuantity = isQuantity;
	}
}
