package com.paylogic.scanwarelite;

public enum PaymentCode {
	/* 
	 * 202 in case automatic payment was rejected
	 * 211 should never occur, customer hasn't paid
	 * 212 should never occur, customer cancelled the order
	 * 213 customer has not paid the full amount, but somehow still got a ticket
	 * 214 paylogic has refunded this ticket, thus it is not valid anymore
	 * 215 something went wrong with payment, needs to be taken care of manually by customer service
	 */
	
	CHARGED_BACK(202), EXPIRED(211), CANCELLED(212), INSUFFICIENT_PAYMENT(213), REFUNDED(
			214), ON_HOLD(221);

	private int code;

	private PaymentCode(int code) {
		this.code = code;
	}
	
	public int getCode(){
		return code;
	}
}
