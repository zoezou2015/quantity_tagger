package org.statnlp.example.math.type;

import java.io.Serializable;

public class ComparisonSample implements Serializable {

	private static final long serialVersionUID = -5249124003316109181L;
	Quantity largerQuantity;
	Quantity smallerQuantity;
	Quantity unknown;

	public ComparisonSample(Quantity largerQuantity, Quantity smallerQuantity, Quantity unknown) {
		this.largerQuantity = largerQuantity;
		this.smallerQuantity = smallerQuantity;
		this.unknown = unknown;
	}

	public ComparisonSample() {

	}

	/**
	 * @return the largerQuantity
	 */
	public Quantity getLargerQuantity() {
		return largerQuantity;
	}

	/**
	 * @param largerQuantity
	 *            the largerQuantity to set
	 */
	public void setLargerQuantity(Quantity largerQuantity) {
		this.largerQuantity = largerQuantity;
	}

	/**
	 * @return the smallerQuantity
	 */
	public Quantity getSmallerQuantity() {
		return smallerQuantity;
	}

	/**
	 * @param smallerQuantity
	 *            the smallerQuantity to set
	 */
	public void setSmallerQuantity(Quantity smallerQuantity) {
		this.smallerQuantity = smallerQuantity;
	}

	/**
	 * @return the difference
	 */
	public Quantity getUnknown() {
		return unknown;
	}

	/**
	 * @param difference
	 *            the difference to set
	 */
	public void setUnknown(Quantity unknown) {
		this.unknown = unknown;
	}

}