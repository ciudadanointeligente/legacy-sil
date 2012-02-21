package cl.ciudadanointeligente.sil.processor;

import cl.votainteligente.legislativo.model.Bill;
import cl.votainteligente.legislativo.model.MergedBillContainer;

import cl.ciudadanointeligente.sil.model.SilBill;
import cl.ciudadanointeligente.sil.parser.BillParser;

import org.hibernate.Session;

import java.util.HashSet;
import java.util.Set;

public class MergedBillProcessor implements Processor<SilBill, MergedBillContainer> {

	BillProcessor billProcessor;
	BillParser billParser;
	private final boolean test;

	public MergedBillProcessor(BillProcessor billProcessor, BillParser billParser, boolean test) {
		this.billProcessor = billProcessor;
		this.billParser = billParser;
		this.test = test;
	}

	public MergedBillContainer process(SilBill newBill, Session session) throws Throwable {
		MergedBillContainer mergedBillContainer = new MergedBillContainer();
		Set<SilBill> mergedSilBills = new HashSet<SilBill>();
		Set<Bill> mergedBills = new HashSet<Bill>();

		for (String mergedBulletinNumber : newBill.getMergedBulletinNumbers()) {
			SilBill mergedSilBill = billParser.getBill(mergedBulletinNumber);
			mergedSilBills.add(mergedSilBill);
		}
		mergedSilBills.add(newBill);

		for (SilBill mergedBill : mergedSilBills) {
			Bill processedBill = billProcessor.process(mergedBill, session);
			mergedBills.add(processedBill);
		}
		mergedBillContainer.setBills(mergedBills);

		for (Bill processedBill : mergedBills) {
			processedBill.setMergedBills(mergedBillContainer);
		}

		if (!test) {
			session.save(mergedBillContainer);
		}
		return mergedBillContainer;
	}
}
